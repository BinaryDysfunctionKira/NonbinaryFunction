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
    public boolean cloudActivated;
    public String email;

    public Account(String username, String passwordHash, String fullName, String description, String profilePicturePath, String uid, List<Object> assemblies, boolean cloudActivated, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.description = description;
        this.profilePicturePath = profilePicturePath;
        this.uid = uid;
        this.assemblies = assemblies;
        this.cloudActivated = cloudActivated;
        this.email = email;

        if (this.email.equals("")) this.cloudActivated = false;
    }

    @Override
    public String toString() {
        String output = "\nUsername: " + username + ",\nPasswordHash: " + passwordHash + "\nFull Name: " + fullName + "\nPFP_Path: " + profilePicturePath + ",\nDescription: " + description + ",\nUID: " + uid + ", \nAssemblies: ";
        for (Object assembly : assemblies) {
            output += "\n-'" + assembly.toString() + "'";
        }
        output += "\nCloud Activated: " + cloudActivated + "\nE-Mail: " + email + "\n";
        return output;
    }

    public static void logOut(JFrame currentFrame) {
        Main.loggedInAccount = null;
        currentFrame.dispose();
        SwingUtilities.invokeLater(new PasswordFrame()::startup);
    }
}
