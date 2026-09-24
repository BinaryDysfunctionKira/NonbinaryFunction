package com.binary_dysfunction.printing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.binary_dysfunction.types.Ticket;
import com.google.zxing.WriterException;

public class TicketPreviewFrame extends JFrame {

    private static class TicketSize {
        final double widthCm, heightCm;
        TicketSize(double widthCm, double heightCm) {
            this.widthCm = widthCm;
            this.heightCm = heightCm;
        }
    }

    // Gängige Größen, u. a. klassische Konzert-/Kino-Ticket-Maße
    private static final Map<String, TicketSize> SIZE_PRESETS = new LinkedHashMap<>();
    static {
        SIZE_PRESETS.put("Konzertticket (21 × 7,4 cm)", new TicketSize(21.0, 7.4));
        SIZE_PRESETS.put("Kinoticket (16 × 5,5 cm)", new TicketSize(16.0, 5.5));
        SIZE_PRESETS.put("Eintrittskarte klein (14 × 5,1 cm)", new TicketSize(14.0, 5.1));
        SIZE_PRESETS.put("Eventticket groß (21 × 9,9 cm)", new TicketSize(21.0, 9.9));
        SIZE_PRESETS.put("Benutzerdefiniert...", null);
    }

    private final List<Ticket> ticketsList;
    private BufferedImage template;
    private BufferedImage previewImage;
    private final JPanel previewPanel;
    private final JLabel templatePathLabel;
    private final JButton printButton, savePngButton, sheetPreviewButton;
    private final JComboBox<String> sizePresetBox;
    private final JSpinner customWidthSpinner, customHeightSpinner;

    private double currentWidthCm = 21.0;
    private double currentHeightCm = 7.4;

    public TicketPreviewFrame(List<Ticket> ticketsList) {
        super("Ticket Vorschau");
        this.ticketsList = ticketsList;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (previewImage == null) return;
                double scale = Math.min(
                        (double) getWidth() / previewImage.getWidth(),
                        (double) getHeight() / previewImage.getHeight()
                );
                int w = (int) (previewImage.getWidth() * scale);
                int h = (int) (previewImage.getHeight() * scale);
                g.drawImage(previewImage, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            }
        };
        previewPanel.setPreferredSize(new Dimension(900, 650));
        previewPanel.setBackground(Color.DARK_GRAY);

        JButton chooseTemplateButton = new JButton("Vorlage auswählen...");

        template = TicketImageGenerator.createBlankWhiteTemplate(currentWidthCm, currentHeightCm);
        templatePathLabel = new JLabel("Standard (weiß)");

        sizePresetBox = new JComboBox<>(SIZE_PRESETS.keySet().toArray(new String[0]));
        customWidthSpinner = new JSpinner(new SpinnerNumberModel(currentWidthCm, 1.0, 100.0, 0.1));
        customHeightSpinner = new JSpinner(new SpinnerNumberModel(currentHeightCm, 1.0, 100.0, 0.1));
        customWidthSpinner.setEnabled(false);
        customHeightSpinner.setEnabled(false);
        ((JSpinner.DefaultEditor) customWidthSpinner.getEditor()).getTextField().setColumns(4);
        ((JSpinner.DefaultEditor) customHeightSpinner.getEditor()).getTextField().setColumns(4);

        sizePresetBox.addActionListener(e -> {
            String selected = (String) sizePresetBox.getSelectedItem();
            TicketSize size = SIZE_PRESETS.get(selected);
            boolean isCustom = size == null;
            customWidthSpinner.setEnabled(isCustom);
            customHeightSpinner.setEnabled(isCustom);

            if (!isCustom) {
                currentWidthCm = size.widthCm;
                currentHeightCm = size.heightCm;
                customWidthSpinner.setValue(currentWidthCm);
                customHeightSpinner.setValue(currentHeightCm);
            } else {
                currentWidthCm = (Double) customWidthSpinner.getValue();
                currentHeightCm = (Double) customHeightSpinner.getValue();
            }
            onSizeChanged();
        });

        customWidthSpinner.addChangeListener(e -> {
            currentWidthCm = (Double) customWidthSpinner.getValue();
            onSizeChanged();
        });
        customHeightSpinner.addChangeListener(e -> {
            currentHeightCm = (Double) customHeightSpinner.getValue();
            onSizeChanged();
        });

        printButton = new JButton("Drucken (" + ticketsList.size() + ")");
        savePngButton = new JButton("Als PNGs speichern");
        sheetPreviewButton = new JButton("A4-Vorschau");

        boolean hasTickets = !ticketsList.isEmpty();
        printButton.setEnabled(hasTickets);
        savePngButton.setEnabled(hasTickets);
        sheetPreviewButton.setEnabled(hasTickets);

        chooseTemplateButton.addActionListener(e -> chooseTemplate());
        printButton.addActionListener(e -> printTickets());
        savePngButton.addActionListener(e -> savePngs());
        sheetPreviewButton.addActionListener(e -> showSheetPreview());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(chooseTemplateButton);
        controlPanel.add(templatePathLabel);
        controlPanel.add(new JLabel("Größe:"));
        controlPanel.add(sizePresetBox);
        controlPanel.add(new JLabel("B (cm):"));
        controlPanel.add(customWidthSpinner);
        controlPanel.add(new JLabel("H (cm):"));
        controlPanel.add(customHeightSpinner);
        controlPanel.add(printButton);
        controlPanel.add(savePngButton);
        controlPanel.add(sheetPreviewButton);

