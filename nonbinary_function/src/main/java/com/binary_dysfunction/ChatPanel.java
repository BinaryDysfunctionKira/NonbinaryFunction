package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.collections4.CollectionUtils;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Component;

public final class ChatPanel extends JPanel {

    // px away from the bottom that still counts as "at the bottom"
    private static final int BOTTOM_TOLERANCE = 30;

    // background of a chat button that has unread messages
    private static final Color UNREAD_COLOR = new Color(46, 125, 60);

    // the ChatPanel currently on screen, so Updater can ask it to redraw
    // the sidebar without having to rebuild the whole panel
    private static ChatPanel activeInstance;

    // True only while a ChatPanel instance is actually attached to the
    // visible frame. addNotify()/removeNotify() are called automatically by
    // Swing whenever a component is added to / removed from a realized
    // container - which is exactly what HomeFrame's contentPanel.removeAll()
    // + contentPanel.add(...) does when switching screens. This is what lets
    // Updater tell "chat panel is on screen right now" apart from
    // "currentTargetUser still remembers the last chat we looked at", so
    // toasts for that chat resume as soon as you navigate away, instead of
    // being suppressed forever.
    private static volatile boolean panelVisible = false;

    public static boolean isPanelVisible() {
        return panelVisible;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        panelVisible = true;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        panelVisible = false;
    }

    JPanel chatsPanel;
    HomeFrame currentFrame;

    static JPanel chatContentPanel = new JPanel();
    static JScrollPane chatContentScrollPane;

    // message id -> its component, so we only add what's actually new
    private static final Map<String, java.awt.Component> renderedMessages = new LinkedHashMap<>();

    public static volatile Chat currentTargetUser;

    public ChatPanel(HomeFrame currentFrame) {
        this.currentFrame = currentFrame;
        activeInstance = this;

        // UI Constructor
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
                        } catch (Exception e) {}
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

        chatContentPanel = new JPanel();
        chatContentPanel.setLayout(new BoxLayout(chatContentPanel, BoxLayout.Y_AXIS));
        chatContentPanel.setBackground(Colors.backgroundColor);
        renderedMessages.clear(); // new panel, cached components are gone

        chatContentScrollPane = new JScrollPane(chatContentPanel);
        chatContentScrollPane.setHorizontalScrollBar(null);
        chatContentScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        chatContentScrollPane.setBackground(Colors.backgroundColor);

        // no resize listener here anymore, scrolling down is handled in loadMessages()

        // load chat
        loadMessages();

