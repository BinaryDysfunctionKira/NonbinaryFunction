package com.binary_dysfunction;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

public final class Updater {

    private static final long POLL_INTERVAL_MS = 500;

    private Thread updateThread;
    private volatile boolean running = false;
    private volatile int counter = 0;

    // these get replaced, never modified in place (the UI reads them from the EDT)
    public volatile List<Account> membersList = new ArrayList<>();
    public volatile List<Chat> chatsList = new ArrayList<>();
    public volatile List<Message> currentChatsMessages = new ArrayList<>();

    private volatile String loadedChatId = null;

    // chat id -> message ids we've already handled, used to spot new arrivals for toasts
    private final Map<String, Set<String>> knownMessageIds = new ConcurrentHashMap<>();

    // chat id -> has unread messages for the logged-in user, read by ChatPanel to color its list
    private final Map<String, Boolean> unreadByChatId = new ConcurrentHashMap<>();

    // guards loadedChatId/currentChatsMessages against the updater thread and
    // an EDT button click (which calls updateMessages()) racing each other
    private final Object messagesLock = new Object();

    // chat id -> timestamp of its most recent message, used to sort the chat list
    private final Map<String, Long> lastMessageDateByChatId = new ConcurrentHashMap<>();

    public long getLastMessageDate(String chatId) {
        return lastMessageDateByChatId.getOrDefault(chatId, 0L);
    }

    public Updater() {
        startUpdating();
    }

