package com.binary_dysfunction;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Account {

    public String username;
    public String passwordHash;
    public String fullName;
    public String description;
    public String profilePicturePath;
    public final String uid;
    public List<Object> assemblies;

    public Account(String username, String passwordHash, String fullName, String description, String profilePicturePath, String uid, List<Object> assemblies) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.description = description;
        this.profilePicturePath = profilePicturePath;
        this.uid = uid;
        this.assemblies = assemblies;
    }

    @Override
    public String toString() {
         return "\nUsername: " + username + ",\nPasswordHash: " + passwordHash + "\nFull Name: " + fullName + ",\nDescription: " + description + ",\nUID: " + uid;
    }

    public static void logOut(JFrame currentFrame) {
        Main.loggedInAccount = null;
        currentFrame.dispose();
        SwingUtilities.invokeLater(new PasswordFrame()::startup);
    }
}
