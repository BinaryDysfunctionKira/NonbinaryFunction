package com.binary_dysfunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONConfigurations {

    public final static Path ACCOUNT_PATH = Paths.get(Main.serverPath + "/users/accounts.json");
    public final static Path CHATS_PATH = Paths.get(Main.serverPath + "/chats/chats.json");

    public static void addAccount(String username, String passwordHash) throws IOException {

        if (ACCOUNT_PATH.getParent() != null) {
            Files.createDirectories(ACCOUNT_PATH.getParent());
        }

        JSONArray accounts;
        if (Files.exists(ACCOUNT_PATH)) {
            String content = Files.readString(ACCOUNT_PATH);
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

        Files.writeString(ACCOUNT_PATH, accounts.toString(4));

        File privateVault = new File(Main.serverPath + Config.ACCOUNTS_DIR + username + "/user-data/");
        privateVault.mkdirs();
        // Path path = Paths.get(Main.serverPath + "/users/." + username);
        // Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
        // Path path2 = Paths.get(privateVault.getPath());
        // Files.setAttribute(path2, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
        

        if (Main.isServerNew && !Main.ownerSet) {
            Main.config.saveServerOwner(uid);
            Main.ownerSet = true;
        }
    }

    public static void updateAccountField(String username, String fieldName, Object newValue) throws IOException {

        if (!Files.exists(ACCOUNT_PATH)) {
            return; // nothing to update
        }

        String content = Files.readString(ACCOUNT_PATH);
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
        // for (int i = 0; i < accounts.length(); i++) {
        //     JSONObject acc = accounts.getJSONObject(i);
        //     if (acc.getString("uid").equals(username)) {
        //         acc.put(fieldName, newValue); // overwrites the existing key, or adds it if missing
        //         found = true;
        //         break; // stop once found, assuming usernames are unique
        //     }
        // }

        if (!found) {
            System.out.println("Account not found: " + username);
            return;
        }

        Files.writeString(ACCOUNT_PATH, accounts.toString(4));
    }
    public static Object getAccountField(String username, String fieldName) throws IOException {

        if (!Files.exists(ACCOUNT_PATH)) {
            return null; // nothing to return
        }

        String content = Files.readString(ACCOUNT_PATH);
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

        if (!Files.exists(ACCOUNT_PATH)) {
            return null; // nothing to return
        }

        String content = Files.readString(ACCOUNT_PATH);
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

        if (ACCOUNT_PATH.getParent() != null) {
            Files.createDirectories(ACCOUNT_PATH.getParent());
        }

        JSONArray accounts;
        if (Files.exists(ACCOUNT_PATH)) {
            String content = Files.readString(ACCOUNT_PATH);
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

        if (!Files.exists(ACCOUNT_PATH)) {
            return;
        }

        String content = Files.readString(ACCOUNT_PATH);
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

        Files.writeString(ACCOUNT_PATH, updatedAccounts.toString(4));

        FileUtils.deleteDirectory(new File(Main.serverPath + Config.ACCOUNTS_DIR + username));
    }

    public static void addChat(Account... member) throws IOException {

        if (CHATS_PATH.getParent() != null) {
            Files.createDirectories(CHATS_PATH.getParent());
        }

        JSONArray chats;
        if (Files.exists(CHATS_PATH)) {
            String content = Files.readString(CHATS_PATH);
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

        Files.writeString(CHATS_PATH, chats.toString(4));

        for (Account mbr : member) {
            Object rawChats = getAccountField(mbr.username, "chats");
            JSONArray oldChats = (rawChats instanceof JSONArray) ? (JSONArray) rawChats : new JSONArray();
            oldChats.put(id);
            updateAccountField(mbr.username, "chats", oldChats);
        }
    }
}
