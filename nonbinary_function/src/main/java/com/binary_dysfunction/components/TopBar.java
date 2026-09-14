package com.binary_dysfunction.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.binary_dysfunction.Account;
import com.binary_dysfunction.HomeFrame;
import com.binary_dysfunction.Main;

public class TopBar extends JPanel {

    public JButton accountButton;

    public TopBar(HomeFrame currentFrame) {

        JButton homeButton = new JButton(Component.geticon("home.png"));
        homeButton.setPreferredSize(new Dimension(50, 50));
        homeButton.setToolTipText("Home");
        homeButton.setBackground(null);
        homeButton.setBorder(null);
        homeButton.setOpaque(true);
        homeButton.addActionListener(e -> {
            currentFrame.setHomePanel();
            System.out.println("Home-Panel loaded");
        });

        accountButton = new JButton(Main.loggedInAccount.username, Component.scaleImage(Main.serverPath + Main.loggedInAccount.profilePicturePath, 40));
        accountButton.setToolTipText("Account Details");
        accountButton.setFont(new Font("Arial", Font.PLAIN, 13));
        accountButton.setBackground(Colors.backgorundColorDarker);
        accountButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 35));
        accountButton.addActionListener(e -> {
            currentFrame.setProfilePanel();
            System.out.println("Profile-Panel loaded");
        });
        

        JButton logoutButton = new JButton(Component.geticon("logout.png"));
        logoutButton.setToolTipText("Log out");
        logoutButton.setPreferredSize(new Dimension(40, 40));
        logoutButton.setBackground(Colors.redButtonColor);
        logoutButton.addActionListener(e -> {
            Account.logOut(HomeFrame.frame);
            Main.updater.stopUpdating();
        });

        JPanel accountPanel = new JPanel(new BorderLayout());
        accountPanel.setBackground(Colors.backgorundColorDarker);
        accountPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        accountPanel.add(accountButton, BorderLayout.WEST);
        accountPanel.add(logoutButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Colors.backgorundColorVeryDark);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPanel.add(homeButton, BorderLayout.WEST);
        contentPanel.add(accountPanel, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.backgorundColorVeryDark);
        mainPanel.add(contentPanel);
        

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }

}
