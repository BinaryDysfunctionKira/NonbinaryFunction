package com.binary_dysfunction;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

public class Main {

    public static String serverPath = "";
    public static String serverName = "";
    public static boolean isServerNew = false;
    public static boolean ownerSet = false;

    public static Image appIcon = new ImageIcon("nonbinary_function\\src\\main\\resources\\icon.png").getImage();

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

    public static void launchNewInstance() {
        try {
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String mainClass = "com.binary_dysfunction.Main";

            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-cp");
            command.add(classpath);
            command.add(mainClass);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.inheritIO(); // optional — lets the new instance's System.out show in your console
            builder.start();
        } catch (IOException e) {
            System.getLogger(Main.class.getName()).log(System.Logger.Level.ERROR, "Failed to launch new instance", e);
            JOptionPane.showMessageDialog(null, "Neues Fenster konnte nicht gestartet werden: " + e.getMessage(),
                "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }
}