        JTextField chatTextField = new JTextField();
        chatTextField.setSize(Integer.MAX_VALUE, 50);
        chatTextField.setFont(new Font("Arial", Font.PLAIN, 15));
        if (currentTargetUser == null) chatTextField.setEnabled(false);
        chatTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        char[] chatMessageTmp = chatTextField.getText().toCharArray();
                        boolean chatIsEmpty = true;
                        for (char c : chatMessageTmp) {
                            if (c != ' ' && c != '\n') chatIsEmpty = false;
                        }
                        if (!chatIsEmpty) JSONConfigurations.addMessage(currentTargetUser.id, chatTextField.getText(), Main.loggedInAccount.uid);
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                    chatTextField.setText("");
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    chatTextField.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }

        });

        JButton chatSelectFileButton = new JButton(Component.scaleImage("nonbinary_function\\src\\main\\resources\\drive-folder.png", 30));
        chatSelectFileButton.setFocusable(false);
        if (currentTargetUser == null) chatSelectFileButton.setEnabled(false);

        JPanel chatBarPanel = new JPanel(new BorderLayout());
        chatBarPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        chatBarPanel.setBackground(Colors.backgorundColorVeryDark);
        chatBarPanel.add(chatTextField);
        chatBarPanel.add(chatSelectFileButton, BorderLayout.EAST);

        JPanel currentChatPanel = new JPanel();
        currentChatPanel.setLayout(new BorderLayout());
        currentChatPanel.setBackground(Colors.backgroundColor);
        currentChatPanel.add(currentTargetUserPanel, BorderLayout.NORTH);
        currentChatPanel.add(chatContentScrollPane);
        currentChatPanel.add(chatBarPanel, BorderLayout.SOUTH);

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

        JLabel myChatsLabel = new JLabel("Meine Chats");
        myChatsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        myChatsLabel.setForeground(Colors.subtleFontColor);
        myChatsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 0));
        chatsPanel.add(myChatsLabel);

        // create buttons for existing chats
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
                    if (!chat.isGroupChat) {
                        for (Object memberHash : chat.members) {
                            if (!memberHash.equals(Main.loggedInAccount)) {
                                try {
                                    pfpPath = Main.serverPath + Main.updater.getAccountByUID(memberHash.toString()).profilePicturePath;
                                } catch (Exception e) {}
                            }
                        }
                    }

                    // create button for existing chats
                    boolean unread = Main.updater.isChatUnread(chat.id);

                    JButton tmpButton = new JButton(chatName, Component.scaleImage(pfpPath, 40));
                    tmpButton.setHorizontalAlignment(SwingConstants.LEFT);
                    tmpButton.setPreferredSize(new Dimension(200, 40));
                    tmpButton.setMinimumSize(new Dimension(200, 40));
                    tmpButton.setMaximumSize(new Dimension(200, 40));
                    tmpButton.setBackground(unread ? UNREAD_COLOR : Colors.backgroundColorLighter);
                    tmpButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    tmpButton.addActionListener(e -> {
                        // load chat
                        currentTargetUser = chat;
                        resetMessageCache();
                        try {
                            // also marks the chat as read for this user
                            Main.updater.updateMessages();
                        } catch (IOException ex) {}
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

        // creates buttons for recommended chats
        for (Account acc : Main.updater.membersList) {

            boolean alreadyExists = false;
            List<Object> compareList = new ArrayList<>();
            compareList.add(Main.loggedInAccount.uid);
            compareList.add(acc.uid);

            for (Chat chat : chatterList) {
                List<Object> members = chat.members;
                alreadyExists = CollectionUtils.isEqualCollection(members, compareList);
                if (alreadyExists) {
                    break;
                }
            }

            if ((acc.uid == null ? Main.loggedInAccount.uid != null : !acc.uid.equals(Main.loggedInAccount.uid)) && !alreadyExists) {
                // create button
                JButton tmpButton = new JButton(acc.fullName, Component.scaleImage(Main.serverPath + acc.profilePicturePath, 40));
                tmpButton.setHorizontalAlignment(SwingConstants.LEFT);
                tmpButton.setPreferredSize(new Dimension(200, 40));
                tmpButton.setMinimumSize(new Dimension(200, 40));
                tmpButton.setMaximumSize(new Dimension(200, 40));
                tmpButton.setBackground(Colors.backgroundColorLighter);
                tmpButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                tmpButton.addActionListener(e -> {
                    try {
                        // save new chat
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

        chatsPanel.revalidate();
        chatsPanel.repaint();
    }

    // lets Updater redraw the sidebar (e.g. unread colors) without rebuilding the whole panel
    public static void refreshChatList() {
        ChatPanel instance = activeInstance;
        if (instance != null) instance.updateChatList();
    }

    // call this when the displayed chat changes
    public static void resetMessageCache() {
        renderedMessages.clear();
        chatContentPanel.removeAll();
        chatContentPanel.revalidate();
        chatContentPanel.repaint();
    }

    public static void loadMessages() {
        if (chatContentScrollPane == null) return;

        if (currentTargetUser == null) {
            if (!renderedMessages.isEmpty()) resetMessageCache();
            return;
        }

        JScrollBar bar = chatContentScrollPane.getVerticalScrollBar();
        boolean firstLoad = renderedMessages.isEmpty();
        boolean wasAtBottom = firstLoad
                || bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - BOTTOM_TOLERANCE;

        List<Message> messages = Main.updater.currentChatsMessages;

        Set<String> wantedIds = new HashSet<>();
        for (Message msg : messages) {
            wantedIds.add(msg.id);
        }

        boolean changed = false;

        // throw out messages that don't exist anymore
        Iterator<Map.Entry<String, java.awt.Component>> it = renderedMessages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, java.awt.Component> entry = it.next();
            if (!wantedIds.contains(entry.getKey())) {
                chatContentPanel.remove(entry.getValue());
                it.remove();
                changed = true;
            }
        }

        // append everything we haven't drawn yet
        for (Message msg : messages) {
            if (!renderedMessages.containsKey(msg.id)) {
                java.awt.Component msgComponent = msg.getJPanel();
                renderedMessages.put(msg.id, msgComponent);
                chatContentPanel.add(msgComponent);
                changed = true;
            }
        }

        if (!changed) return;

        chatContentPanel.revalidate();
        chatContentPanel.repaint();

        if (wasAtBottom) {
            // getMaximum() is only correct after the layout pass
            SwingUtilities.invokeLater(() -> {
                JScrollBar b = chatContentScrollPane.getVerticalScrollBar();
                b.setValue(b.getMaximum());
            });
        }
    }
}