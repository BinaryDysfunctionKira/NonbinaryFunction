package com.binary_dysfunction.printing;

import com.binary_dysfunction.types.Ticket;
import com.google.zxing.WriterException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicketPreviewFrame extends JFrame {

    private final List<Ticket> ticketsList;
    private BufferedImage template;
    private BufferedImage previewImage;
    private final JPanel previewPanel;
    private final JLabel templatePathLabel;
    private final JButton printButton, savePngButton, sheetPreviewButton;

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

        // Standardmäßig eine schlichte weiße Vorlage verwenden, damit direkt eine Vorschau da ist
        template = TicketImageGenerator.createBlankWhiteTemplate();
        templatePathLabel = new JLabel("Standard (weiß)");

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
        controlPanel.add(printButton);
        controlPanel.add(savePngButton);
        controlPanel.add(sheetPreviewButton);

        add(previewPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        refreshPreview();
        pack();
        setLocationRelativeTo(null);
    }

    private void chooseTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Bilder", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                template = ImageIO.read(chooser.getSelectedFile());
                templatePathLabel.setText(chooser.getSelectedFile().getName());
                boolean hasTickets = !ticketsList.isEmpty();
                printButton.setEnabled(hasTickets);
                savePngButton.setEnabled(hasTickets);
                sheetPreviewButton.setEnabled(hasTickets);
                refreshPreview();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Vorlage konnte nicht geladen werden: " + ex.getMessage());
            }
        }
    }

    private void refreshPreview() {
        if (template == null || ticketsList.isEmpty()) return;
        try {
            previewImage = TicketImageGenerator.generateTicketOverlay(template, ticketsList.get(0));
            previewPanel.repaint();
        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(this, "Fehler bei der Vorschau: " + ex.getMessage());
        }
    }

    private List<BufferedImage> generateAllTicketImages() throws WriterException {
        List<BufferedImage> images = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            images.add(TicketImageGenerator.generateTicketOverlay(template, ticket));
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
            List<BufferedImage> images = generateAllTicketImages();
            List<BufferedImage> sheets = TicketSheetRenderer.renderSheets(images);
            new SheetPreviewDialog(this, sheets).setVisible(true);
        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(this, "Fehler bei der A4-Vorschau: " + ex.getMessage());
        }
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
            List<BufferedImage> images = generateAllTicketImages();
            List<BufferedImage> sheets = TicketSheetRenderer.renderSheets(images);

            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pageFormat = job.defaultPage();

            TicketPrintable printable = new TicketPrintable(sheets);
            Book book = new Book();
            book.append(printable, pageFormat, sheets.size());
            job.setPageable(book);

            if (job.printDialog()) {
                job.print();
            }
        } catch (WriterException | HeadlessException | PrinterException | NullPointerException ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Drucken: " + ex.getMessage());
        }
    }
}