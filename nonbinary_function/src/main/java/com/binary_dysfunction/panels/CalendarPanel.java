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

    // Der aktuell angezeigte Monat
    private YearMonth aktuellerMonat = YearMonth.now();

    // Das ausgewählte Datum
    private LocalDate ausgewaehltesDatum;

    // Zeigt z.B. "September 2026"
    private JLabel monatsLabel;

    // Hier kommen NUR die Zahlen hinein
    private JPanel tageGrid;


    // Farben
    private final Color HINTERGRUND = new Color(30, 30, 30);
    private final Color TEXT = new Color(235, 235, 235);
    private final Color RAHMEN = new Color(100, 100, 100);
    private final Color AUSGEWAEHLT = new Color(70, 90, 120);


    public CalendarPanel() {

        // Hintergrund des gesamten Kalenders
        setBackground(HINTERGRUND);

        // Hauptaufteilung
        setLayout(new BorderLayout());


        // =====================================================
        // NAVIGATION OBEN
        // =====================================================

        JPanel navigationPanel =
                new JPanel(new BorderLayout());

        navigationPanel.setBackground(HINTERGRUND);


        // Linker Pfeil
        JButton vorherigerButton =
                new JButton("◀");

        // Rechter Pfeil
        JButton naechsterButton =
                new JButton("▶");


        // Monatsname
        monatsLabel =
                new JLabel("", JLabel.CENTER);

        monatsLabel.setForeground(TEXT);

        monatsLabel.setFont(
                monatsLabel.getFont().deriveFont(22f)
        );


        // Pfeile dunkel machen
        styleButton(vorherigerButton);
        styleButton(naechsterButton);


        navigationPanel.add(
                vorherigerButton,
                BorderLayout.WEST
        );

        navigationPanel.add(
                monatsLabel,
                BorderLayout.CENTER
        );

        navigationPanel.add(
                naechsterButton,
                BorderLayout.EAST
        );


        add(
                navigationPanel,
                BorderLayout.NORTH
        );


        // =====================================================
        // KALENDER-BEREICH
        // =====================================================

        JPanel kalenderBereich =
                new JPanel(new BorderLayout());

        kalenderBereich.setBackground(HINTERGRUND);


        // =====================================================
        // WOCHENTAGE
        // =====================================================

        JPanel wochentageGrid =
                new JPanel(new GridLayout(1, 7));

        wochentageGrid.setBackground(HINTERGRUND);


        String[] wochentage = {
                "Mo",
                "Di",
                "Mi",
                "Do",
                "Fr",
                "Sa",
                "So"
        };


        for (String wochentag : wochentage) {

            JLabel label =
                    new JLabel(
                            wochentag,
                            JLabel.CENTER
                    );

            label.setForeground(TEXT);

            label.setBackground(HINTERGRUND);

            label.setOpaque(true);

            label.setBorder(
                    BorderFactory.createLineBorder(RAHMEN)
            );

            wochentageGrid.add(label);
        }


        // Wochentage ganz oben
        kalenderBereich.add(
                wochentageGrid,
                BorderLayout.NORTH
        );


        // =====================================================
        // TAGE-GRID
        // =====================================================

        // NUR die Zahlen kommen hier hinein
        tageGrid =
                new JPanel(new GridLayout(6, 7));

        tageGrid.setBackground(HINTERGRUND);


        kalenderBereich.add(
                tageGrid,
                BorderLayout.CENTER
        );


        // Kalenderbereich hinzufügen
        add(
                kalenderBereich,
                BorderLayout.CENTER
        );


        // =====================================================
        // PFEIL LINKS
        // =====================================================

        vorherigerButton.addActionListener(e -> {

            aktuellerMonat =
                    aktuellerMonat.minusMonths(1);

            kalenderAktualisieren();
        });


        // =====================================================
        // PFEIL RECHTS
        // =====================================================

        naechsterButton.addActionListener(e -> {

            aktuellerMonat =
                    aktuellerMonat.plusMonths(1);

            kalenderAktualisieren();
        });


        // Kalender beim Start erstellen
        kalenderAktualisieren();
    }


    // =========================================================
    // KALENDER AKTUALISIEREN
    // =========================================================

    private void kalenderAktualisieren() {

        // Alte Tage entfernen
        tageGrid.removeAll();


        // -----------------------------------------------------
        // MONATSNAME
        // -----------------------------------------------------

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "MMMM yyyy",
                        Locale.GERMAN
                );

        monatsLabel.setText(
                aktuellerMonat.format(formatter)
        );


        // -----------------------------------------------------
        // ERSTER TAG DES MONATS
        // -----------------------------------------------------

        LocalDate ersterTag =
                aktuellerMonat.atDay(1);


        // Montag = 0
        // Dienstag = 1
        // ...
        // Sonntag = 6

        int startPosition =
                ersterTag.getDayOfWeek().getValue() - 1;


        // -----------------------------------------------------
        // LEERE FELDER VOR DEM 1.
        // -----------------------------------------------------

        for (int i = 0; i < startPosition; i++) {

            JPanel leer =
                    new JPanel();

            leer.setBackground(HINTERGRUND);

            leer.setBorder(
                    BorderFactory.createLineBorder(RAHMEN)
            );

            tageGrid.add(leer);
        }


        // -----------------------------------------------------
        // TAGE DES MONATS
        // -----------------------------------------------------

        int tageImMonat =
                aktuellerMonat.lengthOfMonth();


        for (int tag = 1; tag <= tageImMonat; tag++) {

            // Vollständiges Datum
            LocalDate datum =
                    aktuellerMonat.atDay(tag);


            // Button für den Tag
            JButton tagButton =
                    new JButton(
                            String.valueOf(tag)
                    );


            // Button dunkel machen
            styleButton(tagButton);


            // -------------------------------------------------
            // TAG ANKLICKEN
            // -------------------------------------------------

            tagButton.addActionListener(e -> {

                // Datum speichern
                ausgewaehltesDatum = datum;

                System.out.println(
                        "Ausgewählt: "
                        + ausgewaehltesDatum
                );


                // Alle Buttons zurücksetzen
                for (
                        java.awt.Component component
                        : tageGrid.getComponents()
                ) {

                    if (component instanceof JButton) {

                        component.setBackground(
                                HINTERGRUND
                        );
                    }
                }


                // Ausgewählten Tag markieren
                tagButton.setBackground(
                        AUSGEWAEHLT
                );
            });


            tageGrid.add(tagButton);
        }


        // -----------------------------------------------------
        // RESTLICHE FELDER
        // -----------------------------------------------------

        int benutzteFelder =
                startPosition + tageImMonat;


        for (
                int i = benutzteFelder;
                i < 42;
                i++
        ) {

            JPanel leer =
                    new JPanel();

            leer.setBackground(HINTERGRUND);

            leer.setBorder(
                    BorderFactory.createLineBorder(RAHMEN)
            );

            tageGrid.add(leer);
        }


        // GUI aktualisieren
        tageGrid.revalidate();
        tageGrid.repaint();
    }


    // =========================================================
    // BUTTON-DESIGN
    // =========================================================

    private void styleButton(JButton button) {

        // Hintergrund dunkel
        button.setBackground(HINTERGRUND);

        // Schrift hell
        button.setForeground(TEXT);

        // Keine Standard-Fokus-Markierung
        button.setFocusPainted(false);

        // Keine Standard-Innenabstände
        button.setBorder(
                BorderFactory.createLineBorder(RAHMEN)
        );
    }


    // =========================================================
    // AUSGEWÄHLTES DATUM
    // =========================================================

    public LocalDate getAusgewaehltesDatum() {

        return ausgewaehltesDatum;
    }
}