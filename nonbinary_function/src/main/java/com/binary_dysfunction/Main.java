package com.binary_dysfunction;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

public class Main {

    public static String serverPath = "";
    public static String serverName = "";
    public static boolean isServerNew = false;
    public static boolean ownerSet = false;

    public static Image appIcon = new ImageIcon("nonbinary_function\\src\\main\\resources\\BinaryDysfunctionLogo.png").getImage();

    public static final Config config = new Config();

    public static Account loggedInAccount;

    public static Updater updater;

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        FlatMacDarkLaf.setup();
        try {
            loadSavedUsrConfig();
        } catch (Exception e) {
            config.saveServerPath("");
        }
        
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