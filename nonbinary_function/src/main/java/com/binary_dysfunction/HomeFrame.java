package com.binary_dysfunction;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.binary_dysfunction.components.SideBar;
import com.binary_dysfunction.components.TopBar;

public class HomeFrame {

    public JFrame frame;
    public JPanel contentPanel = new JPanel(new BorderLayout());
    public TopBar topBar;

    public void startup() {

        frame = new JFrame("Nonbinary Function - Home");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1013, 608);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        topBar = new TopBar(this);
        SideBar sideBar = new SideBar();

        contentPanel.add(new HomePanel(this));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(sideBar, BorderLayout.WEST);
        mainPanel.add(contentPanel);

        frame.getContentPane().add(mainPanel);
    }

    public void setHomePanel() {
        contentPanel.removeAll();
        contentPanel.add(new HomePanel(this));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setProfilePanel() {
        contentPanel.removeAll();
        contentPanel.add(new ProfilePanel(this));
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
