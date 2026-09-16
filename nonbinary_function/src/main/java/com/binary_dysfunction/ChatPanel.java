package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.collections4.CollectionUtils;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Component;

public class ChatPanel extends JPanel {

    JPanel chatsPanel;
    HomeFrame currentFrame;

    public static Chat currentTargetUser;

    public ChatPanel(HomeFrame currentFrame) {
        this.currentFrame = currentFrame;

        JLabel currentTargetUserPfpLabel = new JLabel();
        JPanel currentTargetUserDetailsPanel = new JPanel();
        if (currentTargetUser != null) {
            String chatPfp = Main.serverPath + currentTargetUser.pfpPath;
            if (!currentTargetUser.isGroupChat) {
                for (Object usr : currentTargetUser.members) {
                    if (!usr.toString().equals(Main.loggedInAccount.uid)) {
                        try {
                            chatPfp = Main.serverPath + Main.updater.getAccountByUID(usr.toString()).profilePicturePath;
                        } catch (Exception e) {}
                    }
                }
            }
            currentTargetUserPfpLabel = new JLabel(Component.scaleImage(chatPfp, 60));
            currentTargetUserPfpLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            String chatName = currentTargetUser.groupName;
            if (!currentTargetUser.isGroupChat) {
                for (Object usr : currentTargetUser.members) {
                    if (!usr.toString().equals(Main.loggedInAccount.uid)) {
                        try {
                            chatName = Main.updater.getAccountByUID(usr.toString()).fullName;
                        } catch (Exception e) {
                            chatName = "User deleted";
                        }
                    }
                }
            }
            JLabel currentTargetUserFullNameLabel = new JLabel(chatName);
            currentTargetUserFullNameLabel.setFont(new Font("Arial", Font.BOLD, 18));

            String chatUsername = currentTargetUser.id;
            if (!currentTargetUser.isGroupChat) {
                for (Object usr : currentTargetUser.members) {
                    if (!usr.toString().equals(Main.loggedInAccount.uid)) {
                        try {
                            chatUsername = Main.updater.getAccountByUID(usr.toString()).username;
                        } catch(Exception e) {}
                    }
                }
            }
            JLabel currentTargetUserUserNameLabel = new JLabel(chatUsername);
            currentTargetUserUserNameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            currentTargetUserUserNameLabel.setForeground(Colors.subtleFontColor);

            currentTargetUserDetailsPanel = new JPanel();
            currentTargetUserDetailsPanel.setLayout(new BoxLayout(currentTargetUserDetailsPanel, BoxLayout.Y_AXIS));
            currentTargetUserDetailsPanel.setBackground(Colors.backgorundColorVeryDark);
            currentTargetUserDetailsPanel.add(currentTargetUserFullNameLabel);
            currentTargetUserDetailsPanel.add(currentTargetUserUserNameLabel);
        }

        JPanel currentTargetInformationPanel = new JPanel();
        currentTargetInformationPanel.setLayout(new BoxLayout(currentTargetInformationPanel, BoxLayout.X_AXIS));
        currentTargetInformationPanel.setBackground(Colors.backgorundColorVeryDark);
        currentTargetInformationPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
        currentTargetInformationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        currentTargetInformationPanel.add(currentTargetUserPfpLabel);
        currentTargetInformationPanel.add(currentTargetUserDetailsPanel);

        JPanel currentTargetUserPanel = new JPanel();
        currentTargetUserPanel.setLayout(new BorderLayout());
        currentTargetUserPanel.setBackground(Colors.backgorundColorVeryDark);
        currentTargetUserPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
        currentTargetUserPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        currentTargetUserPanel.add(currentTargetInformationPanel, BorderLayout.WEST);

        Message message = new Message("5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9", "poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo poo", 11111111, true, "5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9");
        
        JPanel chatContentPanel = new JPanel();
        chatContentPanel.setLayout(new BoxLayout(chatContentPanel, BoxLayout.Y_AXIS));
        chatContentPanel.setBackground(Colors.backgroundColor);
        chatContentPanel.add(message.getJPanel());
        chatContentPanel.add(message.getJPanel());
        chatContentPanel.add(message.getJPanel());
        chatContentPanel.add(message.getJPanel());
        chatContentPanel.add(message.getJPanel());

        JScrollPane chatContentScrollPane = new JScrollPane(chatContentPanel);
        chatContentScrollPane.setHorizontalScrollBar(null);
        chatContentScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        chatContentScrollPane.setBackground(Colors.backgroundColor);

        chatContentPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                JScrollBar bar = chatContentScrollPane.getVerticalScrollBar();
                SwingUtilities.invokeLater(() -> bar.setValue(bar.getMaximum()));
            }
        });

        JPanel currentChatPanel = new JPanel();
        currentChatPanel.setLayout(new BorderLayout());
        currentChatPanel.setBackground(Colors.backgroundColor);
        currentChatPanel.add(currentTargetUserPanel, BorderLayout.NORTH);
        currentChatPanel.add(chatContentScrollPane);

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
                        for (Object memberHash : chat.members) {
                            if (memberHash.toString() == null ? Main.loggedInAccount.uid != null : !memberHash.toString().equals(Main.loggedInAccount.uid)) {
                                try {
                                    chatName = Main.updater.getAccountByUID(memberHash.toString()).fullName;
                                } catch (Exception e) {
                                    chatName = "User deleted";
                                }
                            }
                        }
                    } else chatName = chat.groupName;

                    String pfpPath = Main.serverPath + chat.pfpPath;
                    if(!chat.isGroupChat) {
                        for (Object memberHash : chat.members) {
                            if (!memberHash.equals(Main.loggedInAccount)) {
                                try {
                                    pfpPath = Main.serverPath + Main.updater.getAccountByUID(memberHash.toString()).profilePicturePath;
                                } catch (Exception e) {}
                            }
                        }
                    }

                    JButton tmpButton = new JButton(chatName, Component.scaleImage(pfpPath, 40));
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
                        currentTargetUser = chat;
                        currentFrame.setChatPanel();
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
                    // System.out.println("Already exists!");
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