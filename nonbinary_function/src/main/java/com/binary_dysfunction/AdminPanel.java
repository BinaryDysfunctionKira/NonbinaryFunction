package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminPanel extends JPanel {

    private final Color backgroundColor = new Color(40, 40, 40);
    private List<Account> registeredAccounts = new ArrayList<>();

    private Account currentAccount = Main.loggedInAccount;

    public AdminPanel() {

        

        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setBackground(backgroundColor);

        try {
            updateAccountList(userListPanel);
        } catch (IOException ex) {
            System.getLogger(AdminPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        JScrollPane userListScrollPane = new JScrollPane(userListPanel);
        userListScrollPane.setBackground(backgroundColor);

        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBackground(backgroundColor);
        usersPanel.add(userListScrollPane, BorderLayout.CENTER);

        JLabel pageTitle = new JLabel("Admin Zone");
        pageTitle.setAlignmentX(LEFT_ALIGNMENT);
        pageTitle.setFont(new Font("Arial", Font.BOLD, 30));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(backgroundColor);
        contentPanel.add(pageTitle);
        contentPanel.add(usersPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }

    private void updateAccountList(JPanel target) throws IOException {

        String content = Files.readString(JSONConfigurations.ACCOUNT_PATH);
        JSONArray accounts = new JSONArray(content);

        System.out.println("Registered accounts:");
        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc =  accounts.getJSONObject(i);

            String username = acc.getString("username");
            String passwordHash = acc.getString("passwordHash");
            String fullName = acc.getString("fullName");
            String description = acc.getString("description");
            String profilePicturePath = acc.getString("profilePicturePath");
            String uid = acc.getString("uid");
            List<Object> assemblies = acc.getJSONArray("assemblies").toList();

            registeredAccounts.add(new Account(username, passwordHash, fullName, description, profilePicturePath, uid, assemblies));
            System.out.println("-" + username);
        }

        for (Account acc : registeredAccounts) {
            JButton userButton = new JButton(acc.username);
            userButton.addActionListener(e -> {
                for (Account account : registeredAccounts) {
                    if (account.equals(acc)) {
                        currentAccount = account;
                        System.out.println(currentAccount.username);
                    }
                }
            });
            target.add(userButton);
        }
    }
}
