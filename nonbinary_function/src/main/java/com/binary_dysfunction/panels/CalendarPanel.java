package com.binary_dysfunction.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CalendarPanel extends JPanel {

    private YearMonth aktuellerMonat = YearMonth.now();
    private LocalDate ausgewaehltesDatum;
    private JLabel monatsLabel;
    private JPanel kalenderGrid;

    public CalendarPanel() {

        setLayout(new BorderLayout());


        //Monatsbereich
         JPanel navigationPanel = new JPanel(new BorderLayout()
        );
        JButton vorherigerButton = new JButton("<");
        JButton naechsterButton = new JButton(">");
        monatsLabel = new JLabel("", JLabel.CENTER);
        navigationPanel.add(
            vorherigerButton,
            BorderLayout.WEST
        );

        navigationPanel.add(monatsLabel,BorderLayout.CENTER
        );

        navigationPanel.add(naechsterButton,BorderLayout.EAST
        );
        add(navigationPanel, BorderLayout.NORTH);
        
        
        // 0 = beliebig viele Zeilen
        JPanel kalenderGrid = new JPanel(new GridLayout(0, 7));

        String[] wochentage = {
            "Mo",
            "Di",
            "Mi",
            "Do",
            "Fr",
            "Sa",
            "So"
        };
        
        // Jeden Wochentag als JLabel hinzufügen
        for (int i = 0; i < wochentage.length; i++) {

            JLabel label = new JLabel(
                wochentage[i],
                JLabel.CENTER
            );
            // Rahmen um das Feld
            label.setBorder(
                BorderFactory.createLineBorder(Color.GRAY)
            );

            kalenderGrid.add(label);
        }
        // Das heutige Datum holen
        LocalDate heute = LocalDate.now();

        // Erster Tag des aktuellen Monats
        LocalDate ersterTag = heute.withDayOfMonth(1);


        //-1 um Montag auf 0 zu setzen
        int startPosition =
            ersterTag.getDayOfWeek().getValue() - 1;

        // Leere Felder machen, wenn der Monat z.B. an einem Mittwoch beginnt
        for (int i = 0; i < startPosition; i++) {

            JLabel leer = new JLabel("");

            leer.setBorder(
                BorderFactory.createLineBorder(Color.GRAY)
            );

            kalenderGrid.add(leer);
        }
        // Das ist der Tag des Monats, Java weiß automatisch, ob der Monat 28, 29, 30 oder 31 Tage hat.

        int tageImMonat = heute.lengthOfMonth();

        // Jeden Tag des Monats hinzufügen
        for (int tag = 1; tag <= tageImMonat; tag++) {

            JLabel label = new JLabel(
                String.valueOf(tag),
                JLabel.CENTER
            );

            // Rahmen um jedes Tagesfeld
            label.setBorder(
                BorderFactory.createLineBorder(Color.GRAY)
            );

            kalenderGrid.add(label);
        }
        // Kalender einsetzen
        add(kalenderGrid, BorderLayout.CENTER);
    }
}