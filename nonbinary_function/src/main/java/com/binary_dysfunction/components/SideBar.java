package com.binary_dysfunction.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.binary_dysfunction.Config;
import com.binary_dysfunction.HomeFrame;
import com.binary_dysfunction.Main;

public class SideBar extends JPanel {

    public SideBar(HomeFrame currentFrame) {

        JButton notificationsButton = new JButton(Component.geticon("notifications.png"));
        notificationsButton.setToolTipText("Benachrichtigungen");
        notificationsButton.setPreferredSize(new Dimension(40, 40));
        notificationsButton.setBackground(null);
        notificationsButton.setBorder(null);

        JButton assembliesButton = new JButton(Component.geticon("groups.png"));
        assembliesButton.setToolTipText("Assemblies");
        assembliesButton.setPreferredSize(new Dimension(40, 40));
        assembliesButton.setBackground(null);
        assembliesButton.setBorder(null);

        JButton chatButton = new JButton(Component.geticon("chat-bubble.png"));
        chatButton.setToolTipText("Chats");
        chatButton.setPreferredSize(new Dimension(40, 40));
        chatButton.setBackground(null);
        chatButton.setBorder(null);

        JButton calendarButton = new JButton(Component.geticon("calendar.png"));
        calendarButton.setToolTipText("Kalendar");
        calendarButton.setPreferredSize(new Dimension(40, 40));
        calendarButton.setBackground(null);
        calendarButton.setBorder(null);
        calendarButton.addActionListener(e -> {
            currentFrame.setCalendarPanel();
            System.out.println("Calendar-Panel loaded");
        });

        JButton driveButton = new JButton(Component.geticon("drive-folder.png"));
        driveButton.setToolTipText("Öffne Personal-Folder");
        driveButton.setPreferredSize(new Dimension(40, 40));
        driveButton.setBackground(null);
        driveButton.setBorder(null);
        driveButton.addActionListener(e -> {
            if (!Main.loggedInAccount.cloudActivated) {
                JOptionPane.showMessageDialog(null, "Kein Zugriff auf den 'Personal-Vault'. Bitte fragen Sie ihre Administratoren auf Berechtigung an.", "Kein Zugriff auf die Cloud", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Desktop.getDesktop().open(new File(Main.serverPath + Config.ACCOUNTS_DIR + Main.loggedInAccount.username));
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(null, "Kein Zugriff auf den 'Personal-Vault'. Bitte fragen Sie ihre Administratoren auf Berechtigung an.", "Kein Zugriff auf die Cloud", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel topButtons = new JPanel(new GridLayout(0, 1, 0, 10));
        topButtons.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        topButtons.setBackground(new Color(19, 39, 29));
        topButtons.add(notificationsButton);
        topButtons.add(assembliesButton);
        topButtons.add(chatButton);
        topButtons.add(calendarButton);
        topButtons.add(driveButton);


        JButton eventButton = new JButton(Component.geticon("event.png"));
        eventButton.setToolTipText("Event Mode");
        eventButton.setPreferredSize(new Dimension(40, 40));
        eventButton.setBackground(null);
        eventButton.setBorder(null);

        JButton adminButton = new JButton(Component.geticon("admin.png"));
        adminButton.setToolTipText("Admin Zone");
        adminButton.setPreferredSize(new Dimension(40, 40));
        adminButton.addActionListener(e -> {
            currentFrame.setAdminPanel();
            System.out.println("Admin-Panel loaded");
        });

        JPanel bottomButtons = new JPanel(new GridLayout(0, 1, 0, 10));
        bottomButtons.setBackground(new Color(19, 39, 29));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        bottomButtons.add(eventButton);
        if (Main.loggedInAccount.assemblies.contains("Admin") || Main.loggedInAccount.assemblies.contains("Owner")) bottomButtons.add(adminButton);

        JPanel fillPanel = new JPanel();
        fillPanel.setBackground(new Color(19, 39, 29));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(19, 39, 29));
        contentPanel.add(topButtons, BorderLayout.NORTH);
        contentPanel.add(fillPanel);
        contentPanel.add(bottomButtons, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(19, 39, 29));
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}
