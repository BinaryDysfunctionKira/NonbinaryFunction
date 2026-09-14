package com.binary_dysfunction;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

public final class Updater {

    // private final AtomicInteger count = new AtomicInteger(0);

    private Thread updateThread;
    private volatile boolean running = false;
    private volatile int counter = 0;
    public volatile List<Account> members = new ArrayList<>();

    public Updater() {
        startUpdating();
    }

    public void startUpdating() {
        if (updateThread != null && updateThread.isAlive()) {
            return;
        }

        try {
            updateAccountList();
        } catch (IOException e) {}

        running = true;

        updateThread = new Thread(() -> {
            while (running) {

                SwingUtilities.invokeLater(() -> {
                    // System.out.println("Update"+counter);
                    if (counter % 20 == 0) {
                        // int randomNum = new Random().nextInt(0, members.size());
                        // Account randomAcc = members.get(randomNum);
                        // Toast.show(null, Main.serverPath + randomAcc.profilePicturePath, randomAcc.fullName, "Meow!", 5000, Toast.Position.BOTTOM_RIGHT, true);
                    }
                    try {
                        updateAccountList();
                    } catch (IOException e) {}
                    counter++;
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "updater");

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void stopUpdating() {
        running = false;
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    public void updateAccountList() throws IOException {

        String content = Files.readString(JSONConfigurations.ACCOUNT_PATH);
        JSONArray accounts = new JSONArray(content);

        members.clear();

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc =  accounts.getJSONObject(i);

            String username = acc.getString("username");
            String passwordHash = acc.getString("passwordHash");
            String fullName = acc.getString("fullName");
            String description = acc.getString("description");
            String profilePicturePath = acc.getString("profilePicturePath");
            String uid = acc.getString("uid");
            List<Object> assemblies = acc.getJSONArray("assemblies").toList();
            boolean cloudActivated = acc.getBoolean("cloudActivated");
            String email = acc.getString("email");

            // System.out.println(username);
            members.add(new Account(username, passwordHash, fullName, description, profilePicturePath, uid, assemblies, cloudActivated, email));
        }
        // System.out.println("Account-List updated");
    }
}