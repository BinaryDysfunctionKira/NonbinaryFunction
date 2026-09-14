package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Component;

public final class ChatPanel extends JPanel {

    JPanel chatsPanel;

    public ChatPanel() {

        JPanel currentTargetUserPanel = new JPanel();
        currentTargetUserPanel.setLayout(new BorderLayout());
        currentTargetUserPanel.setBackground(Colors.backgorundColorVeryDark);
        currentTargetUserPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
        currentTargetUserPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel currentChatPanel = new JPanel();
        currentChatPanel.setLayout(new BorderLayout());
        currentChatPanel.setBackground(Colors.backgroundColor);
        currentChatPanel.add(currentTargetUserPanel, BorderLayout.NORTH);


        JLabel chatsLabel = new JLabel("Chats", JLabel.LEFT);
        chatsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        chatsLabel.setForeground(Colors.subtleFontColor);
        chatsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 0));

        JPanel chatsLabelPanel = new JPanel();
        chatsLabelPanel.setBackground(Colors.backgroundColor);
        chatsLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        chatsLabelPanel.add(chatsLabel);

        chatsPanel = new JPanel();
        chatsPanel.setLayout(new BoxLayout(chatsPanel, BoxLayout.Y_AXIS));
        chatsPanel.setBackground(Colors.backgroundColor);
        chatsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane chatsScrollPane = new JScrollPane(chatsPanel);
        chatsScrollPane.setHorizontalScrollBar(null);

        JPanel allChatsPanel = new JPanel();
        allChatsPanel.setLayout(new BoxLayout(allChatsPanel, BoxLayout.Y_AXIS));
        allChatsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        allChatsPanel.add(chatsLabelPanel);
        allChatsPanel.add(chatsScrollPane);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(Colors.backgorundColorDarker);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(allChatsPanel, BorderLayout.WEST);
        contentPanel.add(currentChatPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 50));
        mainPanel.add(contentPanel);

        updateChatList();

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }

    private void updateChatList() {

        chatsPanel.removeAll();

        for (Account acc : Main.updater.members) {
            
            JButton tmpButton = new JButton(acc.fullName, Component.scaleImage(Main.serverPath + acc.profilePicturePath, 40));
            tmpButton.setHorizontalAlignment(SwingConstants.LEFT);
            tmpButton.setPreferredSize(new Dimension(200, 40));
            tmpButton.setMinimumSize(new Dimension(200, 40));
            tmpButton.setMaximumSize(new Dimension(200, 40));
            tmpButton.setBackground(Colors.backgroundColorLighter);
            tmpButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            

            JPanel tmpButtonPanel = new JPanel();
            tmpButtonPanel.setLayout(new BoxLayout(tmpButtonPanel, BoxLayout.Y_AXIS));
            tmpButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            tmpButtonPanel.add(tmpButton);

            chatsPanel.add(tmpButtonPanel);
        }
    }
}