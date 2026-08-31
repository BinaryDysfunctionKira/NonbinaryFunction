package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ProfilePanel extends JPanel {

    public ProfilePanel() {

        JLabel headerTitle = new JLabel("Profil");
        headerTitle.setFont(new Font("Arial", Font.BOLD, 32));
        headerTitle.setAlignmentX(CENTER_ALIGNMENT);
        JLabel headerSubtitle = new JLabel("Profilinformationen ändern lassen");
        headerSubtitle.setFont(new Font("Arial", Font.BOLD, 16));
        headerSubtitle.setForeground(new Color(180, 180, 180));
        headerSubtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.add(headerTitle);
        headerTextPanel.add(headerSubtitle);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(headerTextPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}
