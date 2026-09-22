package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.frames.HomeFrame;

public class ConfigureTicketPanel extends JPanel {

    public ConfigureTicketPanel(HomeFrame currentFrame) {

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Colors.backgroundColor);

        JPanel settingsPanelOuter = new JPanel(new BorderLayout());
        settingsPanelOuter.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
        settingsPanelOuter.add(settingsPanel);

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.X_AXIS));
        mainContentPanel.add(settingsPanelOuter);

        JButton backButton = new JButton("Zurück");
        backButton.addActionListener(e -> {
            currentFrame.setEventPanel();
        });

        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        backButtonPanel.add(backButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(backButtonPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 10, 10));
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}