        add(previewPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        refreshPreview();
        pack();
        setLocationRelativeTo(null);
    }

    private void onSizeChanged() {
        // Weiße Standardvorlage muss neu in der passenden Größe erzeugt werden,
        // eine vom Nutzer gewählte eigene Vorlage bleibt unverändert (wird eh per "cover" eingepasst)
        if (templatePathLabel.getText().equals("Standard (weiß)")) {
            template = TicketImageGenerator.createBlankWhiteTemplate(currentWidthCm, currentHeightCm);
        }
        refreshPreview();
    }

    private void chooseTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Bilder", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                template = ImageIO.read(chooser.getSelectedFile());
                templatePathLabel.setText(chooser.getSelectedFile().getName());
                refreshPreview();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Vorlage konnte nicht geladen werden: " + ex.getMessage());
            }
        }
    }

    private void refreshPreview() {
        if (template == null || ticketsList.isEmpty()) return;
        try {
            previewImage = TicketImageGenerator.generateTicketOverlay(template, ticketsList.get(0), currentWidthCm, currentHeightCm);
            previewPanel.repaint();
        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(this, "Fehler bei der Vorschau: " + ex.getMessage());
        }
    }

    private List<BufferedImage> generateAllTicketImages() throws WriterException {
        List<BufferedImage> images = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            images.add(TicketImageGenerator.generateTicketOverlay(template, ticket, currentWidthCm, currentHeightCm));
        }
        return images;
    }

    private void savePngs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Ordner zum Speichern der Tickets wählen");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File targetDir = chooser.getSelectedFile();

        try {
            List<BufferedImage> images = generateAllTicketImages();
            for (int i = 0; i < images.size(); i++) {
                File out = new File(targetDir, ticketsList.get(i).id + ".png");
                ImageIO.write(images.get(i), "png", out);
            }
            JOptionPane.showMessageDialog(this, images.size() + " Tickets gespeichert.");
        } catch (WriterException | HeadlessException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void showSheetPreview() {
        try {
            // Die Vorschau kennt den im Druckdialog gewählten Drucker noch nicht -> Standarddrucker
            PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
            int[] printableArea = printableAreaPx(pageFormat);
            warnIfTicketWasShrunk(printableArea[0], printableArea[1]);
            List<BufferedImage> images = generateAllTicketImages();
            List<BufferedImage> sheets = TicketSheetRenderer.renderSheets(
                    images, currentWidthCm, currentHeightCm, printableArea[0], printableArea[1]);
            new SheetPreviewDialog(this, sheets).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler bei der A4-Vorschau: " + ex.getMessage());
        }
    }

    private static int[] printableAreaPx(PageFormat pageFormat) {
        int widthPx = (int) Math.round(pageFormat.getImageableWidth() / 72.0 * TicketSheetRenderer.DPI);
        int heightPx = (int) Math.round(pageFormat.getImageableHeight() / 72.0 * TicketSheetRenderer.DPI);
        return new int[]{widthPx, heightPx};
    }

    private void printTickets() {
        if (template == null) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst eine Vorlage auswählen.");
            return;
        }
        if (ticketsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Für dieses Event sind keine Tickets vorhanden.");
            return;
        }

        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(Sides.ONE_SIDED);

            // Erst den Dialog, damit Drucker/Papierformat/Ausrichtung des Nutzers ins Layout einfließen
            if (!job.printDialog(attributes)) return;

            PageFormat pageFormat = job.getPageFormat(attributes);
            int[] printableArea = printableAreaPx(pageFormat);
            warnIfTicketWasShrunk(printableArea[0], printableArea[1]);

            List<BufferedImage> images = generateAllTicketImages();
            List<BufferedImage> sheets = TicketSheetRenderer.renderSheets(
                    images, currentWidthCm, currentHeightCm, printableArea[0], printableArea[1]);

            Book book = new Book();
            book.append(new TicketPrintable(sheets), pageFormat, sheets.size());
            job.setPageable(book);
            job.print(attributes);
        } catch (WriterException | HeadlessException | PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Drucken: " + ex.getMessage());
        }
    }

    private void warnIfTicketWasShrunk(int printableWidthPx, int printableHeightPx) {
        TicketSheetRenderer.Layout layout = TicketSheetRenderer.computeLayout(
                currentWidthCm, currentHeightCm, printableWidthPx, printableHeightPx);
        if (layout.isShrunk()) {
            JOptionPane.showMessageDialog(this, String.format(
                    "Hinweis: Das Ticket (%.1f cm breit) passt nicht in den bedruckbaren Bereich des Druckers, "
                    + "auch nicht gedreht. Es wird auf ca. %.1f cm Breite verkleinert gedruckt.",
                    currentWidthCm, currentWidthCm * layout.shrinkScale));
        }
    }
}