    public void startUpdating() {
        if (updateThread != null && updateThread.isAlive()) {
            return;
        }

        try {
            updateAccountList();
        } catch (IOException e) {}

        running = true;

        updateThread = new Thread(() -> {
            while (running) {

                // reading + parsing stays on this thread, only the result goes to the EDT
                try {
                    updateAccountList();
                    updateChatList();
                } catch (IOException e) {
                    // file busy or missing, just try again next round
                }
                pollMessages();

                counter++;

                try {
                    Thread.sleep(POLL_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "updater");

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void stopUpdating() {
        running = false;
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    public void updateAccountList() throws IOException {

        String content;
        try {
            content = Files.readString(JSONConfigurations.getAccountPath());
        } catch (IOException e) {
            return;
        }
        JSONArray accounts = new JSONArray(content);

        List<Account> fresh = new ArrayList<>(accounts.length());

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);

            String username = acc.getString("username");
            String passwordHash = acc.getString("passwordHash");
            String fullName = acc.getString("fullName");
            String description = acc.getString("description");
            String profilePicturePath = acc.getString("profilePicturePath");
            String uid = acc.getString("uid");
            List<Object> assemblies = acc.getJSONArray("assemblies").toList();
            boolean cloudActivated = acc.getBoolean("cloudActivated");
            String email = acc.getString("email");
            List<Object> chats = acc.getJSONArray("chats").toList();

            fresh.add(new Account(username, passwordHash, fullName, description,
                    profilePicturePath, uid, assemblies, cloudActivated, email, chats));
        }

        membersList = Collections.unmodifiableList(fresh);
    }

    public void updateChatList() throws IOException {

        String content = Files.readString(JSONConfigurations.getChatsPath());
        JSONArray chats = new JSONArray(content);

        List<Chat> fresh = new ArrayList<>(chats.length());

        for (int i = 0; i < chats.length(); i++) {
            JSONObject chat = chats.getJSONObject(i);

            String id = chat.getString("id");
            List<Object> members = chat.getJSONArray("members").toList();
            boolean isGroupChat = chat.getBoolean("isGroupChat");
            String groupName = chat.getString("groupName");
            String pfpPath = chat.getString("pfpPath");

            fresh.add(new Chat(id, members, isGroupChat, groupName, pfpPath));
        }

        chatsList = Collections.unmodifiableList(fresh);
    }

    public Account getAccountByUID(String uid) {
        for (Account acc : membersList) {
            if (acc.uid.equals(uid)) return acc;
        }
        return null;
    }

    /**
     * Runs every cycle. Cheaply checks every chat the user is in for new
     * message files (just filenames, no parsing) and toasts for anything
     * new that isn't from the user and isn't in the chat they have open
     * right now. Then refreshes the open chat's message list, same as before.
     */
    private void pollMessages() {
        Account me = Main.loggedInAccount;
        if (me == null) return;

        for (Chat chat : chatsList) {
            if (isMember(chat, me.uid)) {
                checkForNewMessages(chat, me.uid);
            }
        }

        refreshOpenChat();
    }

    private void checkForNewMessages(Chat chat, String myUid) {
        Set<String> currentIds;
        try {
            currentIds = JSONConfigurations.listMessageIds(chat.id);
        } catch (IOException e) {
            return; // folder might be mid-sync, try again next round
        }

        Set<String> known = knownMessageIds.get(chat.id);
        if (known == null) {
            // first time we look at this chat - remember what's already
            // there, don't fire a toast for the whole chat history
            knownMessageIds.put(chat.id, currentIds);
            computeInitialUnreadStatus(chat, myUid);
            return;
        }

        if (currentIds.equals(known)) return;

        Set<String> newIds = new HashSet<>(currentIds);
        newIds.removeAll(known);

        Set<String> handled = new HashSet<>(known);
        for (String newId : newIds) {
            if (notifyNewMessage(chat, newId, myUid)) {
                handled.add(newId);
            }
            // otherwise the file probably hasn't finished syncing yet -
            // leave it out so it's picked up again as "new" next round
        }

        knownMessageIds.put(chat.id, handled);
    }

    /**
     * Runs once per chat, the first time we see it (e.g. right after login).
     * Compares existing messages against the stored "read up to" marker so
     * chats with a real backlog show up as unread immediately, not just
     * ones that receive a new message after the app started.
     */
    private void computeInitialUnreadStatus(Chat chat, String myUid) {
        try {
            long lastRead = JSONConfigurations.getLastReadDate(chat.id, myUid);
            List<Message> messages = JSONConfigurations.readMessages(chat.id);

            boolean unread = false;
            long latest = 0;
            for (Message msg : messages) {
                if (msg.date > latest) latest = msg.date;
                if (!myUid.equals(msg.senderUID) && msg.date > lastRead) {
                    unread = true;
                }
            }
            if (latest > 0) lastMessageDateByChatId.put(chat.id, latest);
            setUnread(chat.id, unread);
        } catch (IOException e) {
            // couldn't read right now - it'll still get flagged correctly
            // the moment a genuinely new message arrives
        }
    }

    /** Updates the unread flag and, if it actually changed, asks the sidebar to redraw. */
    private void setUnread(String chatId, boolean unread) {
        Boolean previous = unreadByChatId.put(chatId, unread);
        if (previous == null || previous != unread) {
            SwingUtilities.invokeLater(ChatPanel::refreshChatList);
        }
    }

    public boolean isChatUnread(String chatId) {
        return unreadByChatId.getOrDefault(chatId, false);
    }

    /**
     * Returns true once the message file was fully readable, toast or not.
     *
     * NOTE: a toast is only suppressed when ChatPanel is BOTH currently
     * attached to the visible frame (ChatPanel.isPanelVisible()) AND showing
     * this exact chat. currentTargetUser alone isn't enough to go on -
     * it's a static field that keeps remembering the last-viewed chat even
     * after you've navigated away to Home/Profile/Admin/Calendar, so relying
     * on it by itself would permanently mute whichever chat you looked at
     * last, no matter where you are in the app.
     */
    private boolean notifyNewMessage(Chat chat, String messageId, String myUid) {
        Message msg;
        try {
            msg = JSONConfigurations.readMessage(chat.id, messageId);
        } catch (IOException e) {
            return false;
        }
        if (msg == null) return false; // still syncing, retry next round

        lastMessageDateByChatId.merge(chat.id, msg.date, Math::max); // see sorting section below

        if (myUid.equals(msg.senderUID)) return true; // our own message, nothing to show

        Chat openChat = ChatPanel.currentTargetUser;
        if (ChatPanel.isPanelVisible() && openChat != null && openChat.id.equals(chat.id)) {
            return true;
        }

        setUnread(chat.id, true);

        Account sender = getAccountByUID(msg.senderUID);
        String senderName = sender != null ? sender.fullName : "Unbekannt";
        String iconPath = sender != null ? Main.serverPath + sender.profilePicturePath : null;

        String toastTitle = senderName;
        if (chat.isGroupChat) {
            String groupName = (chat.groupName != null && !chat.groupName.isBlank()) ? chat.groupName : "Gruppe";
            toastTitle = senderName + " (" + groupName + ")";
        }
        String finalTitle = toastTitle;

        SwingUtilities.invokeLater(() -> {
            if (iconPath != null) {
                Toast.show(null, iconPath, finalTitle, msg.content, 5000, Toast.Position.BOTTOM_RIGHT, true);
            } else {
                Toast.show(null, finalTitle, msg.content, 5000, Toast.Position.BOTTOM_RIGHT, true);
            }
        });

        return true;
    }

    private void refreshOpenChat() {
        Chat target = ChatPanel.currentTargetUser;

        synchronized (messagesLock) {
            if (target == null) {
                if (loadedChatId != null || !currentChatsMessages.isEmpty()) {
                    loadedChatId = null;
                    currentChatsMessages = new ArrayList<>();
                    SwingUtilities.invokeLater(ChatPanel::resetMessageCache);
                }
                return;
            }

            boolean chatChanged = !target.id.equals(loadedChatId);
            List<Message> fresh;
            try {
                fresh = JSONConfigurations.readMessages(target.id);
            } catch (IOException e) {
                return; // try again next round
            }

            if (!chatChanged && sameMessages(fresh, currentChatsMessages)) {
                return; // nothing new, don't touch the scrollbar
            }

            currentChatsMessages = fresh;
            loadedChatId = target.id;

            SwingUtilities.invokeLater(() -> {
                if (chatChanged) ChatPanel.resetMessageCache();
                ChatPanel.loadMessages();
            });
        }
    }

    /** Instant load, e.g. right after the user clicks a chat button. Runs on the EDT. */
    public void updateMessages() throws IOException {

        Chat target = ChatPanel.currentTargetUser;
        if (target == null) return;

        List<Message> fresh = JSONConfigurations.readMessages(target.id);

        synchronized (messagesLock) {
            boolean chatChanged = !target.id.equals(loadedChatId);
            currentChatsMessages = fresh;
            loadedChatId = target.id;

            if (chatChanged) ChatPanel.resetMessageCache();
            ChatPanel.loadMessages();
        }

        // baseline this chat so the poller doesn't toast for what we just loaded ourselves
        try {
            knownMessageIds.put(target.id, JSONConfigurations.listMessageIds(target.id));
        } catch (IOException ignored) {}

        // opening a chat counts as reading it
        Account me = Main.loggedInAccount;
        if (me != null) {
            long readUpTo = fresh.isEmpty() ? System.currentTimeMillis() : fresh.get(fresh.size() - 1).date;
            try {
                JSONConfigurations.markChatAsRead(target.id, me.uid, readUpTo);
            } catch (IOException ignored) {}
            setUnread(target.id, false);
        }
    }

    private static boolean isMember(Chat chat, String uid) {
        if (uid == null) return false;
        for (Object member : chat.members) {
            if (uid.equals(member == null ? null : member.toString())) return true;
        }
        return false;
    }

    // compares by field so it works without Message.equals()
    private static boolean sameMessages(List<Message> a, List<Message> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            Message x = a.get(i);
            Message y = b.get(i);
            if (!x.id.equals(y.id)) return false;
            if (!x.content.equals(y.content)) return false;
            if (x.date != y.date) return false;
            if (x.isRead != y.isRead) return false;
            if (!x.senderUID.equals(y.senderUID)) return false;
        }
        return true;
    }
}