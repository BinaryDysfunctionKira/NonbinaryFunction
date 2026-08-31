package com.binary_dysfunction.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.binary_dysfunction.Account;
import com.binary_dysfunction.Main;

public class UserCard extends JPanel {

    private final Color backgroundColor = new Color(80, 80, 80);

    public UserCard(JFrame currentFrame) {

        JLabel pfp = new JLabel(Component.scaleImage(Main.loggedInAccount.profilePicturePath, 40));
        pfp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JLabel subtitleLabel = new JLabel("Angemeldet | " + Main.loggedInAccount.username);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(160, 160, 160));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JLabel nameLabel = new JLabel(Main.loggedInAccount.fullName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel informationPanel = new JPanel();
        informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
        informationPanel.setBackground(backgroundColor);
        informationPanel.add(subtitleLabel);
        informationPanel.add(nameLabel);

        JPanel cardTopPanel = new JPanel(new BorderLayout());
        cardTopPanel.setBackground(backgroundColor);
        cardTopPanel.add(pfp, BorderLayout.WEST);
        cardTopPanel.add(informationPanel);


        JButton profileButton = new JButton("Profil");
        profileButton.setBackground(new Color(51, 134, 55));
        profileButton.setPreferredSize(new Dimension(150, 40));

        JPanel profileButtonPanel = new JPanel();
        profileButtonPanel.setBackground(backgroundColor);
        profileButtonPanel.add(profileButton);

        JButton logoutButton = new JButton(Component.geticon("logout.png"));
        logoutButton.setToolTipText("Log out");
        logoutButton.setPreferredSize(new Dimension(40, 40));
        logoutButton.setBackground(new Color(128, 37, 37));
        logoutButton.addActionListener(e -> {
            Account.logOut(currentFrame);
        });

        JPanel logoutButtonPanel = new JPanel();
        logoutButtonPanel.setBackground(backgroundColor);
        logoutButtonPanel.add(logoutButton);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(profileButtonPanel);
        buttonPanel.add(logoutButtonPanel, BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(backgroundColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        contentPanel.add(cardTopPanel);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }

}
