package com.binary_dysfunction;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ChatPanel extends JScrollPane {

    public ChatPanel() {

        JPanel mainPanel = new JPanel(new BorderLayout());

        this.setViewportView(mainPanel);
    }
}
