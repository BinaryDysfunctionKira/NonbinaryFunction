package com.binary_dysfunction.printing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/** A4-Vorschau: rendert nur die gerade angezeigte Seite (in niedriger Auflösung) und hält wenige Seiten im Cache.
 *  Zeigt wahlweise die Vorder- oder die Rückseite (Umschalt-Button) für den doppelseitigen Druck. */
public class SheetPreviewDialog extends JDialog {

    private static final int PREVIEW_DPI = 120;
    private static final int CACHE_PAGES = 4;

    private final TicketSheets frontSheets;
    private final TicketSheets backSheets; // kann null sein, falls keine Rückseite verwendet wird

    // Je ein LRU-Cache pro Seite (Vorder-/Rückseite): älteste Seite fliegt raus, sobald mehr als CACHE_PAGES gespeichert sind
    private final Map<Integer, BufferedImage> frontCache = createLruCache();
    private final Map<Integer, BufferedImage> backCache = createLruCache();

    private static Map<Integer, BufferedImage> createLruCache() {
        return new LinkedHashMap<Integer, BufferedImage>(8, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, BufferedImage> eldest) {
                return size() > CACHE_PAGES;
            }
        };
    }

    private final JPanel canvas;
    private final JLabel pageLabel = new JLabel();
    private final JButton prevButton = new JButton("◀ Zurück");
    private final JButton nextButton = new JButton("Weiter ▶");
    private final JButton sideToggleButton = new JButton();

    private boolean showingBack = false;
    private int pageIndex = 0;
    private boolean loading = false;
    private BufferedImage currentImage;
    private String message = "";

    public SheetPreviewDialog(Frame owner, TicketSheets frontSheets) {
        this(owner, frontSheets, null);
    }

    public SheetPreviewDialog(Frame owner, TicketSheets frontSheets, TicketSheets backSheets) {
        super(owner, "A4-Vorschau", true);
        this.frontSheets = frontSheets;
        this.backSheets = backSheets;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                if (currentImage == null) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawString(message, 20, 30);
                    return;
                }
                double scale = Math.min(
                        (double) (getWidth() - 20) / currentImage.getWidth(),
                        (double) (getHeight() - 20) / currentImage.getHeight()
                );
                int w = Math.max(1, (int) (currentImage.getWidth() * scale));
                int h = Math.max(1, (int) (currentImage.getHeight() * scale));
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(currentImage, x, y, w, h, null);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, w, h);
            }
        };
        canvas.setPreferredSize(new Dimension(700, 850));
        canvas.setBackground(Color.DARK_GRAY);

        prevButton.addActionListener(e -> showPage(pageIndex - 1));
        nextButton.addActionListener(e -> showPage(pageIndex + 1));
        sideToggleButton.addActionListener(e -> {
            showingBack = !showingBack;
            updateSideToggleLabel();
            showPage(Math.min(pageIndex, currentSheets().getPageCount() - 1));
        });
        sideToggleButton.setVisible(backSheets != null);
        updateSideToggleLabel();

        TicketSheetRenderer.Layout layout = frontSheets.getLayout();
        JLabel infoLabel = new JLabel(layout.itemsPerPage + " Tickets pro Seite"
                + (layout.rotated ? ", gedreht" : "")
                + (layout.isShrunk() ? ", verkleinert" : ""));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controls.add(prevButton);
        controls.add(pageLabel);
        controls.add(nextButton);
        controls.add(sideToggleButton);
        controls.add(infoLabel);

        add(canvas, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        showPage(0);
    }

    private void updateSideToggleLabel() {
        sideToggleButton.setText(showingBack ? "Zeige: Rückseite ⇄" : "Zeige: Vorderseite ⇄");
    }

    private TicketSheets currentSheets() {
        return showingBack && backSheets != null ? backSheets : frontSheets;
    }

    private Map<Integer, BufferedImage> currentCache() {
        return showingBack ? backCache : frontCache;
    }

    private void showPage(int index) {
        TicketSheets sheets = currentSheets();

        if (sheets.getPageCount() == 0) {
            currentImage = null;
            message = "Keine Tickets vorhanden.";
            updateControls();
            canvas.repaint();
            return;
        }
        if (index < 0 || index >= sheets.getPageCount()) return;

        pageIndex = index;

        Map<Integer, BufferedImage> cache = currentCache();
        BufferedImage cached = cache.get(index);
        if (cached != null) {
            currentImage = cached;
            loading = false;
            updateControls();
            canvas.repaint();
            return;
        }

        loading = true;
        currentImage = null;
        message = "Seite " + (index + 1) + " wird erstellt ...";
        updateControls();
        canvas.repaint();

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return sheets.renderPage(index, PREVIEW_DPI / (double) TicketSheetRenderer.DPI);
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    BufferedImage image = get();
                    cache.put(index, image);
                    currentImage = image;
                } catch (InterruptedException | ExecutionException ex) {
                    currentImage = null;
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    message = "Fehler bei der Vorschau: " + cause;
                }
                updateControls();
                canvas.repaint();
            }
        }.execute();
    }

    private void updateControls() {
        int pageCount = currentSheets().getPageCount();
        prevButton.setEnabled(!loading && pageIndex > 0);
        nextButton.setEnabled(!loading && pageIndex < pageCount - 1);
        sideToggleButton.setEnabled(!loading);
        pageLabel.setText(pageCount == 0
                ? "Keine Seiten"
                : "Seite " + (pageIndex + 1) + " / " + pageCount + " (" + currentSheets().getTicketCount() + " Tickets)");
    }

    @Override
    public void dispose() {
        frontCache.clear();
        backCache.clear();
        currentImage = null;
        super.dispose();
    }
}