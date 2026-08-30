package com.binary_dysfunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONConfigurations {

    public final static Path ACCOUNT_PATH = Paths.get(Main.serverPath + "/users/accounts.json");

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

        accounts.put(newAccount);

        Files.writeString(ACCOUNT_PATH, accounts.toString(4));

        File privateVault = new File(Main.serverPath + "/users/" + username);
        privateVault.mkdirs();

        if (Main.isServerNew && !Main.ownerSet) {
            Main.config.saveServerOwner(uid);
            Main.ownerSet = true;
        }
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
                return new Account(username, passwordHash, fullName, description, profilePicturePath, uid);
            }
        }

        System.out.println("Account not found.");
        return null;
    }
}
