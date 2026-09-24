package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class CalendarPanel extends JPanel {

        private YearMonth aktuellerMonat = YearMonth.now();
        private LocalDate ausgewaehltesDatum;
        private final JLabel monatsLabel;
        private final JPanel tageGrid;

        // Hintergrund des Kalenders
        private final Color HINTERGRUND = new Color(30, 30, 30);
        private final Color TEXT = new Color(235, 235, 235);
        private final Color RAHMEN = new Color(100, 100, 100);
        private final Color AUSGEWAEHLT = new Color(20, 70, 45);

         public CalendarPanel() {setBackground(HINTERGRUND);setLayout(new BorderLayout());

        JPanel linkerBereich =new JPanel();linkerBereich.setLayout(new BoxLayout(linkerBereich,BoxLayout.Y_AXIS));

        linkerBereich.setBackground(HINTERGRUND);

        linkerBereich.setBorder(BorderFactory.createEmptyBorder(20,15,20,20));

        JLabel kalenderTitel = new JLabel("Kalendar");

        kalenderTitel.setFont(kalenderTitel.getFont().deriveFont(36f));
        kalenderTitel.setForeground(TEXT);
        kalenderTitel.setAlignmentX(LEFT_ALIGNMENT);
        linkerBereich.add(kalenderTitel);

        // Abstand zwischen Kalendar und Ereignis
        linkerBereich.add(Box.createVerticalStrut(70));

        JPanel ereignisPanel =new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        ereignisPanel.setBackground(HINTERGRUND);
        ereignisPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel ereignisTitel = new JLabel("Ereignis");
        ereignisTitel.setFont(ereignisTitel.getFont().deriveFont(22f));
        ereignisTitel.setForeground(TEXT);

        JButton plusButton =new JButton("+");
        plusButton.setFont(plusButton.getFont().deriveFont(20f));
        styleButton(plusButton);

        // Der Plus-Button macht momentan noch nichts.

        ereignisPanel.add(ereignisTitel);
        ereignisPanel.add(plusButton);
        linkerBereich.add(ereignisPanel);

        // Abstand zwischen "Ereignis" und dem ersten Rechteck
        linkerBereich.add(Box.createVerticalStrut(20));


        //Beispielereignis
        JButton ereignis1 = new JButton("Beispielereignis");
        styleEreignis(ereignis1);
        linkerBereich.add(ereignis1);

        // Abstand zwischen den Ereignissen
        linkerBereich.add(Box.createVerticalStrut(10));

        // Zweites Beispielereignis
        JButton ereignis2 = new JButton("Beispielereignis");styleEreignis(ereignis2);
        linkerBereich.add(ereignis2);

        linkerBereich.add(Box.createVerticalStrut(10));

        JButton ereignis3 = new JButton("Beispielereignis");
        styleEreignis(ereignis3);
        linkerBereich.add(ereignis3);

        JPanel kalenderBereich =new JPanel(new BorderLayout());
        kalenderBereich.setBackground(HINTERGRUND);
        kalenderBereich.setPreferredSize(new java.awt.Dimension( 650,600));

        JPanel navigationPanel =new JPanel(new BorderLayout());
        navigationPanel.setBackground(HINTERGRUND);

        JButton vorherigerButton = new JButton("<");

        JButton naechsterButton = new JButton(">");
        
        monatsLabel =new JLabel("",JLabel.CENTER);
        monatsLabel.setFont(monatsLabel.getFont().deriveFont(26f));
        monatsLabel.setForeground(TEXT);

        styleButton(vorherigerButton);
        styleButton(naechsterButton);

        navigationPanel.add(vorherigerButton,BorderLayout.WEST);
        navigationPanel.add(monatsLabel,BorderLayout.CENTER);

        navigationPanel.add(naechsterButton,BorderLayout.EAST);
        kalenderBereich.add(navigationPanel,BorderLayout.NORTH);

        JPanel kalenderInhalt =new JPanel(new BorderLayout());
        kalenderInhalt.setBackground(HINTERGRUND);

        JPanel wochentageGrid =new JPanel(new GridLayout(1,7));

        wochentageGrid.setBackground(HINTERGRUND);


        String[] wochentage = {"Mo","Di","Mi","Do","Fr","Sa","So"};

        for (String wochentag : wochentage) {

            JLabel label = new JLabel(wochentag,JLabel.CENTER);

        label.setFont(label.getFont().deriveFont(16f));
        label.setForeground(TEXT);
        label.setOpaque(true);
        label.setBackground(HINTERGRUND);
        label.setBorder(BorderFactory.createLineBorder(RAHMEN));

        wochentageGrid.add(label);}

        kalenderInhalt.add(wochentageGrid,BorderLayout.NORTH);

        tageGrid = new JPanel();
        tageGrid.setBackground(HINTERGRUND);

        kalenderInhalt.add(tageGrid,BorderLayout.CENTER);
        kalenderBereich.add(kalenderInhalt,BorderLayout.CENTER);

        add(linkerBereich,BorderLayout.CENTER);
        add(kalenderBereich,BorderLayout.EAST);

        vorherigerButton.addActionListener(e -> {

        aktuellerMonat = aktuellerMonat.minusMonths(1);kalenderAktualisieren();});

        naechsterButton.addActionListener(e -> {aktuellerMonat =aktuellerMonat.plusMonths(1);kalenderAktualisieren();});

        // Kalender beim Start erstellen
        kalenderAktualisieren();}

    private void styleEreignis(JButton button) {

        button.setBackground(HINTERGRUND);
        button.setForeground(TEXT);
        button.setFont(button.getFont().deriveFont(16f));
        button.setFocusPainted( false);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBorder(BorderFactory.createLineBorder(RAHMEN));
        // Größe des Rechtecks
        button.setPreferredSize(new java.awt.Dimension(300,50));
        button.setMaximumSize(new java.awt.Dimension(300,50));
        button.setAlignmentX(LEFT_ALIGNMENT);
    }

//Kalendar aktualisieren

    private void kalenderAktualisieren() {tageGrid.removeAll();
        DateTimeFormatter formatter =DateTimeFormatter.ofPattern("MMMM yyyy",Locale.GERMAN);
        monatsLabel.setText(aktuellerMonat.format(formatter));

        LocalDate ersterTag =aktuellerMonat.atDay(1);
        int startPosition =ersterTag.getDayOfWeek().getValue() - 1;

        int tageImMonat = aktuellerMonat.lengthOfMonth();

        int benoetigteFelder = startPosition + tageImMonat;

        int anzahlZeilen = (int) Math.ceil(benoetigteFelder / 7.0);

        tageGrid.setLayout(new GridLayout(anzahlZeilen,7));


        // Leere Felder vor dem ersten Tag
        for (int i = 0;i < startPosition;i++) {

        JPanel leer =new JPanel();
        leer.setBackground(HINTERGRUND);
        leer.setBorder(BorderFactory.createLineBorder(RAHMEN));

        tageGrid.add(leer);}

        // Tage erstellen
        for (int tag = 1;tag <= tageImMonat;tag++) {

        LocalDate datum = aktuellerMonat.atDay(tag);
        JButton tagButton = new JButton(String.valueOf(tag));
        styleButton(tagButton);

        // Tag anklicken
        tagButton.addActionListener(e -> {ausgewaehltesDatum =datum;

        System.out.println("Ausgewählt: "+ ausgewaehltesDatum);


        // Alle anderen Tage zurücksetzen
        for (java.awt.Component component: tageGrid.getComponents() ) {

                if (component instanceof JButton) {

                        component.setBackground(HINTERGRUND);
                    }
        }
        tagButton.setBackground(AUSGEWAEHLT);});
        tageGrid.add(tagButton);
        }
        tageGrid.revalidate();
        tageGrid.repaint();
    }
    private void styleButton(JButton button) {

        button.setBackground(HINTERGRUND);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(RAHMEN));
    }
    
    public LocalDate getAusgewaehltesDatum() {

        return ausgewaehltesDatum;
    }
}