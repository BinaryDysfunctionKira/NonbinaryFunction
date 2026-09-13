package com.binary_dysfunction;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.binary_dysfunction.components.Colors;

public final class ChatPanel extends JPanel {

    public ChatPanel() {

        JPanel chatsPanel = new JPanel();
        chatsPanel.setLayout(new BoxLayout(chatsPanel, BoxLayout.Y_AXIS));
        chatsPanel.setBackground(Colors.backgroundColor);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(Colors.backgorundColorDarker);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        contentPanel.add(chatsPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 50));
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}