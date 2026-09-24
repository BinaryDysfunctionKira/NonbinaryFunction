package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Component;
import com.binary_dysfunction.components.UserCard;
import com.binary_dysfunction.config.Config;
import com.binary_dysfunction.frames.HomeFrame;
import com.binary_dysfunction.main.Main;

public class HomePanel extends JPanel {

    public HomePanel(HomeFrame hp) {     

        JLabel headerTitle = new JLabel(Main.serverName);
        headerTitle.setFont(new Font("Arial", Font.BOLD, 32));
        headerTitle.setAlignmentX(CENTER_ALIGNMENT);
        headerTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JLabel headerSubtitle = new JLabel("Guten Tag, " + Main.loggedInAccount.fullName + "!");
        headerSubtitle.setFont(new Font("Arial", Font.BOLD, 16));
        headerSubtitle.setForeground(Colors.lighterFontColor);
        headerSubtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.add(headerTitle);
        headerTextPanel.add(headerSubtitle);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerTextPanel);
        headerPanel.add(new UserCard(hp), BorderLayout.EAST);

        
        JButton announcementButton = new JButton("Benachrichtigungen", Component.geticon("/notifications.png"));
        announcementButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        announcementButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        announcementButton.setHorizontalTextPosition(SwingConstants.CENTER);
        announcementButton.addActionListener(e -> {
            // hp.setNotificationsPanel();
        });

        JButton assembliesButton = new JButton("Benachrichtigungen", Component.geticon("/groups.png"));
        assembliesButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        assembliesButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        assembliesButton.setHorizontalTextPosition(SwingConstants.CENTER);
        assembliesButton.addActionListener(e -> {
            // hp.setAssembliesPanel();
        });

        JButton chatsButton = new JButton("Chats", Component.geticon("/chat-bubble.png"));
        chatsButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        chatsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        chatsButton.setHorizontalTextPosition(SwingConstants.CENTER);
        chatsButton.addActionListener(e -> {
            hp.setChatPanel();
        });

        JButton calendarButton = new JButton("Kalender", Component.geticon("/calendar.png"));
        calendarButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        calendarButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        calendarButton.setHorizontalTextPosition(SwingConstants.CENTER);
        calendarButton.addActionListener(e -> {
            hp.setCalendarPanel();
        });

        JButton driveButton = new JButton("Drive Öffnen", Component.geticon("/drive-folder.png"));
        driveButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        driveButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        driveButton.setHorizontalTextPosition(SwingConstants.CENTER);
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

        JButton eventModeButton = new JButton("Event-Funktionen", Component.geticon("/event.png"));
        eventModeButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        eventModeButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        eventModeButton.setHorizontalTextPosition(SwingConstants.CENTER);
        eventModeButton.setVisible(false);
        if (Main.loggedInAccount.assemblies.contains("Admin") || Main.loggedInAccount.assemblies.contains("Event")) eventModeButton.setVisible(true);
        eventModeButton.addActionListener(e -> {
            hp.setEventPanel();
        });

        JButton adminButton = new JButton("Admin Bereich", Component.geticon("/admin.png"));
        adminButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        adminButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        adminButton.setHorizontalTextPosition(SwingConstants.CENTER);
        adminButton.setVisible(false);
        if (Main.loggedInAccount.assemblies.contains("Admin")) adminButton.setVisible(true);
        adminButton.addActionListener(e -> {
            hp.setAdminPanel();
        });

        JPanel mainContentContentPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        mainContentContentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        // mainContentContentPanel.setLayout(new BoxLayout(mainContentContentPanel, BoxLayout.Y_AXIS));
        // mainContentContentPanel.setBackground(Colors.backgorundColorDarker);
        mainContentContentPanel.add(announcementButton);
        mainContentContentPanel.add(assembliesButton);
        mainContentContentPanel.add(chatsButton);
        mainContentContentPanel.add(calendarButton);
        mainContentContentPanel.add(driveButton);
        mainContentContentPanel.add(eventModeButton);
        mainContentContentPanel.add(adminButton);
        
        // JScrollPane mainContentContentScrollPane = new JScrollPane(mainContentContentPanel);
        // mainContentContentScrollPane.setBorder(null);
        // mainContentContentScrollPane.setBackground(null);
        // mainContentContentScrollPane.setVerticalScrollBar(null);

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainContentPanel.add(mainContentContentPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}
