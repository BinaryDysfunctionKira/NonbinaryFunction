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
import javax.swing.JButton;
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
    private BufferedImage previewImage;
    private final JPanel previewPanel;
    private final JLabel templatePathLabel;
    private final JButton printButton, savePngButton, saveSheetPngButton, sheetPreviewButton;
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
        saveSheetPngButton = new JButton("A4-Seiten als PNGs");
        sheetPreviewButton = new JButton("A4-Vorschau");

        boolean hasTickets = !ticketsList.isEmpty();
        printButton.setEnabled(hasTickets);
        savePngButton.setEnabled(hasTickets);
        saveSheetPngButton.setEnabled(hasTickets);
        sheetPreviewButton.setEnabled(hasTickets);

        chooseTemplateButton.addActionListener(e -> chooseTemplate());
        printButton.addActionListener(e -> printTickets());
        savePngButton.addActionListener(e -> savePngs());
        saveSheetPngButton.addActionListener(e -> saveSheetPngs());
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
        controlPanel.add(saveSheetPngButton);
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

    /**
     * Erzeugt die (lazy gerenderten) Druckseiten. Vorlage, Größe und Ticketliste werden eingefroren,
     * damit spätere Änderungen im Fenster eine laufende Vorschau bzw. einen laufenden Druck nicht beeinflussen.
     */
    private TicketSheets createSheets(int printableWidthPx, int printableHeightPx) {
        final BufferedImage frozenTemplate = template;
        final double widthCm = currentWidthCm;
        final double heightCm = currentHeightCm;
        final List<Ticket> tickets = new ArrayList<>(ticketsList);

        return new TicketSheets(widthCm, heightCm, printableWidthPx, printableHeightPx, tickets.size(),
                index -> TicketImageGenerator.generateTicketOverlay(frozenTemplate, tickets.get(index), widthCm, heightCm));
    }

    private void savePngs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Ordner zum Speichern der Tickets wählen");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File targetDir = chooser.getSelectedFile();

        try {
            // Ticket für Ticket erzeugen und schreiben, statt alle Bilder gleichzeitig im Speicher zu halten
            for (Ticket ticket : ticketsList) {
                BufferedImage image = TicketImageGenerator.generateTicketOverlay(template, ticket, currentWidthCm, currentHeightCm);
                ImageIO.write(image, "png", new File(targetDir, ticket.id + ".png"));
            }
            JOptionPane.showMessageDialog(this, ticketsList.size() + " Tickets gespeichert.");
        } catch (WriterException | HeadlessException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private static int cmToPx(double cm) {
        return (int) Math.round(cm / 2.54 * TicketSheetRenderer.DPI);
    }

    /**
     * Speichert die A4-Seiten (wie beim Drucken angeordnet) als PNG-Dateien in einen Ordner.
     * Jede Datei ist eine volle DIN-A4-Seite mit 300 DPI (2480 × 3508 px) und trägt die DPI-Angabe,
     * sodass sie später in Originalgröße gedruckt werden kann. Läuft im Hintergrund mit Fortschrittsdialog.
     */
    private void saveSheetPngs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Ordner zum Speichern der A4-Seiten wählen");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final File targetDir = chooser.getSelectedFile();

        final int pageWidthPx = cmToPx(A4_WIDTH_CM);
        final int pageHeightPx = cmToPx(A4_HEIGHT_CM);
        final int marginPx = cmToPx(A4_MARGIN_CM);
        final int areaWidthPx = pageWidthPx - 2 * marginPx;
        final int areaHeightPx = pageHeightPx - 2 * marginPx;

        warnIfTicketWasShrunk(areaWidthPx, areaHeightPx);

        final TicketSheets sheets = createSheets(areaWidthPx, areaHeightPx);
        final int pageCount = sheets.getPageCount();
        final int digits = Math.max(3, String.valueOf(pageCount).length());

        JProgressBar bar = new JProgressBar(0, pageCount);
        bar.setStringPainted(true);
        bar.setString("0 / " + pageCount);
        JButton cancelButton = new JButton("Abbrechen");
        JDialog progressDialog = createProgressDialog("A4-Seiten werden gespeichert ...",
                "Die A4-Seiten werden als PNG-Dateien geschrieben.", bar, cancelButton);

        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int written = 0;
                for (int i = 0; i < pageCount; i++) {
                    if (isCancelled()) break;

                    // Seite immer nur einzeln erzeugen -> konstanter Speicherbedarf
                    BufferedImage area = sheets.renderPage(i, 1.0);
                    BufferedImage page = new BufferedImage(pageWidthPx, pageHeightPx, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = page.createGraphics();
                    try {
                        g2d.setColor(Color.WHITE);
                        g2d.fillRect(0, 0, pageWidthPx, pageHeightPx);
                        g2d.drawImage(area, marginPx, marginPx, null);
                    } finally {
                        g2d.dispose();
                    }

                    File out = new File(targetDir, String.format("a4-seite-%0" + digits + "d.png", i + 1));
                    writePngWithDpi(page, out, TicketSheetRenderer.DPI);
                    publish(++written);
                }
                return written;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int done = chunks.get(chunks.size() - 1);
                bar.setValue(done);
                bar.setString(done + " / " + pageCount);
            }

            @Override
            protected void done() {
                progressDialog.dispose();

                if (isCancelled()) {
                    JOptionPane.showMessageDialog(TicketPreviewFrame.this,
                            "Abgebrochen. Bereits gespeicherte Seiten bleiben im Ordner liegen.");
                    return;
                }
                try {
                    int written = get();
                    JOptionPane.showMessageDialog(TicketPreviewFrame.this,
                            written + " A4-Seiten gespeichert in:\n" + targetDir.getAbsolutePath());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String hint = cause instanceof OutOfMemoryError
                            ? " Starte die Anwendung ggf. mit mehr Heap (z. B. -Xmx2g)." : "";
                    JOptionPane.showMessageDialog(TicketPreviewFrame.this,
                            "Fehler beim Speichern der A4-Seiten: " + cause + hint);
                }
            }
        };

        cancelButton.addActionListener(e -> {
            cancelButton.setEnabled(false);
            worker.cancel(true);
        });

        worker.execute();
        progressDialog.setVisible(true); // blockiert (modal), bis done() den Dialog schließt
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

            TicketSheets sheets = createSheets(printableArea[0], printableArea[1]);
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

            // Der Druckdialog liest die Seitenzahl vom Job (für "Seiten X bis Y"). Deshalb vorher ein
            // Platzhalter-Dokument mit der geschätzten Seitenzahl (Standarddrucker) setzen.
            PageFormat estimateFormat = job.defaultPage();
            int[] estimateArea = printableAreaPx(estimateFormat);
            int estimatedPages = createSheets(estimateArea[0], estimateArea[1]).getPageCount();
            Book placeholder = new Book();
            placeholder.append((g, pf, index) -> Printable.NO_SUCH_PAGE, estimateFormat, estimatedPages);
            job.setPageable(placeholder);

            // Dann den Dialog, damit Drucker/Papierformat/Ausrichtung des Nutzers ins Layout einfließen
            if (!job.printDialog(attributes)) return;

            PageFormat pageFormat = job.getPageFormat(attributes);
            int[] printableArea = printableAreaPx(pageFormat);
            warnIfTicketWasShrunk(printableArea[0], printableArea[1]);

            TicketSheets sheets = createSheets(printableArea[0], printableArea[1]);
            boolean[] selected = selectedPages(attributes, sheets.getPageCount());

            boolean anySelected = false;
            for (boolean s : selected) anySelected |= s;
            if (!anySelected) {
                JOptionPane.showMessageDialog(this, "Der gewählte Seitenbereich enthält keine Seiten (es gibt "
                        + sheets.getPageCount() + " Seiten). Es wurde nichts gedruckt.");
                return;
            }

            prepareSelectedSheetsThenPrint(job, attributes, pageFormat, sheets, selected);
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
     * Rendert zuerst ALLE gewählten Seiten (im Hintergrund, mit Fortschrittsdialog) in temporäre PNG-Dateien
     * und startet erst danach den Druckauftrag. Schlägt die Vorbereitung fehl oder wird abgebrochen,
     * wird nichts gedruckt. Die Seiten liegen auf der Platte statt im Arbeitsspeicher.
     */
    private void prepareSelectedSheetsThenPrint(PrinterJob job, PrintRequestAttributeSet attributes,
                                                PageFormat pageFormat, TicketSheets sheets, boolean[] selected) {
        final int pageCount = sheets.getPageCount();
        int selectedCount = 0;
        for (boolean s : selected) if (s) selectedCount++;
        final int totalToRender = selectedCount;

        JProgressBar bar = new JProgressBar(0, totalToRender);
        bar.setStringPainted(true);
        bar.setString("0 / " + totalToRender);

        JButton cancelButton = new JButton("Abbrechen");
        JDialog progressDialog = createProgressDialog("Seiten werden vorbereitet ...",
                "Die gewählten Seiten werden erzeugt, danach startet der Druck.", bar, cancelButton);

        SwingWorker<List<File>, Integer> worker = new SwingWorker<List<File>, Integer>() {
            private Path tempDir;

            @Override
            protected List<File> doInBackground() throws Exception {
                tempDir = Files.createTempDirectory("tickets-print");
                File[] files = new File[pageCount]; // Seiten außerhalb des Bereichs bleiben null
                int done = 0;
                for (int i = 0; i < pageCount; i++) {
                    if (!selected[i]) continue;
                    if (isCancelled()) break;
                    BufferedImage sheet = sheets.renderPage(i, 1.0);
                    File file = tempDir.resolve("sheet-" + i + ".png").toFile();
                    ImageIO.write(sheet, "png", file);
                    files[i] = file;
                    publish(++done);
                }
                return Arrays.asList(files);
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

                if (files != null) {
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