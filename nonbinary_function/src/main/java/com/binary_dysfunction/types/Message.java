package com.binary_dysfunction.types;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.main.Main;

public class Message {

    public String id;
    public String content;
    public long date;
    public boolean isRead;
    public String senderUID;

    private String dateFormatted = "";

    public Message(String id, String content, long date, boolean isRead, String senderUID) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.isRead = isRead;
        this.senderUID = senderUID;

        LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), TimeZone.getDefault().toZoneId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        dateFormatted = datetime.format(formatter);
    }

    public JPanel getJPanel() {

        String senderName;
        try {
            senderName = Main.updater.getAccountByUID(senderUID).fullName;
        } catch (Exception e) {
            senderName = "User Deleted";
        }
        if (senderUID.equals("Admin")) senderName = "";

        JLabel senderLabel = new JLabel(senderName);
        senderLabel.setFont(new Font("Arial", Font.BOLD, 15));
        if (senderUID.equals(Main.loggedInAccount.uid)) {
            senderLabel.setForeground(Colors.greenButtonColor);
        } else {
            senderLabel.setForeground(Colors.lighterFontColor);
        }

        JLabel dateLabel = new JLabel(dateFormatted);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        dateLabel.setForeground(Colors.subtleFontColor);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(senderLabel);
        infoPanel.add(dateLabel, BorderLayout.EAST);

        JTextPane textPane = new JTextPane() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(490, Integer.MAX_VALUE);
            }
        };
        textPane.setFont(new Font("Arial", Font.PLAIN, 13));
        textPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        textPane.setText(content);
        textPane.setEditable(false); // falls nicht schon anderswo gesetzt

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 8));
        mainPanel.setMinimumSize(new Dimension(500, 30));
        mainPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        mainPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        mainPanel.add(infoPanel);
        mainPanel.add(textPane);

        JPanel superPanel = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        superPanel.setLayout(new BoxLayout(superPanel, BoxLayout.Y_AXIS));
        superPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        superPanel.setBackground(Colors.backgroundColor);
        superPanel.add(mainPanel);

        return superPanel;
    }
}
