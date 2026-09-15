package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.commons.collections4.CollectionUtils;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Component;

public final class ChatPanel extends JPanel {

    JPanel chatsPanel;
    HomeFrame currentFrame;

    public ChatPanel(HomeFrame currentFrame) {
        this.currentFrame = currentFrame;

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
        System.out.println(Main.updater.chatsList.size());

        JLabel myChatsLabel = new JLabel("Meine Chats");
        myChatsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        myChatsLabel.setForeground(Colors.subtleFontColor);
        myChatsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 0));
        chatsPanel.add(myChatsLabel);

        List<Chat> chatterList = Main.updater.chatsList;
        for (Chat chat : chatterList) {
            List<Object> members = chat.members;
            for (Object member : members) {
                if (member.toString() == null ? Main.loggedInAccount.uid == null : member.toString().equals(Main.loggedInAccount.uid)) {
                    String chatName = "";
                    if (!chat.isGroupChat) {
                        for (Object memberHashBrown : chat.members) {
                            if (memberHashBrown.toString() == null ? Main.loggedInAccount.uid != null : !memberHashBrown.toString().equals(Main.loggedInAccount.uid)) {
                                chatName = Main.updater.getAccountByUID(memberHashBrown.toString()).fullName;
                            }
                        }
                    } else chatName = chat.groupName;

                    JButton tmpButton = new JButton(chatName, Component.scaleImage(Main.serverPath + chat.pfpPath, 40));
                    tmpButton.setHorizontalAlignment(SwingConstants.LEFT);
                    tmpButton.setPreferredSize(new Dimension(200, 40));
                    tmpButton.setMinimumSize(new Dimension(200, 40));
                    tmpButton.setMaximumSize(new Dimension(200, 40));
                    tmpButton.setBackground(Colors.backgroundColorLighter);
                    tmpButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    tmpButton.addActionListener(e -> {
                        // try {
                        //     // LOAD CHAT
                        // } catch (IOException e1) {}
                    });
                    

                    JPanel tmpButtonPanel = new JPanel();
                    tmpButtonPanel.setLayout(new BoxLayout(tmpButtonPanel, BoxLayout.Y_AXIS));
                    tmpButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                    tmpButtonPanel.add(tmpButton);

                    chatsPanel.add(tmpButtonPanel);
                }
            }
        }

        JLabel recommendedLabel = new JLabel("Empfohlen");
        recommendedLabel.setFont(new Font("Arial", Font.BOLD, 12));
        recommendedLabel.setForeground(Colors.subtleFontColor);
        recommendedLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 0));
        chatsPanel.add(recommendedLabel);

        for (Account acc : Main.updater.membersList) {

            boolean alreadyExists = false;
            List<Object> compareList = new ArrayList<>();
            compareList.add(Main.loggedInAccount.uid);
            compareList.add(acc.uid);
            
            for (Chat chat : chatterList) {
                List<Object> members = chat.members;
                alreadyExists = CollectionUtils.isEqualCollection(members, compareList);
                if (alreadyExists) {
                    System.out.println("Already exists!");
                    break;
                }
            }

            if ((acc.uid == null ? Main.loggedInAccount.uid != null : !acc.uid.equals(Main.loggedInAccount.uid)) && !alreadyExists) {
                JButton tmpButton = new JButton(acc.fullName, Component.scaleImage(Main.serverPath + acc.profilePicturePath, 40));
                tmpButton.setHorizontalAlignment(SwingConstants.LEFT);
                tmpButton.setPreferredSize(new Dimension(200, 40));
                tmpButton.setMinimumSize(new Dimension(200, 40));
                tmpButton.setMaximumSize(new Dimension(200, 40));
                tmpButton.setBackground(Colors.backgroundColorLighter);
                tmpButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                tmpButton.addActionListener(e -> {
                    try {
                        JSONConfigurations.addChat(Main.loggedInAccount, acc);
                        Main.updater.updateChatList();
                        currentFrame.setChatPanel();
                    } catch (IOException e1) {}
                });

                JPanel tmpButtonPanel = new JPanel();
                tmpButtonPanel.setLayout(new BoxLayout(tmpButtonPanel, BoxLayout.Y_AXIS));
                tmpButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                tmpButtonPanel.add(tmpButton);

                chatsPanel.add(tmpButtonPanel);
            }
        }
    }
}