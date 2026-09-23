package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.config.JSONConfigurations;
import com.binary_dysfunction.frames.HomeFrame;
import com.binary_dysfunction.types.Ticket;

public class StatisticPanel extends JPanel {

    Ticket currentTicket;
    Object currentEventName;
    List<Ticket> ticketsList;

    public StatisticPanel(HomeFrame currentFrame) {

        JLabel titleLabel = new JLabel("Statistik");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel searchTitleLabel = new JLabel("Suche");
        searchTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        searchTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        searchTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel informationTitleLabel = new JLabel("Informationen");
        informationTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        informationTitleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 8, 0));
        informationTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);


        JComboBox<Object> eventNameSelectBox = new JComboBox<>(JSONConfigurations.getEvents().toArray());
        eventNameSelectBox.setFont(new Font("Arial", Font.PLAIN, 14));
        eventNameSelectBox.setBackground(Colors.backgorundColorDarker);
        if (currentEventName != null) eventNameSelectBox.setSelectedItem(currentEventName);
        eventNameSelectBox.addActionListener(e -> {
            currentEventName = eventNameSelectBox.getSelectedItem();
        });
        currentEventName = eventNameSelectBox.getSelectedItem();

        JPanel eventNameSearchTextFieldPanel = new JPanel(new BorderLayout());
        eventNameSearchTextFieldPanel.setBackground(null);
        eventNameSearchTextFieldPanel.setMaximumSize(new Dimension(200, 50));
        eventNameSearchTextFieldPanel.add(new JLabel("Event Name"), BorderLayout.NORTH);
        eventNameSearchTextFieldPanel.add(eventNameSelectBox);

        try {
            ticketsList = JSONConfigurations.getTicketsAsList(currentEventName);
        } catch (IOException ex) {}

        JLabel eventNameLabel = new JLabel();

        JPanel informationPanel = new JPanel();
        informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
        informationPanel.setBackground(null);
        informationPanel.add(eventNameLabel);

        JPanel rowOne = new JPanel();
        rowOne.setLayout(new BoxLayout(rowOne, BoxLayout.X_AXIS));
        rowOne.setBackground(null);
        rowOne.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowOne.add(eventNameSearchTextFieldPanel);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Colors.backgroundColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 55, 35));
        settingsPanel.add(titleLabel);
        settingsPanel.add(searchTitleLabel);
        settingsPanel.add(rowOne);
        settingsPanel.add(informationTitleLabel);


        JScrollPane settingsScrollPane = new JScrollPane(settingsPanel);
        settingsScrollPane.setHorizontalScrollBar(null);
        settingsScrollPane.getVerticalScrollBar().setUnitIncrement(8);

        JPanel settingsPanelOuter = new JPanel(new BorderLayout());
        settingsPanelOuter.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
        settingsPanelOuter.add(settingsScrollPane);

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
