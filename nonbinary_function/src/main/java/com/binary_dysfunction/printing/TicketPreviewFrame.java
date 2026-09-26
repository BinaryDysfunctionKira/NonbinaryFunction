package com.binary_dysfunction.printing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

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

    // Export der A4-Seiten als PNG: volle DIN-A4-Seite, Tickets werden innerhalb des Randes angeordnet
    private static final double A4_WIDTH_CM = 21.0;
    private static final double A4_HEIGHT_CM = 29.7;
    private static final double A4_MARGIN_CM = 0.5; // Rand rundum (hier anpassen)

    private final List<Ticket> ticketsList;
    private BufferedImage template;
    private BufferedImage backTemplate; // null = Rückseite ist einfarbig schwarz (Standard)
    private BufferedImage previewImage;
    private final JPanel previewPanel;
    private final JLabel templatePathLabel;
    private final JLabel backTemplatePathLabel = new JLabel("Schwarz (Standard)");
    private final JCheckBox duplexCheckBox = new JCheckBox("Doppelseitig drucken (Rückseite)");
    private final JCheckBox blurryCheckBox = new JCheckBox("Rückseite weichzeichnen");
    private final JComboBox<String> edgeBox = new JComboBox<>(new String[]{"Lange Kante (Standard)", "Kurze Kante"});
    private final JButton chooseBackTemplateButton = new JButton("Rückseiten-Vorlage wählen...");
    private final JButton resetBackTemplateButton = new JButton("Rückseite: Schwarz");
    private final JButton printButton, savePngButton, saveSheetPngButton, sheetPreviewButton;
    private final JButton toggleSideButton = new JButton();
    private final JComboBox<String> sizePresetBox;
    private final JSpinner customWidthSpinner, customHeightSpinner;

    private double currentWidthCm = 21.0;
    private double currentHeightCm = 7.4;
    private boolean showingBackPreview = false;

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
        saveSheetPngButton = new JButton("A4-Seiten als PNGs");
        sheetPreviewButton = new JButton("A4-Vorschau");

        boolean hasTickets = !ticketsList.isEmpty();
        printButton.setEnabled(hasTickets);
        savePngButton.setEnabled(hasTickets);
        saveSheetPngButton.setEnabled(hasTickets);
        sheetPreviewButton.setEnabled(hasTickets);

        chooseTemplateButton.addActionListener(e -> chooseTemplate());
        chooseBackTemplateButton.addActionListener(e -> chooseBackTemplate());
        resetBackTemplateButton.addActionListener(e -> {
            backTemplate = null;
            backTemplatePathLabel.setText("Schwarz (Standard)");
            if (showingBackPreview) refreshPreview();
        });
        blurryCheckBox.addActionListener(e -> {
            if (showingBackPreview) refreshPreview();
        });
        printButton.addActionListener(e -> printTickets());
        savePngButton.addActionListener(e -> savePngs());
        saveSheetPngButton.addActionListener(e -> saveSheetPngs());
        sheetPreviewButton.addActionListener(e -> showSheetPreview());
        // Doppelseitig an/aus ändert, ob das Namensfeld auf der Vorderseite erscheint - Vorschau nur
        // aktualisieren, wenn gerade die Vorderseite angezeigt wird (die Rückseite ist davon nicht betroffen).
        duplexCheckBox.addActionListener(e -> {
            if (!showingBackPreview) refreshPreview();
        });
        toggleSideButton.addActionListener(e -> {
            showingBackPreview = !showingBackPreview;
            updateToggleSideButtonLabel();
            refreshPreview();
        });
        updateToggleSideButtonLabel();

        edgeBox.setToolTipText("An welcher Kante das Blatt beim doppelseitigen Druck gewendet wird - bestimmt, "
                + "wie die Rückseite gespiegelt werden muss, damit sie nach dem Wenden zur Vorderseite passt. "
                + "\"Lange Kante\" ist bei den meisten Duplexdruckern voreingestellt.");

        JPanel frontRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        frontRow.add(chooseTemplateButton);
        frontRow.add(templatePathLabel);
        frontRow.add(new JLabel("Größe:"));
        frontRow.add(sizePresetBox);
        frontRow.add(new JLabel("B (cm):"));
        frontRow.add(customWidthSpinner);
        frontRow.add(new JLabel("H (cm):"));
        frontRow.add(customHeightSpinner);

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backRow.add(duplexCheckBox);
        backRow.add(chooseBackTemplateButton);
        backRow.add(backTemplatePathLabel);
        backRow.add(resetBackTemplateButton);
        backRow.add(blurryCheckBox);
        backRow.add(new JLabel("Wendekante:"));
        backRow.add(edgeBox);
        backRow.add(toggleSideButton);

        // Die Rückseiten-Einstellungen sind nur relevant, wenn doppelseitiger Druck aktiviert ist;
        // die Vorschau (toggleSideButton) bleibt davon unabhängig nutzbar, um die Rückseite trotzdem zu gestalten.
        setBackSettingsEnabled(duplexCheckBox.isSelected());
        duplexCheckBox.addActionListener(e -> setBackSettingsEnabled(duplexCheckBox.isSelected()));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionRow.add(printButton);
        actionRow.add(savePngButton);
        actionRow.add(saveSheetPngButton);
        actionRow.add(sheetPreviewButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(frontRow);
        controlPanel.add(backRow);
        controlPanel.add(actionRow);

        add(previewPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        refreshPreview();
        pack();
        setLocationRelativeTo(null);
    }

    private void setBackSettingsEnabled(boolean enabled) {
        chooseBackTemplateButton.setEnabled(enabled);
        backTemplatePathLabel.setEnabled(enabled);
        resetBackTemplateButton.setEnabled(enabled);
        blurryCheckBox.setEnabled(enabled);
        edgeBox.setEnabled(enabled);
    }

    private void updateToggleSideButtonLabel() {
        toggleSideButton.setText(showingBackPreview ? "Vorschau: Rückseite ⇄" : "Vorschau: Vorderseite ⇄");
    }

    private TicketSheetRenderer.DuplexEdge selectedEdge() {
        return edgeBox.getSelectedIndex() == 1
                ? TicketSheetRenderer.DuplexEdge.SHORT_EDGE
                : TicketSheetRenderer.DuplexEdge.LONG_EDGE;
    }

    private Sides selectedSidesAttribute() {
        return selectedEdge() == TicketSheetRenderer.DuplexEdge.SHORT_EDGE
                ? Sides.TWO_SIDED_SHORT_EDGE
                : Sides.TWO_SIDED_LONG_EDGE;
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

    private void chooseBackTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Bilder", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                backTemplate = ImageIO.read(chooser.getSelectedFile());
                backTemplatePathLabel.setText(chooser.getSelectedFile().getName());
                if (showingBackPreview) refreshPreview();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Rückseiten-Vorlage konnte nicht geladen werden: " + ex.getMessage());
            }
        }
    }

    private void refreshPreview() {
        if (template == null || ticketsList.isEmpty()) return;
        try {
            previewImage = showingBackPreview
                    ? TicketImageGenerator.generateTicketBack(backTemplate, blurryCheckBox.isSelected(),
                            ticketsList.get(0), currentWidthCm, currentHeightCm)
                    : TicketImageGenerator.generateTicketOverlay(template, ticketsList.get(0), currentWidthCm, currentHeightCm,
                            !duplexCheckBox.isSelected());
            previewPanel.repaint();
        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(this, "Fehler bei der Vorschau: " + ex.getMessage());
        }
    }

    /**
     * Erzeugt die (lazy gerenderten) Druckseiten für die Vorderseite. Vorlage, Größe und Ticketliste werden
     * eingefroren, damit spätere Änderungen im Fenster eine laufende Vorschau bzw. einen laufenden Druck nicht
     * beeinflussen.
     */
    private TicketSheets createFrontSheets(int printableWidthPx, int printableHeightPx) {
        final BufferedImage frozenTemplate = template;
        final double widthCm = currentWidthCm;
        final double heightCm = currentHeightCm;
        final List<Ticket> tickets = new ArrayList<>(ticketsList);
        // Bei aktivem doppelseitigem Druck steht das Namensfeld bereits auf der Rückseite -
        // auf der Vorderseite entfällt es dann, damit es nicht doppelt erscheint.
        final boolean includeNameField = !duplexCheckBox.isSelected();

        return new TicketSheets(widthCm, heightCm, printableWidthPx, printableHeightPx, tickets.size(),
                index -> TicketImageGenerator.generateTicketOverlay(frozenTemplate, tickets.get(index), widthCm, heightCm,
                        includeNameField));
    }

    /**
     * Erzeugt die Druckseiten für die Rückseite. Die Zellenanordnung wird passend zur gewählten Wendekante
     * gespiegelt, damit jede Rückseite nach dem Wenden des Blattes exakt hinter ihrer Vorderseite liegt.
     */
    private TicketSheets createBackSheets(int printableWidthPx, int printableHeightPx) {
        final BufferedImage frozenBackTemplate = backTemplate;
        final boolean blurry = blurryCheckBox.isSelected();
        final double widthCm = currentWidthCm;
        final double heightCm = currentHeightCm;
        final List<Ticket> tickets = new ArrayList<>(ticketsList);
        final TicketSheetRenderer.DuplexEdge edge = selectedEdge();

        return new TicketSheets(widthCm, heightCm, printableWidthPx, printableHeightPx, tickets.size(),
                index -> TicketImageGenerator.generateTicketBack(frozenBackTemplate, blurry, tickets.get(index), widthCm, heightCm),
                edge);
    }

    private void savePngs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Ordner zum Speichern der Tickets wählen");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File targetDir = chooser.getSelectedFile();

        boolean duplex = duplexCheckBox.isSelected();
        try {
            // Ticket für Ticket erzeugen und schreiben, statt alle Bilder gleichzeitig im Speicher zu halten
            for (Ticket ticket : ticketsList) {
                BufferedImage front = TicketImageGenerator.generateTicketOverlay(template, ticket, currentWidthCm, currentHeightCm,
                        !duplex);
                ImageIO.write(front, "png", new File(targetDir, ticket.id + (duplex ? "-vorne.png" : ".png")));

                if (duplex) {
                    BufferedImage back = TicketImageGenerator.generateTicketBack(backTemplate, blurryCheckBox.isSelected(),
                            ticket, currentWidthCm, currentHeightCm);
                    ImageIO.write(back, "png", new File(targetDir, ticket.id + "-hinten.png"));
                }
            }
            JOptionPane.showMessageDialog(this, ticketsList.size() + " Tickets"
                    + (duplex ? " (Vorder- und Rückseite)" : "") + " gespeichert.");
        } catch (WriterException | HeadlessException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private static int cmToPx(double cm) {
        return (int) Math.round(cm / 2.54 * TicketSheetRenderer.DPI);
    }

    /**
     * Speichert die A4-Seiten (wie beim Drucken angeordnet) als PNG-Dateien in einen Ordner - für Vorder- UND
     * Rückseite. Jede Datei ist eine volle DIN-A4-Seite mit 300 DPI (2480 × 3508 px) und trägt die DPI-Angabe,
     * sodass sie später in Originalgröße gedruckt werden kann. Läuft im Hintergrund mit Fortschrittsdialog.
     */
    private void saveSheetPngs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("PDF-Datei für die A4-Seiten wählen");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF-Datei", "pdf"));
        chooser.setSelectedFile(new File("tickets-a4.pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File targetFile = chooser.getSelectedFile();
        if (!targetFile.getName().toLowerCase().endsWith(".pdf")) {
            targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".pdf");
        }
        final File pdfFile = targetFile;

        final int pageWidthPx = cmToPx(A4_WIDTH_CM);
        final int pageHeightPx = cmToPx(A4_HEIGHT_CM);
        final int marginPx = cmToPx(A4_MARGIN_CM);
        final int areaWidthPx = pageWidthPx - 2 * marginPx;
        final int areaHeightPx = pageHeightPx - 2 * marginPx;

        warnIfTicketWasShrunk(areaWidthPx, areaHeightPx);

        final boolean duplex = duplexCheckBox.isSelected();
        final TicketSheets frontSheets = createFrontSheets(areaWidthPx, areaHeightPx);
        final TicketSheets backSheets = duplex ? createBackSheets(areaWidthPx, areaHeightPx) : null;
        final int pageCount = frontSheets.getPageCount();
        final int totalToWrite = pageCount * (duplex ? 2 : 1);

        JProgressBar bar = new JProgressBar(0, totalToWrite);
        bar.setStringPainted(true);
        bar.setString("0 / " + totalToWrite);
        JButton cancelButton = new JButton("Abbrechen");
        JDialog progressDialog = createProgressDialog("PDF wird erzeugt ...",
                "Die A4-Seiten" + (duplex ? " (Vorder- und Rückseite)" : "") + " werden in eine PDF-Datei geschrieben.",
                bar, cancelButton);

        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int written = 0;
                try (PDDocument document = new PDDocument()) {
                    for (int i = 0; i < pageCount; i++) {
                        if (isCancelled()) break;
                        addSheetPage(document, frontSheets, i, pageWidthPx, pageHeightPx, marginPx);
                        publish(++written);

                        if (!duplex) continue;
                        if (isCancelled()) break;
                        addSheetPage(document, backSheets, i, pageWidthPx, pageHeightPx, marginPx);
                        publish(++written);
                    }
                    if (!isCancelled()) {
                        document.save(pdfFile);
                    }
                }
                return written;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int done = chunks.get(chunks.size() - 1);
                bar.setValue(done);
                bar.setString(done + " / " + totalToWrite);
            }

            @Override
            protected void done() {
                progressDialog.dispose();

                if (isCancelled()) {
                    JOptionPane.showMessageDialog(TicketPreviewFrame.this,
                            "Abgebrochen. Es wurde keine PDF-Datei gespeichert.");
                    return;
                }
                try {
                    get();
                    JOptionPane.showMessageDialog(TicketPreviewFrame.this,
                            pageCount + " A4-Seite(n)" + (duplex ? " (Vorder-/Rückseite)" : "")
                                    + " als PDF gespeichert:\n" + pdfFile.getAbsolutePath());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String hint = cause instanceof OutOfMemoryError
                            ? " Starte die Anwendung ggf. mit mehr Heap (z. B. -Xmx2g)." : "";
                    JOptionPane.showMessageDialog(TicketPreviewFrame.this,
                            "Fehler beim Erzeugen der PDF-Datei: " + cause + hint);
                }
            }
        };

        cancelButton.addActionListener(e -> {
            cancelButton.setEnabled(false);
            worker.cancel(true);
        });

        worker.execute();
        progressDialog.setVisible(true);
    }

    /** Rendert eine Bogenseite, bettet sie mittig mit Rand in eine A4-Seite ein und fügt sie als PDF-Seite an. */
    private static void addSheetPage(PDDocument document, TicketSheets sheets, int pageIndex,
                                    int pageWidthPx, int pageHeightPx, int marginPx) throws Exception {
        BufferedImage area = sheets.renderPage(pageIndex, 1.0);
        BufferedImage page = new BufferedImage(pageWidthPx, pageHeightPx, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = page.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, pageWidthPx, pageHeightPx);
            g2d.drawImage(area, marginPx, marginPx, null);
        } finally {
            g2d.dispose();
        }

        PDPage pdPage = new PDPage(PDRectangle.A4);
        document.addPage(pdPage);
        PDImageXObject pdImage = LosslessFactory.createFromImage(document, page);
        try (PDPageContentStream content = new PDPageContentStream(document, pdPage)) {
            content.drawImage(pdImage, 0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
        }
    }

    /** Schreibt ein PNG inklusive DPI-Angabe (pHYs-Chunk), damit es beim Drucken in der richtigen Größe erscheint. */
    private static void writePngWithDpi(BufferedImage image, File file, int dpi) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image), param);

            // Der PNG-Writer des JDK erwartet hier Pixel pro Millimeter
            String pixelsPerMm = Double.toString(dpi / 25.4);
            IIOMetadataNode horizontal = new IIOMetadataNode("HorizontalPixelSize");
            horizontal.setAttribute("value", pixelsPerMm);
            IIOMetadataNode vertical = new IIOMetadataNode("VerticalPixelSize");
            vertical.setAttribute("value", pixelsPerMm);
            IIOMetadataNode dimension = new IIOMetadataNode("Dimension");
            dimension.appendChild(horizontal);
            dimension.appendChild(vertical);
            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
            root.appendChild(dimension);
            metadata.mergeTree("javax_imageio_1.0", root);

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, metadata), param);
            }
        } finally {
            writer.dispose();
        }
    }

    /** Modaler Fortschrittsdialog mit Balken und "Abbrechen"-Button (schließbar nur über den Button bzw. dispose()). */
    private JDialog createProgressDialog(String title, String infoText, JProgressBar bar, JButton cancelButton) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        content.add(new JLabel(infoText), BorderLayout.NORTH);
        content.add(bar, BorderLayout.CENTER);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonRow.add(cancelButton);
        content.add(buttonRow, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setPreferredSize(new Dimension(460, 150));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private void showSheetPreview() {
        try {
            // Die Vorschau kennt den im Druckdialog gewählten Drucker noch nicht -> Standarddrucker
            PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
            int[] printableArea = printableAreaPx(pageFormat);
            warnIfTicketWasShrunk(printableArea[0], printableArea[1]);

            boolean duplex = duplexCheckBox.isSelected();
            TicketSheets frontSheets = createFrontSheets(printableArea[0], printableArea[1]);
            TicketSheets backSheets = duplex ? createBackSheets(printableArea[0], printableArea[1]) : null;
            new SheetPreviewDialog(this, frontSheets, backSheets).setVisible(true);
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
            boolean duplex = duplexCheckBox.isSelected();

            PrinterJob job = PrinterJob.getPrinterJob();
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            // Duplexdruck nur vorschlagen, wenn die Option aktiviert ist; sonst ausdrücklich einseitig
            attributes.add(duplex ? selectedSidesAttribute() : Sides.ONE_SIDED);

            // Der Druckdialog liest die Seitenzahl vom Job (für "Seiten X bis Y"). Deshalb vorher ein
            // Platzhalter-Dokument mit der geschätzten Seitenzahl (Standarddrucker, Anzahl der Bögen) setzen.
            // Die Zahl bezieht sich bewusst auf Bögen (bei Duplex zählen Vorder+Rückseite zusammen als 1),
            // damit die Seitenauswahl im Dialog sich auf Tickets/Bögen bezieht und nicht auf einzelne Druckseiten.
            PageFormat estimateFormat = job.defaultPage();
            int[] estimateArea = printableAreaPx(estimateFormat);
            int estimatedPages = createFrontSheets(estimateArea[0], estimateArea[1]).getPageCount();
            Book placeholder = new Book();
            placeholder.append((g, pf, index) -> Printable.NO_SUCH_PAGE, estimateFormat, estimatedPages);
            job.setPageable(placeholder);

            // Dann den Dialog, damit Drucker/Papierformat/Ausrichtung des Nutzers ins Layout einfließen
            if (!job.printDialog(attributes)) return;

            // Unsere Auswahl (Duplex an/aus + Wendekante) ist maßgeblich dafür, wie die Rückseite gespiegelt
            // wurde bzw. ob es überhaupt eine gibt - falls der native Dialog eine andere Duplex-Option gesetzt
            // hat, hier wieder auf unsere Auswahl vereinheitlichen.
            attributes.add(duplex ? selectedSidesAttribute() : Sides.ONE_SIDED);

            PageFormat pageFormat = job.getPageFormat(attributes);
            int[] printableArea = printableAreaPx(pageFormat);
            warnIfTicketWasShrunk(printableArea[0], printableArea[1]);

            TicketSheets frontSheets = createFrontSheets(printableArea[0], printableArea[1]);
            TicketSheets backSheets = duplex ? createBackSheets(printableArea[0], printableArea[1]) : null;
            boolean[] selected = selectedPages(attributes, frontSheets.getPageCount());

            boolean anySelected = false;
            for (boolean s : selected) anySelected |= s;
            if (!anySelected) {
                JOptionPane.showMessageDialog(this, "Der gewählte Seitenbereich enthält keine Seiten (es gibt "
                        + frontSheets.getPageCount() + " Seiten). Es wurde nichts gedruckt.");
                return;
            }

            // Die Auswahl wurde bereits oben (auf Bogenebene) ausgewertet; die ursprüngliche PageRanges-Angabe
            // würde beim eigentlichen Druck sonst erneut - diesmal fälschlich auf einzelne Vorder-/Rückseiten -
            // angewendet. Deshalb hier entfernen, das fertige (bereits gefilterte) Buch drucken wir vollständig.
            attributes.remove(PageRanges.class);

            prepareSelectedSheetsThenPrint(job, attributes, pageFormat, frontSheets, backSheets, selected);
        } catch (HeadlessException ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Drucken: " + ex.getMessage());
        }
    }

    /** Liest den im Druckdialog gewählten Seitenbereich; ohne Angabe sind alle Seiten gewählt. */
    private static boolean[] selectedPages(PrintRequestAttributeSet attributes, int pageCount) {
        boolean[] selected = new boolean[pageCount];
        PageRanges ranges = (PageRanges) attributes.get(PageRanges.class);
        if (ranges == null) {
            Arrays.fill(selected, true);
            return selected;
        }
        for (int[] member : ranges.getMembers()) {
            int from = Math.max(1, member[0]);
            int to = Math.min(pageCount, member[1]);
            for (int page = from; page <= to; page++) {
                selected[page - 1] = true;
            }
        }
        return selected;
    }

    /**
     * Rendert zuerst ALLE gewählten Seiten - Vorder- UND Rückseite je Bogen - (im Hintergrund, mit
     * Fortschrittsdialog) in temporäre PNG-Dateien und startet erst danach den Druckauftrag, mit Vorder- und
     * Rückseite abwechselnd (Vorne 1, Hinten 1, Vorne 2, Hinten 2, ...), passend zum Duplexdruck. Schlägt die
     * Vorbereitung fehl oder wird abgebrochen, wird nichts gedruckt. Die Seiten liegen auf der Platte statt im
     * Arbeitsspeicher.
     */
    private void prepareSelectedSheetsThenPrint(PrinterJob job, PrintRequestAttributeSet attributes,
                                                PageFormat pageFormat, TicketSheets frontSheets, TicketSheets backSheets,
                                                boolean[] selected) {
        final boolean duplex = backSheets != null;
        final int pageCount = frontSheets.getPageCount();
        int selectedCount = 0;
        for (boolean s : selected) if (s) selectedCount++;
        final int totalToRender = selectedCount * (duplex ? 2 : 1);

        JProgressBar bar = new JProgressBar(0, totalToRender);
        bar.setStringPainted(true);
        bar.setString("0 / " + totalToRender);

        JButton cancelButton = new JButton("Abbrechen");
        JDialog progressDialog = createProgressDialog("Seiten werden vorbereitet ...",
                "Die gewählten Seiten werden erzeugt" + (duplex ? " (Vorder- und Rückseite)" : "")
                        + ", danach startet der Druck.", bar, cancelButton);

        SwingWorker<List<File>, Integer> worker = new SwingWorker<List<File>, Integer>() {
            private Path tempDir;

            @Override
            protected List<File> doInBackground() throws Exception {
                tempDir = Files.createTempDirectory("tickets-print");
                List<File> files = new ArrayList<>(totalToRender);
                int done = 0;
                for (int i = 0; i < pageCount; i++) {
                    if (!selected[i]) continue;
                    if (isCancelled()) break;

                    BufferedImage front = frontSheets.renderPage(i, 1.0);
                    File frontFile = tempDir.resolve("front-" + i + ".png").toFile();
                    ImageIO.write(front, "png", frontFile);
                    files.add(frontFile);
                    publish(++done);

                    if (!duplex) continue;
                    if (isCancelled()) break;

                    BufferedImage back = backSheets.renderPage(i, 1.0);
                    File backFile = tempDir.resolve("back-" + i + ".png").toFile();
                    ImageIO.write(back, "png", backFile);
                    files.add(backFile);
                    publish(++done);
                }
                return files;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int done = chunks.get(chunks.size() - 1);
                bar.setValue(done);
                bar.setString(done + " / " + totalToRender);
            }

            @Override
            protected void done() {
                progressDialog.dispose();

                List<File> files = null;
                if (!isCancelled()) {
                    try {
                        files = get();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        String hint = cause instanceof OutOfMemoryError
                                ? " Starte die Anwendung ggf. mit mehr Heap (z. B. -Xmx2g)." : "";
                        JOptionPane.showMessageDialog(TicketPreviewFrame.this,
                                "Fehler beim Erzeugen der Seiten, es wurde nichts gedruckt: " + cause + hint);
                    }
                }

                if (files != null && !files.isEmpty()) {
                    try {
                        Book book = new Book();
                        book.append(new TicketPrintable(files), pageFormat, files.size());
                        job.setPageable(book);
                        job.print(attributes);
                    } catch (PrinterException ex) {
                        JOptionPane.showMessageDialog(TicketPreviewFrame.this, "Fehler beim Drucken: " + ex.getMessage());
                    }
                }

                deleteTempDir(tempDir);
            }
        };

        cancelButton.addActionListener(e -> {
            cancelButton.setEnabled(false);
            worker.cancel(true);
        });

        worker.execute();
        progressDialog.setVisible(true); // blockiert (modal), bis done() den Dialog schließt
    }

    private static void deleteTempDir(Path dir) {
        if (dir == null) return;
        File[] files = dir.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        dir.toFile().delete();
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