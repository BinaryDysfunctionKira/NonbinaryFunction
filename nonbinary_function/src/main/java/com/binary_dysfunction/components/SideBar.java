package com.binary_dysfunction.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class SideBar extends JPanel {

    public SideBar() {

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

        JButton driveButton = new JButton(Component.geticon("drive-folder.png"));
        driveButton.setToolTipText("Öffne Drive-Folder");
        driveButton.setPreferredSize(new Dimension(40, 40));
        driveButton.setBackground(null);
        driveButton.setBorder(null);

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

        JPanel bottomButtons = new JPanel(new GridLayout(0, 1, 0, 10));
        bottomButtons.setBackground(new Color(19, 39, 29));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        bottomButtons.add(eventButton);

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
