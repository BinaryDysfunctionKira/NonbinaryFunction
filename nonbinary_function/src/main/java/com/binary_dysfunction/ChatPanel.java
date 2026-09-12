package com.binary_dysfunction;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public final class ChatPanel extends JPanel {

    public ChatPanel() {

        JPanel mainPanel = new JPanel(new BorderLayout());

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}