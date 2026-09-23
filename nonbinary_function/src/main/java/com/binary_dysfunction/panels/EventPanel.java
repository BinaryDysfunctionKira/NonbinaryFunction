package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Component;
import com.binary_dysfunction.config.JSONConfigurations;
import com.binary_dysfunction.frames.HomeFrame;

public class EventPanel extends JPanel {

    public EventPanel(HomeFrame currentFrame) {

        JLabel titleLabel = new JLabel("Event Funktionen");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 36));
        titleLabel.setBackground(Colors.backgroundColor);
        titleLabel.setAlignmentX(0.5f);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));

        JButton addTicketsButton = new JButton("Tickets hinzufügen", Component.geticon("/add.png"));
        addTicketsButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        addTicketsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        addTicketsButton.setHorizontalTextPosition(SwingConstants.CENTER);
        addTicketsButton.addActionListener(e -> {
            currentFrame.setAddTicketPanel();
        });

        JButton configureTicketsButton = new JButton("Tickets bearbeiten", Component.geticon("/settings.png"));
        configureTicketsButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        configureTicketsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        configureTicketsButton.setHorizontalTextPosition(SwingConstants.CENTER);
        if (!JSONConfigurations.eventTicketsExist()) configureTicketsButton.setEnabled(false);
        configureTicketsButton.addActionListener(e -> {
            currentFrame.setConfigureTicketPanel();
        });

        JButton registerTicketsButton = new JButton("Tickets registrieren", Component.geticon("/check.png"));
        registerTicketsButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        registerTicketsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        registerTicketsButton.setHorizontalTextPosition(SwingConstants.CENTER);
        if (!JSONConfigurations.eventTicketsExist()) registerTicketsButton.setEnabled(false);
        registerTicketsButton.addActionListener(e -> {
            currentFrame.setRegisterTicketPanel();
        });

        JButton statisticsButton = new JButton("Statistiken", Component.geticon("/chart.png"));
        statisticsButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 20,15));
        statisticsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        statisticsButton.setHorizontalTextPosition(SwingConstants.CENTER);
        if (!JSONConfigurations.eventTicketsExist()) statisticsButton.setEnabled(false);
        statisticsButton.addActionListener(e -> {
            currentFrame.setStatisticsPanel();
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        buttonsPanel.add(addTicketsButton);
        buttonsPanel.add(configureTicketsButton);
        buttonsPanel.add(registerTicketsButton);
        buttonsPanel.add(statisticsButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(titleLabel);
        contentPanel.add(buttonsPanel);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 10, 100));
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }
}
