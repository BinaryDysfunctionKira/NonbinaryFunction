package com.binary_dysfunction;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.binary_dysfunction.components.SideBar;
import com.binary_dysfunction.components.TopBar;

public class HomeFrame {

    public static JFrame frame;
    public static JPanel contentPanel;

    public void startup() {

        frame = new JFrame("Nonbinary Function - Home");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1013, 608);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        TopBar topBar = new TopBar(frame);
        SideBar sideBar = new SideBar();

        contentPanel = new HomePanel(frame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(contentPanel);
        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(sideBar, BorderLayout.WEST);
        mainPanel.add(contentPanel);

        frame.getContentPane().add(mainPanel);
    }
}
