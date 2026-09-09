package com.binary_dysfunction.components;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HomeLastChats extends JScrollPane {

    public HomeLastChats() {

        JPanel mainpanel = new JPanel(new BorderLayout());
        mainpanel.setBackground(Colors.backgroundColor);

        this.setViewportView(mainpanel);
        this.setBackground(Colors.backgroundColor);
        this.setBorder(BorderFactory.createTitledBorder("Letzte Chats"));
    }
}
