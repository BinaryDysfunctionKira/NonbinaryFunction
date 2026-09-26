package com.binary_dysfunction.printing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.google.zxing.WriterException;

public class TicketSheetRenderer {

    public static final int DPI = 300;
    private static final int GAP_PX = (int) (0.15 * DPI);

    /** Liefert das fertige Ticketbild (300 DPI) für einen Ticket-Index - wird erst beim Zeichnen der Seite aufgerufen. */
    public interface TicketImageProvider {
        BufferedImage getTicketImage(int ticketIndex) throws WriterException;
    }

    /** Um welche Kante beim doppelseitigen Druck gewendet wird - bestimmt, wie die Rückseite gespiegelt werden muss. */
    public enum DuplexEdge {
        LONG_EDGE,  // Bei Hochformat die übliche Wendung: Rückseite muss spaltenweise (horizontal) gespiegelt werden
        SHORT_EDGE  // Rückseite muss zeilenweise (vertikal) gespiegelt werden
    }

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

    public static int pageCount(Layout layout, int ticketCount) {
        return (int) Math.ceil((double) ticketCount / layout.itemsPerPage);
    }

    /**
     * Rendert genau eine Seite. scale 1.0 = 300 DPI (Druck); kleinere Werte erzeugen ein kleineres Bild (Vorschau).
     * Die Ticketbilder werden einzeln erzeugt und nach dem Zeichnen sofort wieder freigegeben.
     */
    public static BufferedImage renderSheet(Layout layout, int pageIndex, int ticketCount,
                                            TicketImageProvider provider, double scale) throws WriterException {
        return renderSheet(layout, pageIndex, ticketCount, provider, scale, null);
    }

    /**
     * Wie {@link #renderSheet(Layout, int, int, TicketImageProvider, double)}, aber für die Rückseite beim
     * doppelseitigen Druck: die Zellenreihenfolge wird passend zur Wendekante gespiegelt, damit Vorder- und
     * Rückseite nach dem Wenden des Blattes deckungsgleich übereinanderliegen.
     */
    public static BufferedImage renderSheet(Layout layout, int pageIndex, int ticketCount,
                                            TicketImageProvider provider, double scale, DuplexEdge mirrorForEdge) throws WriterException {
        int sheetW = Math.max(1, (int) Math.round(layout.sheetWidthPx * scale));
        int sheetH = Math.max(1, (int) Math.round(layout.sheetHeightPx * scale));

        BufferedImage sheet = new BufferedImage(sheetW, sheetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = sheet.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, sheetW, sheetH);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int startIndex = pageIndex * layout.itemsPerPage;
            for (int i = 0; i < layout.itemsPerPage; i++) {
                int ticketIndex = startIndex + i;
                if (ticketIndex >= ticketCount) break;

                int col = i % layout.columns;
                int row = i / layout.columns;

                int cellW = (int) Math.round(layout.cellWidthPx * scale);
                int cellH = (int) Math.round(layout.cellHeightPx * scale);

                int x = (int) Math.round(col * (layout.cellWidthPx + GAP_PX) * scale);
                int y = (int) Math.round(row * (layout.cellHeightPx + GAP_PX) * scale);

                // Beim Spiegeln für die Rückseite wird die Zelle an der tatsächlichen Bogenkante (sheetW/sheetH)
                // gespiegelt - NICHT nur innerhalb des vom Raster belegten Bereichs (layout.columns/rows Zellen).
                // Da die Zellen den Bogen meist nicht bis exakt zum Rand ausfüllen (Rest durch die Ganzzahl-
                // Aufteilung in buildLayoutForOrientation), bliebe sonst auf der Vorderseite der Rand-Rest immer
                // rechts/unten, auf der gespiegelten Rückseite aber weiterhin am Ende des Rasters statt am Rand
                // des Bogens - ein konstanter Versatz über die ganze Seite, der genau das Ausschneide-Problem
                // verursacht. Durch die Spiegelung an sheetW/sheetH landet der Rand-Rest auf der Rückseite
                // automatisch korrekt auf der jeweils anderen Seite.
                if (mirrorForEdge == DuplexEdge.LONG_EDGE) {
                    x = sheetW - x - cellW;
                } else if (mirrorForEdge == DuplexEdge.SHORT_EDGE) {
                    y = sheetH - y - cellH;
                }

                BufferedImage ticketImg = provider.getTicketImage(ticketIndex);

                // Bei "lange Kante" dreht der Duplexdrucker die Rückseite bei den meisten Treibern zusätzlich
                // um 180° (Standardverhalten für Buch-artige Bindung). Die Zellposition (s.o.) stimmt dadurch
                // schon, aber das Ticketmotiv selbst steht auf dem Kopf - deshalb hier zusätzlich drehen.
                int rotationDegrees = layout.rotated ? 90 : 0;
                if (mirrorForEdge == DuplexEdge.LONG_EDGE) {
                    rotationDegrees = (rotationDegrees + 180) % 360;
                }

                if (rotationDegrees == 0) {
                    g2d.drawImage(ticketImg, x, y, cellW, cellH, null);
                } else {
                    drawRotated(g2d, ticketImg, x, y, cellW, cellH, rotationDegrees);
                }
            }
        } finally {
            g2d.dispose();
        }
        return sheet;
    }

    /**
     * Zeichnet das Bild um 90° im Uhrzeigersinn gedreht in die Zelle (x, y, cellW, cellH),
     * ohne vorher eine gedrehte Kopie des Bildes im Speicher anzulegen.
     */
    private static void drawRotated(Graphics2D g2d, BufferedImage src, int x, int y, int cellW, int cellH, int angleDegrees) {
        double srcW = src.getWidth();
        double srcH = src.getHeight();
        boolean swapDimensions = (angleDegrees % 180) != 0; // 90°/270° tauschen Breite und Höhe

        AffineTransform transform = new AffineTransform();
        transform.translate(x + cellW / 2.0, y + cellH / 2.0);
        transform.rotate(Math.toRadians(angleDegrees));
        if (swapDimensions) {
            transform.scale(cellH / srcW, cellW / srcH);
        } else {
            transform.scale(cellW / srcW, cellH / srcH);
        }
        transform.translate(-srcW / 2.0, -srcH / 2.0);

        g2d.drawImage(src, transform, null);
    }
}