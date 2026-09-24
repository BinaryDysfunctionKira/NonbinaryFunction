package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Toast;
import com.binary_dysfunction.config.JSONConfigurations;
import com.binary_dysfunction.frames.HomeFrame;
import com.binary_dysfunction.types.Ticket;

public class RegisterTicketPanel extends JPanel {

    Ticket currentTicket;
    Object currentEventName;
    // int availableCount;

    public RegisterTicketPanel(HomeFrame currentFrame) {

        JLabel titleLabel = new JLabel("Ticket registrieren");
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


        JTextField ownerTextField = new JTextField();
        ownerTextField.setEnabled(false);
        ownerTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        ownerTextField.setBackground(Colors.backgorundColorDarker);

        JPanel ownerFieldPanel = new JPanel(new BorderLayout());
        ownerFieldPanel.setBackground(null);
        ownerFieldPanel.setMinimumSize(new Dimension(250, 50));
        ownerFieldPanel.setMaximumSize(new Dimension(250, 50));
        ownerFieldPanel.add(new JLabel("Ticketinhaber*in"), BorderLayout.NORTH);
        ownerFieldPanel.add(ownerTextField);

        JTextField locationTextField = new JTextField();
        locationTextField.setEnabled(false);
        locationTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        locationTextField.setBackground(Colors.backgorundColorDarker);

        JPanel locationFieldPanel = new JPanel(new BorderLayout());
        locationFieldPanel.setBackground(null);
        locationFieldPanel.setMinimumSize(new Dimension(200, 50));
        locationFieldPanel.setMaximumSize(new Dimension(200, 50));
        locationFieldPanel.add(new JLabel("Ort"), BorderLayout.NORTH);
        locationFieldPanel.add(locationTextField);

        SpinnerModel spinnerPriceModel = new SpinnerNumberModel(0.0, 0.0,99999.0, 0.5);
        JSpinner eventTicketPrice = new JSpinner(spinnerPriceModel);
        eventTicketPrice.setEnabled(false);
        eventTicketPrice.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(eventTicketPrice, "0.00' €'");
        eventTicketPrice.setEditor(editor);
        editor.getTextField().setOpaque(true);
        editor.getTextField().setBackground(Colors.backgorundColorDarker);

        JPanel priceFieldPanel = new JPanel(new BorderLayout());
        priceFieldPanel.setBackground(null);
        priceFieldPanel.setMinimumSize(new Dimension(100, 50));
        priceFieldPanel.setMaximumSize(new Dimension(100, 50));
        priceFieldPanel.add(new JLabel("Preis"), BorderLayout.NORTH);
        priceFieldPanel.add(eventTicketPrice);

        JPanel rowTwo = new JPanel();
        rowTwo.setLayout(new BoxLayout(rowTwo, BoxLayout.X_AXIS));
        rowTwo.setBackground(null);
        rowTwo.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowTwo.add(ownerFieldPanel);
        rowTwo.add(locationFieldPanel);
        rowTwo.add(priceFieldPanel);


        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner eventDateSpinner = new JSpinner(dateModel);
        eventDateSpinner.setEnabled(false);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(eventDateSpinner, "dd.MM.yyyy");
        eventDateSpinner.setEditor(dateEditor);
        eventDateSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        dateEditor.setOpaque(true);
        dateEditor.setBackground(Colors.backgorundColorDarker);

        JPanel dateFieldPanel = new JPanel(new BorderLayout());
        dateFieldPanel.setBackground(null);
        dateFieldPanel.setMaximumSize(new Dimension(150, 50));
        dateFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateFieldPanel.add(new JLabel("Datum"), BorderLayout.NORTH);
        dateFieldPanel.add(eventDateSpinner);

        JCheckBox availableCheckBox = new JCheckBox();
        availableCheckBox.setEnabled(false);
        JPanel availableCheckBoxPanel = new JPanel(new BorderLayout());
        availableCheckBoxPanel.setBackground(null);
        availableCheckBoxPanel.setMaximumSize(new Dimension(150, 50));
        availableCheckBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        availableCheckBoxPanel.add(new JLabel("Verfügbar"));
        availableCheckBoxPanel.add(availableCheckBox, BorderLayout.WEST);

        JCheckBox registeredCheckBox = new JCheckBox();
        registeredCheckBox.setEnabled(false);
        JPanel registeredCheckBoxPanel = new JPanel(new BorderLayout());
        registeredCheckBoxPanel.setBackground(null);
        registeredCheckBoxPanel.setMaximumSize(new Dimension(150, 50));
        registeredCheckBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        registeredCheckBoxPanel.add(new JLabel("Registriert"));
        registeredCheckBoxPanel.add(registeredCheckBox, BorderLayout.WEST);

        JPanel rowThree = new JPanel();
        rowThree.setLayout(new BoxLayout(rowThree, BoxLayout.X_AXIS));
        rowThree.setBackground(null);
        rowThree.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        rowThree.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowThree.add(availableCheckBoxPanel);
        rowThree.add(registeredCheckBoxPanel);

        JLabel errorLabel = new JLabel("Ungültige Angaben.");
        errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        errorLabel.setForeground(Colors.redFontColor);
        errorLabel.setVisible(false);


        JComboBox<Object> searchTicketIDBox = new JComboBox<>();
        JButton submitButton = new JButton();

        JComboBox<Object> eventNameSelectBox = new JComboBox<>(JSONConfigurations.getEvents().toArray());
        eventNameSelectBox.setFont(new Font("Arial", Font.PLAIN, 14));
        eventNameSelectBox.setBackground(Colors.backgorundColorDarker);
        if (currentEventName != null) eventNameSelectBox.setSelectedItem(currentEventName);
        eventNameSelectBox.addActionListener(e -> {
            currentEventName = eventNameSelectBox.getSelectedItem();
            try {
                searchTicketIDBox.setModel(new DefaultComboBoxModel<>(JSONConfigurations.getNonAvailableTickets(eventNameSelectBox.getSelectedItem()).toArray()));
            } catch (IOException ex) {}
            try {
                currentTicket = JSONConfigurations.getTicket(currentEventName, searchTicketIDBox.getSelectedItem());
            } catch (IOException ex) {}
            ownerTextField.setText(currentTicket.owner);
            locationTextField.setText(currentTicket.location);
            spinnerPriceModel.setValue(currentTicket.price);
            eventDateSpinner.setValue(new Date(currentTicket.date));
            availableCheckBox.setSelected(currentTicket.available);
            registeredCheckBox.setSelected(currentTicket.registered);
            if (!currentTicket.registered) {
                submitButton.setBackground(Colors.greenButtonColor);
                submitButton.setText("Registrieren");
            }
            else {
                submitButton.setBackground(Colors.redButtonColor);
                submitButton.setText("Abmelden");
            }
        });

        JPanel eventNameSearchTextFieldPanel = new JPanel(new BorderLayout());
        eventNameSearchTextFieldPanel.setBackground(null);
        eventNameSearchTextFieldPanel.setMaximumSize(new Dimension(200, 50));
        eventNameSearchTextFieldPanel.add(new JLabel("Event Name"), BorderLayout.NORTH);
        eventNameSearchTextFieldPanel.add(eventNameSelectBox);

        try {
            searchTicketIDBox.setModel(new DefaultComboBoxModel<>(JSONConfigurations.getNonAvailableTickets(eventNameSelectBox.getSelectedItem()).toArray()));
        } catch (IOException e1) {}
        searchTicketIDBox.setFont(new Font("Arial", Font.PLAIN, 14));
        searchTicketIDBox.setBackground(Colors.backgorundColorDarker);
        searchTicketIDBox.addActionListener(e -> {
            try {
                currentTicket = JSONConfigurations.getTicket(currentEventName, searchTicketIDBox.getSelectedItem());
            } catch (IOException ex) {}
            ownerTextField.setText(currentTicket.owner);
            locationTextField.setText(currentTicket.location);
            spinnerPriceModel.setValue(currentTicket.price);
            eventDateSpinner.setValue(new Date(currentTicket.date));
            availableCheckBox.setSelected(currentTicket.available);
            registeredCheckBox.setSelected(currentTicket.registered);
            if (!currentTicket.registered) {
                submitButton.setBackground(Colors.greenButtonColor);
                submitButton.setText("Registrieren");
            }
            else {
                submitButton.setBackground(Colors.redButtonColor);
                submitButton.setText("Abmelden");
            }
        });

        JPanel ticketIdSearchTextFieldPanel = new JPanel(new BorderLayout());
        ticketIdSearchTextFieldPanel.setBackground(null);
        ticketIdSearchTextFieldPanel.setMaximumSize(new Dimension(200, 50));
        ticketIdSearchTextFieldPanel.add(new JLabel("Ticket ID"), BorderLayout.NORTH);
        ticketIdSearchTextFieldPanel.add(searchTicketIDBox);

        currentEventName = eventNameSelectBox.getSelectedItem();
        try {
            currentTicket = JSONConfigurations.getTicket(eventNameSelectBox.getSelectedItem(), searchTicketIDBox.getSelectedItem());
        } catch (IOException ex) {}

        JPanel rowOne = new JPanel();
        rowOne.setLayout(new BoxLayout(rowOne, BoxLayout.X_AXIS));
        rowOne.setBackground(null);
        rowOne.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowOne.add(eventNameSearchTextFieldPanel);
        rowOne.add(ticketIdSearchTextFieldPanel);

        if (currentTicket != null) ownerTextField.setText(currentTicket.owner);
        if (currentTicket != null) locationTextField.setText(currentTicket.location);
        if (currentTicket != null) spinnerPriceModel.setValue(currentTicket.price);
        if (currentTicket != null) eventDateSpinner.setValue(new Date(currentTicket.date));
        if (currentTicket != null) availableCheckBox.setSelected(currentTicket.available);
        if (currentTicket != null) registeredCheckBox.setSelected(currentTicket.registered);


        if (!currentTicket.registered) {
            submitButton.setBackground(Colors.greenButtonColor);
            submitButton.setText("Registrieren");
        }
        else {
            submitButton.setBackground(Colors.redButtonColor);
            submitButton.setText("Abmelden");
        }
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        submitButton.addActionListener((ActionEvent e) -> {
            try {
                if (currentTicket.registered) {
                    JSONConfigurations.changeTicketValue(currentEventName, searchTicketIDBox.getSelectedItem(), "registered", false);
                    registeredCheckBox.setSelected(false);
                    submitButton.setBackground(Colors.greenButtonColor);
                    submitButton.setText("Registrieren");
                    currentTicket.registered = false;
                    Toast.show(null, "Ticket", currentTicket.owner + " signed out", 3000, Toast.Position.BOTTOM_RIGHT, false);
                } else {
                    JSONConfigurations.changeTicketValue(currentEventName, searchTicketIDBox.getSelectedItem(), "registered", true);
                    registeredCheckBox.setSelected(true);
                    submitButton.setBackground(Colors.redButtonColor);
                    submitButton.setText("Abmelden");
                    currentTicket.registered = true;
                    Toast.show(null, "Ticket", currentTicket.owner + " registered", 3000, Toast.Position.BOTTOM_RIGHT, false);
                }
            } catch (IOException ex) {}
        });

        JPanel submitButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        submitButtonPanel.setBackground(null);
        submitButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        submitButtonPanel.add(submitButton);


        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Colors.backgroundColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 55, 0));
        settingsPanel.add(titleLabel);
        settingsPanel.add(searchTitleLabel);
        settingsPanel.add(rowOne);
        settingsPanel.add(informationTitleLabel);
        settingsPanel.add(rowTwo);
        settingsPanel.add(dateFieldPanel);
        settingsPanel.add(rowThree);
        settingsPanel.add(submitButtonPanel);
        settingsPanel.add(errorLabel);

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
