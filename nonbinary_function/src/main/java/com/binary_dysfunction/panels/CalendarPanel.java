package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class CalendarPanel extends JPanel {

        private YearMonth aktuellerMonat = YearMonth.now();
        private LocalDate ausgewaehltesDatum;
        private final JLabel monatsLabel;
        private final JPanel tageGrid;

// Hintergrund des Kalenders
    private final Color HINTERGRUND =
            new Color(30, 30, 30);

// Farbe der Schrift
    private final Color TEXT =
            new Color(235, 235, 235);

// Farbe der Rahmen
    private final Color RAHMEN =
            new Color(100, 100, 100);

// Farbe für einen ausgewählten Tag
    private final Color AUSGEWAEHLT =
            new Color(20, 70, 45);

    public CalendarPanel() {setBackground(HINTERGRUND);setLayout(new BorderLayout());

//Monatnaviagtion
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setBackground(HINTERGRUND);
        JButton vorherigerButton = new JButton("<");
        JButton naechsterButton =  new JButton(">");
        monatsLabel = new JLabel("",  JLabel.CENTER );
        monatsLabel.setFont(monatsLabel.getFont().deriveFont(26f));
         monatsLabel.setForeground(TEXT);

         // Pfeile gestalten
        styleButton(vorherigerButton );
        styleButton(naechsterButton );

        navigationPanel.add(vorherigerButton,BorderLayout.WEST );
        navigationPanel.add(monatsLabel,BorderLayout.CENTER);
        navigationPanel.add(naechsterButton,BorderLayout.EAST);

        add(navigationPanel, BorderLayout.NORTH);
//Wochentage
        JPanel kalenderBereich =new JPanel(new BorderLayout());
        kalenderBereich.setBackground(HINTERGRUND);
        JPanel wochentageGrid =new JPanel(new GridLayout(1, 7));
        wochentageGrid.setBackground(HINTERGRUND);


        String[] wochentage = {"Mo","Di","Mi","Do","Fr","Sa","So"};

        for (String wochentag : wochentage) { JLabel label = new JLabel( wochentag,JLabel.CENTER );
// Schrift größer
        label.setFont(label.getFont() .deriveFont(16f));

        label.setForeground(TEXT);
        label.setOpaque(true);
        label.setBackground(HINTERGRUND);
        label.setBorder(BorderFactory.createLineBorder(RAHMEN));
        wochentageGrid.add(label);}
        kalenderBereich.add( wochentageGrid,BorderLayout.NORTH);

// Das Grid wird später abhängig vom Monat mit der richtigen Anzahl an Zeilen erstellt.
        tageGrid = new JPanel();tageGrid.setBackground(HINTERGRUND );
        kalenderBereich.add(tageGrid,BorderLayout.CENTER);
        add(kalenderBereich,BorderLayout.CENTER);

//vorheriger Monat
        vorherigerButton.addActionListener(e -> {aktuellerMonat = aktuellerMonat.minusMonths(1);kalenderAktualisieren(); });

//nächster Monat
        naechsterButton.addActionListener(e -> {aktuellerMonat = aktuellerMonat.plusMonths(1);kalenderAktualisieren(); });


 // Kalender erstellen
        kalenderAktualisieren(); }

//Kalendar aktualisieren
        private void kalenderAktualisieren() {

        // Alte Tagesfelder löschen
        tageGrid.removeAll();

//Monatsname
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy",Locale.GERMAN);
        monatsLabel.setText(aktuellerMonat.format(formatter));

//erster Tag
        LocalDate ersterTag = aktuellerMonat.atDay(1);
// Montag = 0
// Dienstag = 1
 // ...
// Sonntag = 6
        int startPosition = ersterTag.getDayOfWeek().getValue() - 1;
//Anzahl Tage
        int tageImMonat = aktuellerMonat.lengthOfMonth();
//benötigte Felder
        int benoetigteFelder = startPosition+ tageImMonat;

//Berechen wieviele Felder gebraucht werden
        int anzahlZeilen =(int) Math.ceil(benoetigteFelder / 7.0 );

//mit diesen Feldern wird das Grid dann erstellt
        tageGrid.setLayout(new GridLayout(anzahlZeilen,7 ));

        for (int i = 0;i < startPosition;i++)
        {
        JPanel leer = new JPanel();
        leer.setBackground(HINTERGRUND);
        leer.setBorder(BorderFactory.createLineBorder(RAHMEN));
        tageGrid.add(leer);
        }
//Tage
        for (int tag = 1;tag <= tageImMonat;tag++ ) 
        {

// Vollständiges Datum
        LocalDate datum = aktuellerMonat.atDay(tag);

//Button für Tag
        JButton tagButton = new JButton( String.valueOf(tag));

// Button gestalten
        styleButton(tagButton );

//Tag anklicken

        tagButton.addActionListener(e -> {ausgewaehltesDatum = datum;
        System.out.println("Ausgewählt: "+ ausgewaehltesDatum);

// Alle anderen Tage zurücksetzen
        for (java.awt.Component component: tageGrid.getComponents())
        {

        if (component instanceof JButton) 
                {
                component.setBackground(HINTERGRUND);
                }
        }

// Angeklickten Tag 
        tagButton.setBackground(AUSGEWAEHLT);});
        tageGrid.add(tagButton);}

//GUI aktualisieren
        tageGrid.revalidate();
        tageGrid.repaint();
    }

    private void styleButton(JButton button) {

        
        button.setBackground(HINTERGRUND);
        button.setForeground(TEXT);
 // Fokus-Rahmen entfernen
        button.setFocusPainted(false);
// Rahmen
        button.setBorder(BorderFactory.createLineBorder(RAHMEN));
        button.setFont(button.getFont().deriveFont(16f));
    }
    public LocalDate getAusgewaehltesDatum() {return ausgewaehltesDatum;}
}