package com.binary_dysfunction.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.binary_dysfunction.main.Main;
import com.binary_dysfunction.types.Account;
import com.binary_dysfunction.types.Message;
import com.binary_dysfunction.types.Ticket;

public class JSONConfigurations {

    public static Path getAccountPath() {
        return Paths.get(Main.serverPath + "/users/accounts.json");
    }

    public static Path getChatsPath() {
        return Paths.get(getChatsDir() + "chats.json");
    }

    public static String getChatsDir() {
        return Main.serverPath + "/chats/";
    }

    public static String getEventsDir() {
        return Main.serverPath + "/events/";
    }

    public static String getTicketsDir() {
        return getEventsDir() + "tickets/";
    }

    public static void addAccount(String username, String passwordHash) throws IOException {

        if (getAccountPath().getParent() != null) {
            Files.createDirectories(getAccountPath().getParent());
        }

        JSONArray accounts;
        if (Files.exists(getAccountPath())) {
            String content = Files.readString(getAccountPath());
            accounts = new JSONArray(content);
        } else {
            accounts = new JSONArray();
        }

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);
            if (acc.getString("username").equals(username)) {
                System.out.println("Account already taken");
                @SuppressWarnings("unused")
                int warning = JOptionPane.showConfirmDialog(null, "Benutzername bereits vergeben.", "Benutzername vergeben",
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        JSONObject newAccount = new JSONObject();
        newAccount.put("username", username);
        newAccount.put("passwordHash", passwordHash);
        newAccount.put("fullName", username);
        newAccount.put("description", "");
        newAccount.put("profilePicturePath", "nonbinary_function\\src\\main\\resources\\BinaryDysfunctionLogo.png");
        String uid = Config.hashPassword(Integer.toString(accounts.length()));
        newAccount.put("uid", uid);
        JSONArray assembliesArray = new JSONArray();
        if (Main.isServerNew && !Main.ownerSet) {
            assembliesArray.put("Owner");
            assembliesArray.put("Admin");
        }
        newAccount.put("assemblies", assembliesArray);
        newAccount.put("cloudActivated", false);
        newAccount.put("email", "");
        JSONArray chatsArray = new JSONArray();
        newAccount.put("chats", chatsArray);

        accounts.put(newAccount);

        Files.writeString(getAccountPath(), accounts.toString(4));

        File privateVault = new File(Main.serverPath + Config.ACCOUNTS_DIR + username + "/user-data/");
        privateVault.mkdirs();

        if (Main.isServerNew && !Main.ownerSet) {
            Main.config.saveServerOwner(uid);
            Main.ownerSet = true;
        }
    }

    public static void updateAccountField(String username, String fieldName, Object newValue) throws IOException {

        if (!Files.exists(getAccountPath())) {
            return; // nothing to update
        }

        String content = Files.readString(getAccountPath());
        JSONArray accounts = new JSONArray(content);

        boolean found = false;

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);
            if (acc.getString("username").equals(username)) {
                acc.put(fieldName, newValue); // overwrites the existing key, or adds it if missing
                found = true;
                break; // stop once found, assuming usernames are unique
            }
        }

        if (!found) {
            System.out.println("Account not found: " + username);
            return;
        }

