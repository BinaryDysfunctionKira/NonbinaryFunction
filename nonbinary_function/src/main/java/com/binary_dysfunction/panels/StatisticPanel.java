package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        currentEventName = eventNameSelectBox.getSelectedItem();

        JPanel eventNameSearchTextFieldPanel = new JPanel(new BorderLayout());
        eventNameSearchTextFieldPanel.setBackground(null);
        eventNameSearchTextFieldPanel.setMaximumSize(new Dimension(200, 50));
        eventNameSearchTextFieldPanel.add(new JLabel("Event Name"), BorderLayout.NORTH);
        eventNameSearchTextFieldPanel.add(eventNameSelectBox);

        try {
            ticketsList = JSONConfigurations.getTicketsAsList(currentEventName);
            currentTicket = ticketsList.get(0);
        } catch (IOException ex) {}

        JLabel eventNameLabel = new JLabel("Event Name: " + currentEventName);
        eventNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        eventNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        JLabel dateLabel = new JLabel("Date: " + dateFormat.format(new Date(currentTicket.date)));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel locationLabel = new JLabel("Ort: " + currentTicket.location);
        locationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        locationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel ticketCountLabel = new JLabel();
        try {
            ticketCountLabel.setText("Ticket-Anzahl: " + JSONConfigurations.getTicketsCount(currentEventName));
        } catch (IOException ex) {}
        ticketCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel priceLabel = new JLabel("Preis: " + currentTicket.price + "€");
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel nonavailableTicketsLabel = new JLabel();
        try {
            nonavailableTicketsLabel.setText("Verkaufte Tickets: " + JSONConfigurations.getNonAvailableTicketsCount(currentEventName));
        } catch (IOException ex) {}
        nonavailableTicketsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nonavailableTicketsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel registeredTicketsLabel = new JLabel();
        try {
            registeredTicketsLabel.setText("Registrierte Tickets: " + JSONConfigurations.getRegisteredTicketsCount(currentEventName));
        } catch (IOException ex) {}
        registeredTicketsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registeredTicketsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel revenueLabel = new JLabel();
        try {
            revenueLabel.setText("Umsatz: " + JSONConfigurations.getNonAvailableTicketsCount(currentEventName) * currentTicket.price + "€");
        } catch (IOException ex) {}
        revenueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        revenueLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JPanel informationPanel = new JPanel();
        informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
        informationPanel.setBackground(null);
        informationPanel.add(eventNameLabel);
        informationPanel.add(dateLabel);
        informationPanel.add(locationLabel);
        informationPanel.add(ticketCountLabel);
        informationPanel.add(priceLabel);
        informationPanel.add(nonavailableTicketsLabel);
        informationPanel.add(registeredTicketsLabel);
        informationPanel.add(revenueLabel);

        JPanel rowOne = new JPanel();
        rowOne.setLayout(new BoxLayout(rowOne, BoxLayout.X_AXIS));
        rowOne.setBackground(null);
        rowOne.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowOne.add(eventNameSearchTextFieldPanel);

        eventNameSelectBox.addActionListener(e -> {
            currentEventName = eventNameSelectBox.getSelectedItem();
            try {
                ticketsList = JSONConfigurations.getTicketsAsList(currentEventName);
                currentTicket = ticketsList.get(0);
            } catch (IOException ex) {}
            eventNameLabel.setText("Event Name: " + currentEventName);
            dateLabel.setText("Date: " + dateFormat.format(new Date(currentTicket.date)));
            locationLabel.setText("Ort: " + currentTicket.location);
            try {
                ticketCountLabel.setText("Ticket-Anzahl: " + JSONConfigurations.getTicketsCount(currentEventName));
            } catch (IOException ex) {}
            priceLabel.setText("Preis: " + currentTicket.price + "€");
            try {
                nonavailableTicketsLabel.setText("Verkaufte Tickets: " + JSONConfigurations.getNonAvailableTicketsCount(currentEventName));
            } catch (IOException ex) {}
            try {
                registeredTicketsLabel.setText("Registrierte Tickets: " + JSONConfigurations.getRegisteredTicketsCount(currentEventName));
            } catch (IOException ex) {}
            try {
                revenueLabel.setText("Umsatz: " + JSONConfigurations.getNonAvailableTicketsCount(currentEventName) * currentTicket.price + "€");
            } catch (IOException ex) {}
        });

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Colors.backgroundColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 55, 35));
        settingsPanel.add(titleLabel);
        settingsPanel.add(searchTitleLabel);
        settingsPanel.add(rowOne);
        settingsPanel.add(informationTitleLabel);
        settingsPanel.add(informationPanel);


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
