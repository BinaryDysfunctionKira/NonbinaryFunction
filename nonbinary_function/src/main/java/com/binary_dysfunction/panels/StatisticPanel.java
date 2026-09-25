package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.config.JSONConfigurations;
import com.binary_dysfunction.config.JSONConfigurations.EventStats;
import com.binary_dysfunction.frames.HomeFrame;
import com.binary_dysfunction.printing.TicketPreviewFrame;
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
        currentEventName = eventNameSelectBox.getSelectedItem();

        JPanel eventNameSearchTextFieldPanel = new JPanel(new BorderLayout());
        eventNameSearchTextFieldPanel.setBackground(null);
        eventNameSearchTextFieldPanel.setMaximumSize(new Dimension(200, 50));
        eventNameSearchTextFieldPanel.add(new JLabel("Event Name"), BorderLayout.NORTH);
        eventNameSearchTextFieldPanel.add(eventNameSelectBox);

        JProgressBar loadingBar = new JProgressBar(0, 100);
        loadingBar.setStringPainted(true);
        loadingBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadingBar.setMaximumSize(new Dimension(300, 20));
        loadingBar.setVisible(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        JLabel eventNameLabel = new JLabel("Event Name: ");
        eventNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        eventNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel dateLabel = new JLabel("Date: ");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel locationLabel = new JLabel("Ort: ");
        locationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        locationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel ticketCountLabel = new JLabel("Ticket-Anzahl: ");
        ticketCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel priceLabel = new JLabel("Preis: ");
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel nonavailableTicketsLabel = new JLabel("Verkaufte Tickets: ");
        nonavailableTicketsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nonavailableTicketsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel registeredTicketsLabel = new JLabel("Registrierte Tickets: ");
        registeredTicketsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registeredTicketsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel revenueLabel = new JLabel("Umsatz: ");
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

        JButton printTicketsButton = new JButton("Tickets drucken");
        printTicketsButton.setBackground(Colors.greenButtonColor);
        printTicketsButton.addActionListener(e -> {
            new TicketPreviewFrame(ticketsList).setVisible(true);
        });

        // Lädt alle Ticket-Dateien eines Events in einem Hintergrund-Thread
        // (statt bis zu 3x auf dem EDT) und meldet den Fortschritt an die Progress Bar.
        Runnable[] loadEventHolder = new Runnable[1];
        loadEventHolder[0] = () -> {
            Object eventName = eventNameSelectBox.getSelectedItem();
            if (eventName == null) return;
            currentEventName = eventName;

            eventNameSelectBox.setEnabled(false);
            printTicketsButton.setEnabled(false);
            loadingBar.setValue(0);
            loadingBar.setVisible(true);

            SwingWorker<EventStats, Integer> worker = new SwingWorker<>() {
                @Override
                protected EventStats doInBackground() throws Exception {
                    return JSONConfigurations.loadEventStats(eventName, (doneCount, total) -> {
                        int percent = total == 0 ? 100 : (int) ((doneCount * 100L) / total);
                        publish(percent);
                    });
                }

                @Override
                protected void process(List<Integer> chunks) {
                    loadingBar.setValue(chunks.get(chunks.size() - 1));
                }

                @Override
                protected void done() {
                    try {
                        EventStats stats = get();
                        ticketsList = stats.tickets;
                        currentTicket = ticketsList.isEmpty() ? null : ticketsList.get(0);

                        eventNameLabel.setText("Event Name: " + eventName);
                        if (currentTicket != null) {
                            dateLabel.setText("Date: " + dateFormat.format(new Date(currentTicket.date)));
                            locationLabel.setText("Ort: " + currentTicket.location);
                            priceLabel.setText("Preis: " + currentTicket.price + "€");
                        }
                        ticketCountLabel.setText("Ticket-Anzahl: " + stats.totalCount);
                        nonavailableTicketsLabel.setText("Verkaufte Tickets: " + stats.nonAvailableCount);
                        registeredTicketsLabel.setText("Registrierte Tickets: " + stats.registeredCount);
                        double price = currentTicket != null ? currentTicket.price : 0;
                        revenueLabel.setText("Umsatz: " + (stats.nonAvailableCount * price) + "€");
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                    } finally {
                        loadingBar.setVisible(false);
                        eventNameSelectBox.setEnabled(true);
                        printTicketsButton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        };

        eventNameSelectBox.addActionListener(e -> loadEventHolder[0].run());
        if (currentEventName != null) loadEventHolder[0].run();

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Colors.backgroundColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 55, 35));
        settingsPanel.add(titleLabel);
        settingsPanel.add(searchTitleLabel);
        settingsPanel.add(rowOne);
        settingsPanel.add(loadingBar);
        settingsPanel.add(informationTitleLabel);
        settingsPanel.add(informationPanel);
        settingsPanel.add(printTicketsButton);

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