        Files.writeString(getAccountPath(), accounts.toString(4));
    }

    public static Object getAccountField(String username, String fieldName) throws IOException {

        if (!Files.exists(getAccountPath())) {
            return null; // nothing to return
        }

        String content = Files.readString(getAccountPath());
        JSONArray accounts = new JSONArray(content);

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);
            if (acc.getString("username").equals(username)) {

                return acc.get(fieldName);
            }
        }
        return null;
    }

    public static List<Object> getAccountFieldList(String username, String fieldName) throws IOException {

        if (!Files.exists(getAccountPath())) {
            return null; // nothing to return
        }

        String content = Files.readString(getAccountPath());
        JSONArray accounts = new JSONArray(content);

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);
            if (acc.getString("username").equals(username)) {

                return acc.getJSONArray(fieldName).toList();
            }
        }
        return null;
    }

    public static Account logInAccount(String username, String passwordHash) throws IOException {

        if (getAccountPath().getParent() != null) {
            Files.createDirectories(getAccountPath().getParent());
        }

        JSONArray accounts;
        if (Files.exists(getAccountPath())) {
            String content = Files.readString(getAccountPath());
            accounts = new JSONArray(content);
        } else {
            accounts = new JSONArray();
        }

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);
            if (acc.getString("username").equals(username)) {
                System.out.println("Logged in: " + username);

                String fullName = acc.getString("fullName");
                String description = acc.getString("description");
                String profilePicturePath = acc.getString("profilePicturePath");
                String uid = acc.getString("uid");
                List<Object> assemblies = acc.getJSONArray("assemblies").toList();
                boolean cloudActivated = acc.getBoolean("cloudActivated");
                String email = acc.getString("email");
                List<Object> chats = acc.getJSONArray("chats").toList();
                return new Account(username, passwordHash, fullName, description, profilePicturePath, uid, assemblies, cloudActivated, email, chats);
            }
        }

        System.out.println("Account not found.");
        return null;
    }

    public static void removeAccount(String username) throws IOException {

        if (!Files.exists(getAccountPath())) {
            return;
        }

        String content = Files.readString(getAccountPath());
        JSONArray accounts = new JSONArray(content);

        JSONArray updatedAccounts = new JSONArray();
        boolean found = false;

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);
            if (acc.getString("username").equals(username)) {
                found = true;
            } else {
                updatedAccounts.put(acc);
            }
        }

        if (!found) {
            System.out.println("Account not found: " + username);
            return;
        }

        Files.writeString(getAccountPath(), updatedAccounts.toString(4));

        FileUtils.deleteDirectory(new File(Main.serverPath + Config.ACCOUNTS_DIR + username));
    }

    // NOTE: chats.json is still one shared file, same read-modify-write shape as
    // messages.json used to be. Left as is for now since chat creation is rare -
    // say the word if you want the same per-file treatment here too.
    public static void addChat(Account... member) throws IOException {

        if (getChatsPath().getParent() != null) {
            Files.createDirectories(getChatsPath().getParent());
        }

        JSONArray chats;
        if (Files.exists(getChatsPath())) {
            String content = Files.readString(getChatsPath());
            chats = new JSONArray(content);
        } else {
            chats = new JSONArray();
        }

        String id = "";
        boolean chatIDFound = false;
        while (!chatIDFound) {
            boolean idAlreadyTaken = false;
            String chatID = Integer.toString(new Random().nextInt());
            String hashedChatID = Config.hashPassword(chatID);
            for (int i = 0; i < chats.length(); i++) {
                JSONObject chat = chats.getJSONObject(i);
                if (chat.getString("id").equals(hashedChatID)) {
                    idAlreadyTaken = true;
                }
            }
            if (!idAlreadyTaken) {
                id = hashedChatID;
                chatIDFound = true;
            }
        }

        JSONObject newChat = new JSONObject();
        newChat.put("id", id);
        JSONArray members = new JSONArray();
        for (Account mbr : member) {
            members.put(mbr.uid);
        }
        newChat.put("members", members);
        boolean isGroupChat = false;
        if (member.length > 2) isGroupChat = true;
        newChat.put("isGroupChat", isGroupChat);
        if (isGroupChat) newChat.put("groupName", "New Group");
        else newChat.put("groupName", "New Chat");
        newChat.put("pfpPath", "");

        chats.put(newChat);

        Files.writeString(getChatsPath(), chats.toString(4));

        addMessage(id, "Neuer Chat wurde erstellt.", "Admin");

        for (Account mbr : member) {
            Object rawChats = getAccountField(mbr.username, "chats");
            JSONArray oldChats = (rawChats instanceof JSONArray) ? (JSONArray) rawChats : new JSONArray();
            oldChats.put(id);
            updateAccountField(mbr.username, "chats", oldChats);
        }
    }

    /**
     * Adds one or more accounts to an existing chat's member list and promotes
     * it to a group chat if it now has more than 2 members. Also appends the
     * chat id to each new member's own "chats" list, same as addChat() does
     * for the initial members. Accounts already in the chat are skipped.
     */
    public static void addMembersToChat(String chatId, List<Account> newMembers) throws IOException {

        if (!Files.exists(getChatsPath())) return;

        String content = Files.readString(getChatsPath());
        JSONArray chats = new JSONArray(content);

        boolean found = false;
        for (int i = 0; i < chats.length(); i++) {
            JSONObject chat = chats.getJSONObject(i);
            if (chat.getString("id").equals(chatId)) {
                JSONArray members = chat.getJSONArray("members");

                Set<String> existing = new HashSet<>();
                for (int j = 0; j < members.length(); j++) {
                    existing.add(members.getString(j));
                }

                for (Account acc : newMembers) {
                    if (!existing.contains(acc.uid)) {
                        members.put(acc.uid);
                        existing.add(acc.uid);
                    }
                }

                chat.put("members", members);
                if (members.length() > 2) chat.put("isGroupChat", true);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Chat not found: " + chatId);
            return;
        }

        Files.writeString(getChatsPath(), chats.toString(4));

        for (Account acc : newMembers) {
            Object rawChats = getAccountField(acc.username, "chats");
            JSONArray oldChats = (rawChats instanceof JSONArray) ? (JSONArray) rawChats : new JSONArray();

            boolean already = false;
            for (int i = 0; i < oldChats.length(); i++) {
                if (oldChats.getString(i).equals(chatId)) {
                    already = true;
                    break;
                }
            }
            if (!already) oldChats.put(chatId);

            updateAccountField(acc.username, "chats", oldChats);
        }
    }

    // --------------------------------------------------------------------
    // messages: one file per message instead of one shared array.
    //
    // Two clients can now both add a message at the same time without
    // clobbering each other, because each write only ever creates a brand
    // new file - it never opens or rewrites a file someone else might also
    // be writing to. Google Drive syncing a new file is a non-event; the
    // race only existed because everyone was fighting over the same file.
    // --------------------------------------------------------------------

    public static void addMessage(String chatsID, String messageContent, String senderUID) throws IOException {

        Path messagesDir = Path.of(getChatsDir() + chatsID + "/messages");
        Files.createDirectories(messagesDir);

        Path uploadsDir = Path.of(getChatsDir() + chatsID + "/uploads");
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        String id = generateUniqueMessageId(messagesDir);

        JSONObject newMessage = new JSONObject();
        newMessage.put("id", id);
        newMessage.put("content", messageContent);
        newMessage.put("date", System.currentTimeMillis());
        newMessage.put("isRead", false);
        newMessage.put("senderUID", senderUID);

        Path messageFile = messagesDir.resolve(id + ".json");

        // write to a temp file first, then rename into place - so nobody
        // (including Drive's own sync scan) can ever see a half-written file
        Path tmpFile = Files.createTempFile(messagesDir, id + "-", ".tmp");
        Files.writeString(tmpFile, newMessage.toString(4));
        Files.move(tmpFile, messageFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String generateUniqueMessageId(Path messagesDir) throws IOException {
        while (true) {
            String candidate = Config.hashPassword(Integer.toString(new Random().nextInt()));
            if (!Files.exists(messagesDir.resolve(candidate + ".json"))) {
                return candidate;
            }
        }
    }


    /** All messages of a chat, oldest first. */
    public static List<Message> readMessages(String chatsID) throws IOException {
        Path messagesDir = Path.of(getChatsDir() + chatsID + "/messages");
        List<Message> result = new ArrayList<>();

        if (!Files.isDirectory(messagesDir)) return result;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(messagesDir, "*.json")) {
            for (Path file : stream) {
                Message msg = parseMessageFile(file);
                if (msg != null) result.add(msg);
            }
        }

        result.sort((a, b) -> Long.compare(a.date, b.date));
        return result;
    }

    /** A single message by id, or null if it doesn't exist (yet). */
    public static Message readMessage(String chatsID, String messageId) throws IOException {
        Path messageFile = Path.of(getChatsDir() + chatsID + "/messages/" + messageId + ".json");
        return parseMessageFile(messageFile);
    }

    /**
     * Just the message ids of a chat - cheap to call often since it doesn't
     * parse any JSON, only lists filenames. Used to spot new messages.
     */
    public static Set<String> listMessageIds(String chatsID) throws IOException {
        Path messagesDir = Path.of(getChatsDir() + chatsID + "/messages");
        Set<String> ids = new HashSet<>();

        if (!Files.isDirectory(messagesDir)) return ids;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(messagesDir, "*.json")) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                ids.add(name.substring(0, name.length() - ".json".length()));
            }
        }

        return ids;
    }

    // --------------------------------------------------------------------
    // read markers: one small file per (chat, user), holding the timestamp
    // up to which that user has read the chat. Never touches a message file,
    // and only the user themself ever writes their own marker - so, same as
    // with messages, two writers can never collide on the same file.
    // --------------------------------------------------------------------

    public static void markChatAsRead(String chatsID, String readerUid, long readUpToDate) throws IOException {
        Path readsDir = Path.of(getChatsDir() + chatsID + "/reads");
        Files.createDirectories(readsDir);

        JSONObject marker = new JSONObject();
        marker.put("lastReadDate", readUpToDate);

        Path readFile = readsDir.resolve(readerUid + ".json");
        Path tmpFile = Files.createTempFile(readsDir, readerUid + "-", ".tmp");
        Files.writeString(tmpFile, marker.toString(4));
        Files.move(tmpFile, readFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    /** 0 if the user has never opened this chat (i.e. treat everything in it as unread). */
    public static long getLastReadDate(String chatsID, String readerUid) throws IOException {
        Path readFile = Path.of(getChatsDir() + chatsID + "/reads/" + readerUid + ".json");
        if (!Files.exists(readFile)) return 0L;

        String content;
        try {
            content = Files.readString(readFile);
        } catch (IOException e) {
            return 0L; // mid-sync, safest to treat as unread for now
        }
        if (content.isBlank()) return 0L;

        try {
            return new JSONObject(content).getLong("lastReadDate");
        } catch (JSONException e) {
            return 0L;
        }
    }

    private static Message parseMessageFile(Path file) throws IOException {
        if (!Files.exists(file)) return null;

        String content;
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            return null; // Drive might still be syncing this one, try again later
        }
        if (content.isBlank()) return null; // Drive briefly shows a 0-byte placeholder for new files sometimes

        JSONObject message;
        try {
            message = new JSONObject(content);
        } catch (JSONException e) {
            return null; // same story - caught it mid-write/mid-sync
        }

        String id = message.getString("id");
        String messageContent = message.getString("content");
        long date = message.getLong("date");
        boolean isRead = message.getBoolean("isRead");
        String senderUID = message.getString("senderUID");

        return new Message(id, messageContent, date, isRead, senderUID);
    }

    public static void updateChatField(String chatId, String fieldName, Object newValue) throws IOException {
        if (!Files.exists(getChatsPath())) return;

        String content = Files.readString(getChatsPath());
        JSONArray chats = new JSONArray(content);

        boolean found = false;
        for (int i = 0; i < chats.length(); i++) {
            JSONObject chat = chats.getJSONObject(i);
            if (chat.getString("id").equals(chatId)) {
                chat.put(fieldName, newValue);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Chat not found: " + chatId);
            return;
        }

        Files.writeString(getChatsPath(), chats.toString(4));
    }

    /**
     * Copies the chosen image into this chat's own pfp folder (giving it a
     * fresh, timestamped filename so old cached thumbnails don't stick around)
     * and points chats.json at it. Returns the new pfpPath, relative to
     * Main.serverPath, so the caller can use it immediately.
     */
    public static String setChatPfp(String chatId, File sourceImage) throws IOException {
        Path pfpDir = Path.of(getChatsDir() + chatId + "/pfp");
        Files.createDirectories(pfpDir);

        String name = sourceImage.getName();
        int dot = name.lastIndexOf('.');
        String extension = (dot >= 0) ? name.substring(dot) : "";
        String fileName = "pfp-" + System.currentTimeMillis() + extension;
        Path destination = pfpDir.resolve(fileName);

        Files.copy(sourceImage.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        // clean up previous pfp files for this chat so they don't pile up
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pfpDir, "pfp-*")) {
            for (Path file : stream) {
                if (!file.equals(destination)) Files.deleteIfExists(file);
            }
        }

        String relativePath = "/chats/" + chatId + "/pfp/" + fileName;
        updateChatField(chatId, "pfpPath", relativePath);
        return relativePath;
    }


    // ---------------------------------------------------------
    //                        TICKETS
    // ---------------------------------------------------------
    /**
     * The method creates a seperate JSON File for a new Ticket
     * @param eventName
     * @param count
     * @param location
     * @param price
     * @param date
     * @throws IOException
     */
    public static void addTicket(String eventName, int count, String location, double price, long date) throws IOException {

        if (Paths.get(getTicketsDir()) != null) {
            Files.createDirectories(Paths.get(getTicketsDir()));
        }

        String id = generateUniqueTicketId(Paths.get(getTicketsDir()));

        Path dirPath = Path.of(getTicketsDir() + eventName + "/");
        Files.createDirectories(dirPath);
        Path thisTicket = Path.of(getTicketsDir() + eventName + "/" + id + ".json");
        try {
            Files.createFile(thisTicket);
        } catch (IOException e) {}

        JSONArray tickets = new JSONArray();

        JSONObject newTicket = new JSONObject();
        newTicket.put("id", id);
        newTicket.put("owner", "");
        newTicket.put("eventName", eventName);
        newTicket.put("count", count);
        newTicket.put("location", location);
        newTicket.put("price", price);
        newTicket.put("date", date);
        newTicket.put("available", true);
        newTicket.put("registered", false);

        tickets.put(newTicket);

        Files.writeString(thisTicket, tickets.toString(4));
    }

    // Variables for creating a secure ticket id
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TICKET_ID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // ohne 0,O,1,I,L
    private static final int TICKET_ID_LENGTH = 8;

    /**
     * Generates a secure Ticket id
     * @param ticketsDir
     * @return
     * @throws IOException
     */
    private static String generateUniqueTicketId(Path ticketsDir) throws IOException {
        while (true) {
            String candidate = generateTicketIdCandidate();
            if (!Files.exists(ticketsDir.resolve(candidate + ".json"))) {
                return candidate;
            }
        }
    }

    // Generates a possible candidate for the ticket id. If not needed, discarded.
    private static String generateTicketIdCandidate() {
    StringBuilder sb = new StringBuilder(TICKET_ID_LENGTH + 1);
        for (int i = 0; i < TICKET_ID_LENGTH; i++) {
            if (i > 0 && i % 4 == 0) sb.append('-');
            sb.append(TICKET_ID_CHARS.charAt(RANDOM.nextInt(TICKET_ID_CHARS.length())));
        }
        return sb.toString();
    }

    public static List<Object> getEvents() {
        File folder = new File(getTicketsDir());
        List<Object> folderNames = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) folderNames.add(fileEntry.getName());
        }
        return folderNames;
    }
    /**
     * Returns the tickets as a List
     * @param dirName
     * @return
     */
    public static List<Ticket> getTicketsAsList(Object dirName) throws IOException {
        File dirPath = new File(getTicketsDir() + dirName.toString() + "/");
        List <Ticket> tickets = new ArrayList<>();
        for (File fileEntry : dirPath.listFiles()) {
            if (fileEntry.isFile() && FilenameUtils.getExtension(fileEntry.getName()).equals("json")) {
                String content = Files.readString(fileEntry.toPath());
                JSONArray ticketsArray = new JSONArray(content);
                JSONObject ticketObject = ticketsArray.getJSONObject(0);

                String id = ticketObject.getString("id");
                String owner = ticketObject.getString("owner");
                String eventName = ticketObject.getString("eventName");
                int count = ticketObject.getInt("count");
                String location = ticketObject.getString("location");
                double price = ticketObject.getDouble("price");
                long date = ticketObject.getLong("date");
                boolean available = ticketObject.getBoolean("available");
                boolean registered = ticketObject.getBoolean("registered");

                tickets.add(new Ticket(id, owner, eventName, count, location, price, date, available, registered));
            }
        }
        return tickets;
    }
    /**
     * Returns the names of the tickets as a List of Objects
     * @param dirName
     * @return
     */
    public static List<Object> getTickets(Object dirName) {
        File dirPath = new File(getTicketsDir() + dirName.toString() + "/");
        List <Object> tickets = new ArrayList<>();
        for (File fileEntry : dirPath.listFiles()) {
            if (fileEntry.isFile() && FilenameUtils.getExtension(fileEntry.getName()).equals("json")) tickets.add(FilenameUtils.removeExtension(fileEntry.getName()));
        }
        return tickets;
    }
    /**
     * Returns the ticket by Event Name and Ticket ID
     * @param eventName
     * @param ticketId
     * @return
     * @throws IOException
     */
    public static Ticket getTicket(Object eventName, Object ticketId) throws IOException {
        File file = new File(getTicketsDir() + eventName.toString() + "/" + ticketId.toString() + ".json");
        Path filePath = Path.of(file.getPath());

        if (!file.exists()) return null;

        String content = Files.readString(filePath);
        JSONArray ticketArray = new JSONArray(content);
        JSONObject ticketObject = ticketArray.getJSONObject(0);

        String id = ticketObject.getString("id");
        String owner = ticketObject.getString("owner");
        String eventNamee = ticketObject.getString("eventName");
        int count = ticketObject.getInt("count");
        String location = ticketObject.getString("location");
        double price = ticketObject.getDouble("price");
        long date = ticketObject.getLong("date");
        boolean available = ticketObject.getBoolean("available");
        boolean registered = ticketObject.getBoolean("registered");

        return new Ticket(id, owner, eventNamee, count, location, price, date, available, registered);
    }
    /**
     * Changes the value of the JSON-Object, depending by the field, Event Name and Ticket ID
     * @param eventName
     * @param ticketId
     * @param field
     * @param value
     * @throws IOException
     */
    public static void changeTicketValue(Object eventName, Object ticketId, String field, Object value) throws IOException {
        File file = new File(getTicketsDir() + eventName.toString() + "/" + ticketId.toString() + ".json");
        Path filePath = Path.of(file.getPath());

        if (!file.exists()) return;

        String content = Files.readString(filePath);
        JSONArray ticketArray = new JSONArray(content);
        JSONObject ticketObject = ticketArray.getJSONObject(0);

        ticketObject.put(field, value);

        Files.writeString(filePath, ticketArray.toString(4));
    }
    /**
     * Returns true, if any tickets exist
     * @return
     */
    public static boolean eventTicketsExist() {
        File file = new File(getTicketsDir());
        boolean hasTickets = false;
        for (File fileEntry : file.listFiles()) {
            if (fileEntry.isDirectory()) hasTickets = true;
        }
        return hasTickets;
    }

    /**
     * Returns the count of available tickets of an event
     * @param eventName
     * @return
     * @throws IOException
     */
    public static int getAvailableTicketsCount(Object eventName) throws IOException {
        File dirPath = new File(getTicketsDir() + eventName.toString() + "/");
        int count = 0;
        for (File fileEntry : dirPath.listFiles()) {
            if (fileEntry.isFile() && FilenameUtils.getExtension(fileEntry.getName()).equals("json")) {
                String content = Files.readString(fileEntry.toPath());
                JSONArray ticketArray = new JSONArray(content);
                JSONObject ticketObject = ticketArray.getJSONObject(0);

                boolean available = ticketObject.getBoolean("available");

                if (available) count++;
            }
        }
        return count;
    }
    /**
     * Returns the count of non-available tickets of an event
     * @param eventName
     * @return
     * @throws IOException
     */
    public static int getNonAvailableTicketsCount(Object eventName) throws IOException {
        File dirPath = new File(getTicketsDir() + eventName.toString() + "/");
        int count = 0;
        for (File fileEntry : dirPath.listFiles()) {
            if (fileEntry.isFile() && FilenameUtils.getExtension(fileEntry.getName()).equals("json")) {
                String content = Files.readString(fileEntry.toPath());
                JSONArray ticketArray = new JSONArray(content);
                JSONObject ticketObject = ticketArray.getJSONObject(0);

                boolean available = ticketObject.getBoolean("available");

                if (!available) count++;
            }
        }
        return count;
    }
    /**
     * Returns the general ticket-count of an event
     * @param eventName
     * @return
     * @throws IOException
     */
    public static int getTicketsCount(Object eventName) throws IOException {
        File dirPath = new File(getTicketsDir() + eventName.toString() + "/");
        int count = 0;
        for (File fileEntry : dirPath.listFiles()) {
            if (fileEntry.isFile() && FilenameUtils.getExtension(fileEntry.getName()).equals("json")) count++;
        }
        return count;
    }
    /**
     * Returns the count of registered tickets of an event
     * @param eventName
     * @return
     * @throws IOException
     */
    public static int getRegisteredTicketsCount(Object eventName) throws IOException {
        File dirPath = new File(getTicketsDir() + eventName.toString() + "/");
        int count = 0;
        for (File fileEntry : dirPath.listFiles()) {
            if (fileEntry.isFile() && FilenameUtils.getExtension(fileEntry.getName()).equals("json")) {
                String content = Files.readString(fileEntry.toPath());
                JSONArray ticketArray = new JSONArray(content);
                JSONObject ticketObject = ticketArray.getJSONObject(0);

                boolean registered = ticketObject.getBoolean("registered");

                if (registered) count++;
            }
        }
        return count;
    }

    public static List<Object> getNonAvailableTickets(Object dirName) throws IOException {
        File dirPath = new File(getTicketsDir() + dirName.toString() + "/");
        List <Object> tickets = new ArrayList<>();
        for (File fileEntry : dirPath.listFiles()) {
            if (fileEntry.isFile() && FilenameUtils.getExtension(fileEntry.getName()).equals("json")) {
                String content = Files.readString(fileEntry.toPath());
                JSONArray ticketArray = new JSONArray(content);
                JSONObject ticketObject = ticketArray.getJSONObject(0);

                boolean available = ticketObject.getBoolean("available");

                if (!available) tickets.add(FilenameUtils.removeExtension(fileEntry.getName()));
            }
        }
        return tickets;
    }
}