package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

public class AddTicketPanel extends JPanel {

    public AddTicketPanel(HomeFrame currentFrame) {

        JLabel titleLabel = new JLabel("Ticket hinzufügen");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField eventNameTextField = new JTextField();
        eventNameTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        eventNameTextField.setBackground(Colors.backgorundColorDarker);

        JPanel eventNameTextFieldPanel = new JPanel(new BorderLayout());
        eventNameTextFieldPanel.setBackground(null);
        eventNameTextFieldPanel.setMaximumSize(new Dimension(200, 50));
        eventNameTextFieldPanel.add(new JLabel("Event Name"), BorderLayout.NORTH);
        eventNameTextFieldPanel.add(eventNameTextField);

        SpinnerModel spinnerModel = new SpinnerNumberModel(100, 1,9999999, 10);
        JSpinner eventTicketCount = new JSpinner(spinnerModel);
        eventTicketCount.setFont(new Font("Arial", Font.PLAIN, 14));
        eventTicketCount.setBackground(Colors.backgorundColorDarker);

        JPanel eventTicketCountPanel = new JPanel(new BorderLayout());
        eventTicketCountPanel.setBackground(null);
        eventTicketCountPanel.setMaximumSize(new Dimension(100, 50));
        eventTicketCountPanel.add(new JLabel("Anzahl"), BorderLayout.NORTH);
        eventTicketCountPanel.add(eventTicketCount);

        JPanel rowOne = new JPanel();
        rowOne.setLayout(new BoxLayout(rowOne, BoxLayout.X_AXIS));
        rowOne.setBackground(null);
        rowOne.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowOne.add(eventNameTextFieldPanel);
        rowOne.add(eventTicketCountPanel);


        JTextField locationTextField = new JTextField();
        locationTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        locationTextField.setBackground(Colors.backgorundColorDarker);

        JPanel locationFieldPanel = new JPanel(new BorderLayout());
        locationFieldPanel.setBackground(null);
        locationFieldPanel.setMaximumSize(new Dimension(200, 50));
        locationFieldPanel.add(new JLabel("Ort"), BorderLayout.NORTH);
        locationFieldPanel.add(locationTextField);

        SpinnerModel spinnerPriceModel = new SpinnerNumberModel(0.0, 0.0,99999.0, 0.5);
        JSpinner eventTicketPrice = new JSpinner(spinnerPriceModel);
        eventTicketPrice.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(eventTicketPrice, "0.00' €'");
        eventTicketPrice.setEditor(editor);
        editor.getTextField().setOpaque(true);
        editor.getTextField().setBackground(Colors.backgorundColorDarker);

        JPanel priceFieldPanel = new JPanel(new BorderLayout());
        priceFieldPanel.setBackground(null);
        priceFieldPanel.setMaximumSize(new Dimension(100, 50));
        priceFieldPanel.add(new JLabel("Preis"), BorderLayout.NORTH);
        priceFieldPanel.add(eventTicketPrice);

        JPanel rowTwo = new JPanel();
        rowTwo.setLayout(new BoxLayout(rowTwo, BoxLayout.X_AXIS));
        rowTwo.setBackground(null);
        rowTwo.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowTwo.add(locationFieldPanel);
        rowTwo.add(priceFieldPanel);


        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner eventDateSpinner = new JSpinner(dateModel);
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

        JLabel errorLabel = new JLabel("Ungültige Angaben.");
        errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        errorLabel.setForeground(Colors.redFontColor);
        errorLabel.setVisible(false);

        JButton submitButton = new JButton("Erstellen");
        submitButton.setBackground(Colors.greenButtonColor);
        submitButton.setForeground(Colors.lighterFontColor);
        submitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        submitButton.addActionListener(e -> {
            if ((Integer) eventTicketCount.getValue() <= 0 || eventNameTextField.getText().equals("") || locationTextField.getText().equals("") || (Long) ((Date) eventDateSpinner.getValue()).getTime() < System.currentTimeMillis()) {
                errorLabel.setVisible(true);
            } else {
                boolean errorFree = true;
                for (int i = 0; i < (Integer) eventTicketCount.getValue(); i++) {
                    try {
                        JSONConfigurations.addTicket(eventNameTextField.getText(), i + 1, locationTextField.getText(), (Double) eventTicketPrice.getValue(), ((Date) eventDateSpinner.getValue()).getTime());
                    } catch (IOException ex) {
                        errorLabel.setVisible(true);
                        errorFree = false;
                        System.out.println(ex);
                    }
                }
                if (errorFree) Toast.show(null, "Tickets created", "Created: " + (Integer) eventTicketCount.getValue() + " Tickets.", 3000, Toast.Position.BOTTOM_RIGHT, false);
                currentFrame.setAddTicketPanel();
            }
        });

        JPanel submitButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        submitButtonPanel.setBackground(null);
        submitButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        submitButtonPanel.add(submitButton);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Colors.backgroundColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 55, 35));
        settingsPanel.add(titleLabel);
        settingsPanel.add(rowOne);
        settingsPanel.add(rowTwo);
        settingsPanel.add(dateFieldPanel);
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
