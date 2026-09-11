package com.binary_dysfunction;

import java.io.File;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

public class Main {

    public static String serverPath = "";
    public static String serverName = "";
    public static boolean isServerNew = false;
    public static boolean ownerSet = false;

    public static final Config config = new Config();

    public static Account loggedInAccount;

    public static ChatPanel chat;

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        FlatMacDarkLaf.setup();
        loadSavedUsrConfig();
        SwingUtilities.invokeLater(new PasswordFrame()::startup);
    }

    private static void loadSavedUsrConfig() {
        try {
            serverPath = config.loadServerPath();
        } catch (Exception e) {
            config.saveServerPath(serverPath);
        }
        File serverDir = new File(serverPath);
        serverName = serverDir.getName();
        System.out.println("User Config loaded.");
    }
}