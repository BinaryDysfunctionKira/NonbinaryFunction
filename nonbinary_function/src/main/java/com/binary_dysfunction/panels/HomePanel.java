package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.UserCard;
import com.binary_dysfunction.frames.HomeFrame;
import com.binary_dysfunction.main.Main;

public class HomePanel extends JPanel {

    public HomePanel(HomeFrame hp) {

        JLabel headerTitle = new JLabel(Main.serverName);
        headerTitle.setFont(new Font("Arial", Font.BOLD, 32));
        headerTitle.setAlignmentX(CENTER_ALIGNMENT);
        headerTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JLabel headerSubtitle = new JLabel("Guten Tag, " + Main.loggedInAccount.fullName + "!");
        headerSubtitle.setFont(new Font("Arial", Font.BOLD, 16));
        headerSubtitle.setForeground(Colors.lighterFontColor);
        headerSubtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.add(headerTitle);
        headerTextPanel.add(headerSubtitle);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerTextPanel);
        headerPanel.add(new UserCard(hp), BorderLayout.EAST);

        
        

        JPanel mainContentContentPanel = new JPanel();
        mainContentContentPanel.setLayout(new BoxLayout(mainContentContentPanel, BoxLayout.Y_AXIS));
        // mainContentContentPanel.setBackground(Colors.backgorundColorDarker);
        
        JScrollPane mainContentContentScrollPane = new JScrollPane(mainContentContentPanel);
        mainContentContentScrollPane.setBorder(null);
        mainContentContentScrollPane.setBackground(null);

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainContentPanel.add(mainContentContentScrollPane);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}
