package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.frames.HomeFrame;

public class AddTicketPanel extends JPanel {

    public AddTicketPanel(HomeFrame currentFrame) {

        JLabel titleLabel = new JLabel("Ticket hinzufügen");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

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
        eventTicketCountPanel.setMaximumSize(new Dimension(80, 50));
        eventTicketCountPanel.add(new JLabel("Anzahl"), BorderLayout.NORTH);
        eventTicketCountPanel.add(eventTicketCount);

        JPanel rowOne = new JPanel();
        rowOne.setLayout(new BoxLayout(rowOne, BoxLayout.X_AXIS));
        rowOne.setBackground(null);
        rowOne.add(eventNameTextFieldPanel);
        rowOne.add(eventTicketCountPanel);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Colors.backgroundColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 55, 35));
        settingsPanel.add(titleLabel);
        settingsPanel.add(rowOne);

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
