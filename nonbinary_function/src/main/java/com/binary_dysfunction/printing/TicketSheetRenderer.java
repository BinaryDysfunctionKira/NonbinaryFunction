package com.binary_dysfunction.printing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TicketSheetRenderer {

    public static final int DPI = 300;
    private static final int GAP_PX = (int) (0.15 * DPI);

    public static class Layout {
        public final int columns, rows, itemsPerPage;
        public final int cellWidthPx, cellHeightPx; // Platz, den ein Ticket auf dem Blatt einnimmt (nach Drehung/Skalierung)
        public final boolean rotated;
        public final int sheetWidthPx, sheetHeightPx;
        public final double shrinkScale; // 1.0 = Originalgröße, < 1.0 = verkleinert

        Layout(int columns, int rows, int cellWidthPx, int cellHeightPx, boolean rotated,
               int sheetWidthPx, int sheetHeightPx, double shrinkScale) {
            this.columns = columns;
            this.rows = rows;
            this.itemsPerPage = columns * rows;
            this.cellWidthPx = cellWidthPx;
            this.cellHeightPx = cellHeightPx;
            this.rotated = rotated;
            this.sheetWidthPx = sheetWidthPx;
            this.sheetHeightPx = sheetHeightPx;
            this.shrinkScale = shrinkScale;
        }

        public boolean isShrunk() {
            return shrinkScale < 1.0;
        }
    }

    public static Layout computeLayout(double widthCm, double heightCm, int printableWidthPx, int printableHeightPx) {
        int ticketWidthPx = (int) Math.round(widthCm / 2.54 * DPI);
        int ticketHeightPx = (int) Math.round(heightCm / 2.54 * DPI);

        Layout normal = buildLayoutForOrientation(ticketWidthPx, ticketHeightPx, false, printableWidthPx, printableHeightPx);
        Layout rotated = buildLayoutForOrientation(ticketHeightPx, ticketWidthPx, true, printableWidthPx, printableHeightPx);

        // Vorrang hat die Variante, die in Originalgröße passt (keine Verkleinerung)
        if (normal.isShrunk() != rotated.isShrunk()) {
            return normal.isShrunk() ? rotated : normal;
        }

        // Sonst: mehr Tickets pro Seite; bei Gleichstand die nicht-gedrehte Variante
        return rotated.itemsPerPage > normal.itemsPerPage ? rotated : normal;
    }

    private static Layout buildLayoutForOrientation(int footprintWidthPx, int footprintHeightPx, boolean rotated,
                                                      int printableWidthPx, int printableHeightPx) {
        // Falls selbst diese Ausrichtung nicht in den bedruckbaren Bereich passt, als letzten Ausweg verkleinern
        double shrinkScale = Math.min(
                1.0,
                Math.min((double) printableWidthPx / footprintWidthPx, (double) printableHeightPx / footprintHeightPx)
        );
        if (shrinkScale < 1.0) {
            footprintWidthPx = (int) (footprintWidthPx * shrinkScale);
            footprintHeightPx = (int) (footprintHeightPx * shrinkScale);
        }

        int columns = Math.max(1, (printableWidthPx + GAP_PX) / (footprintWidthPx + GAP_PX));
        int rows = Math.max(1, (printableHeightPx + GAP_PX) / (footprintHeightPx + GAP_PX));

        return new Layout(columns, rows, footprintWidthPx, footprintHeightPx, rotated,
                printableWidthPx, printableHeightPx, shrinkScale);
    }

    public static List<BufferedImage> renderSheets(List<BufferedImage> tickets, double widthCm, double heightCm,
                                                     int printableWidthPx, int printableHeightPx) {
        Layout layout = computeLayout(widthCm, heightCm, printableWidthPx, printableHeightPx);
        List<BufferedImage> sheets = new ArrayList<>();
        int pageCount = (int) Math.ceil((double) tickets.size() / layout.itemsPerPage);
        for (int page = 0; page < pageCount; page++) {
            sheets.add(renderSheet(tickets, layout, page));
        }
        return sheets;
    }

    private static BufferedImage renderSheet(List<BufferedImage> tickets, Layout layout, int pageIndex) {
        BufferedImage sheet = new BufferedImage(layout.sheetWidthPx, layout.sheetHeightPx, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = sheet.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, layout.sheetWidthPx, layout.sheetHeightPx);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int startIndex = pageIndex * layout.itemsPerPage;
        for (int i = 0; i < layout.itemsPerPage; i++) {
            int ticketIndex = startIndex + i;
            if (ticketIndex >= tickets.size()) break;

            int col = i % layout.columns;
            int row = i / layout.columns;
            int x = col * (layout.cellWidthPx + GAP_PX);
            int y = row * (layout.cellHeightPx + GAP_PX);

            BufferedImage ticketImg = tickets.get(ticketIndex);
            if (layout.rotated) {
                ticketImg = rotateImage90(ticketImg);
            }
            g2d.drawImage(ticketImg, x, y, layout.cellWidthPx, layout.cellHeightPx, null);
        }
        g2d.dispose();
        return sheet;
    }

    /** Dreht ein Bild um 90° im Uhrzeigersinn (Breite/Höhe werden vertauscht). */
    private static BufferedImage rotateImage90(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dest.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform transform = new AffineTransform();
        transform.translate(h / 2.0, w / 2.0);
        transform.rotate(Math.PI / 2);
        transform.translate(-w / 2.0, -h / 2.0);

        g2d.drawImage(src, transform, null);
        g2d.dispose();
        return dest;
    }
}