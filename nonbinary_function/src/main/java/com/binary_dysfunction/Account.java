package com.binary_dysfunction;

public class Account {

    public String username;
    public String passwordHash;
    public String description;
    public String profilePicturePath;
    public final String uid;

    public Account(String username, String passwordHash, String description, String profilePicturePath, String uid) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.description = description;
        this.profilePicturePath = profilePicturePath;
        this.uid = uid;
    }

    @Override
    public String toString() {
         return "\nUsername: " + username + ",\nPasswordHash: " + passwordHash + ",\nDescription: " + description + ",\nUID: " + uid;
    }
}
