package com.binary_dysfunction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

public final class Updater {

    // private final AtomicInteger count = new AtomicInteger(0);

    private Thread updateThread;
    private volatile boolean running = false;
    private volatile int counter = 0;
    public volatile List<Account> membersList = new ArrayList<>();
    public volatile List<Chat> chatsList = new ArrayList<>();
    public volatile List<Message> currentChatsMessages = new ArrayList<>();

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

                SwingUtilities.invokeLater(() -> {
                    // System.out.println("Update"+counter);
                    if (counter % 20 == 0) {
                        // int randomNum = new Random().nextInt(0, members.size());
                        // Account randomAcc = members.get(randomNum);
                        // Toast.show(null, Main.serverPath + randomAcc.profilePicturePath, randomAcc.fullName, "Meow!", 5000, Toast.Position.BOTTOM_RIGHT, true);
                    }
                    try {
                        updateAccountList();
                        updateChatList();
                        if (ChatPanel.currentTargetUser != null) updateMessages();
                        else currentChatsMessages.clear();
                    } catch (IOException e) {}
                    counter++;
                });
                try {
                    Thread.sleep(500);
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
            content = Files.readString(JSONConfigurations.ACCOUNT_PATH);
        } catch (IOException e) {
            return;
        }
        JSONArray accounts = new JSONArray(content);

        membersList.clear();

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

            // System.out.println(username);
            membersList.add(new Account(username, passwordHash, fullName, description, profilePicturePath, uid, assemblies, cloudActivated, email, chats));
        }
        // System.out.println("Account-List updated");
    }

    public void updateChatList() throws IOException {

        String content = Files.readString(JSONConfigurations.CHATS_PATH);
        JSONArray chats = new JSONArray(content);

        chatsList.clear();

        for (int i = 0; i < chats.length(); i++) {
            JSONObject chat = chats.getJSONObject(i);

            String id = chat.getString("id");
            List<Object> members = chat.getJSONArray("members").toList();
            boolean isGroupChat = chat.getBoolean("isGroupChat");
            String groupName = chat.getString("groupName");
            String pfpPath = chat.getString("pfpPath");

            chatsList.add(new Chat(id, members, isGroupChat, groupName, pfpPath));
        }
    }

    public Account getAccountByUID(String uid) {
        for (Account acc : membersList) {
            if (acc.uid.equals(uid)) return acc;
        }
        return null;
    }

    public void updateMessages() throws IOException {

        Path messagesPath = Path.of(JSONConfigurations.CHATS_DIR + ChatPanel.currentTargetUser.id + "/messages.json");
        String content = Files.readString(messagesPath);
        JSONArray messages = new JSONArray(content);

        currentChatsMessages.clear();

        for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.getJSONObject(i);

            String id = message.getString("id");
            String messageContent = message.getString("content");
            long date = message.getLong("date");
            boolean isRead = message.getBoolean("isRead");
            String senderUID = message.getString("senderUID");

            currentChatsMessages.add(new Message(id, messageContent, date, isRead, senderUID));
        }

        ChatPanel.loadMessages();
    }
}