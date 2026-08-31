package com.binary_dysfunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class Config {

    private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".nonbinary_function");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "config.properties");

    private static final String KEY_SERVER_DIR = "serverDir";

    private final Properties usrProperties = new Properties();


    private File getServerConfigFile() {
        return new File(Main.serverPath, "server-config.properties");
    }

    private static final String SERVER_OWNER = "Admin";

    private final Properties serverProperties = new Properties();

    public Config() {
        loadUser();
        loadServerProperties();
    }

    private void loadUser() {
        if (!CONFIG_FILE.exists()) return;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), "UTF-8")) {
            usrProperties.load(reader);
        } catch (IOException e) {
        }
    }
    private void saveUsr() {
        try {
            if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), "UTF-8")) {
                usrProperties.store(writer, "User Configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public void saveUsrList(String prefix, List<String> values) {
        int oldCount = Integer.parseInt(usrProperties.getProperty(prefix + ".count", "0"));
        for (int i = 0; i < oldCount; i++) {
            usrProperties.remove(prefix + "." + i);
        }
        usrProperties.setProperty(prefix + ".count", String.valueOf(values.size()));
        for (int i = 0; i < values.size(); i++) {
            usrProperties.setProperty(prefix + "." + i, values.get(i));
        }
        saveUsr();
    }

    public void saveServerPath(String path) {
        if (path == null) {
            return;
        } else {
            usrProperties.setProperty(KEY_SERVER_DIR, path);
        }
        saveUsr();
    }
    public String loadServerPath() {
        return usrProperties.getProperty(KEY_SERVER_DIR);
    }

    private void loadServerProperties() {
        if (!getServerConfigFile().exists()) return;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(getServerConfigFile()), "UTF-8")) {
            serverProperties.load(reader);
        } catch (IOException e) {
        }
    }
    private void saveServerProperties() {
        try {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(getServerConfigFile()), "UTF-8")) {
                serverProperties.store(writer, "Server Configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
    public void saveServerOwner(String uid) {
        if (uid == null) {
            return;
        } else {
            serverProperties.setProperty(SERVER_OWNER, uid);
        }
        saveServerProperties();
    }

    public static void setupServer(File dir) throws IOException {

        deleteFolderContent(dir);

        File assemblies = new File(dir.getPath() + "/assemblies");
        assemblies.mkdirs();
        File chats = new File(dir.getPath() + "/chats");
        chats.mkdirs();
        File announcements = new File(dir.getPath() + "/announcements");
        announcements.mkdirs();
        File concerts = new File(dir.getPath() + "/concerts");
        concerts.mkdirs();
        File users = new File(dir.getPath() + "/users");
        users.mkdirs();
    }
    public static void deleteFolderContent(File dir) throws IOException {
        FileUtils.cleanDirectory(dir);
        System.out.println("Files deleted in: " + dir.toPath());
    }

    public static void createUserConfigFile() {

    }

    public static boolean hasServerConfig(final File dir) {
        if (!Main.serverPath.equals("")) {
            String[] files = dir.list();
            for (String file : files) {
                if (file.equals("server-config.properties")) return true;
            }
            return false;
        }
        return false;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
