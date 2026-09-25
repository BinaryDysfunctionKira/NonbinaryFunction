package com.binary_dysfunction.printing;

import java.awt.image.BufferedImage;

import com.google.zxing.WriterException;

/**
 * Beschreibt alle Druckseiten, rendert sie aber erst auf Anfrage (Seite für Seite),
 * damit nicht alle Tickets und Bögen gleichzeitig im Speicher liegen.
 */
public class TicketSheets {

    private final TicketSheetRenderer.Layout layout;
    private final int ticketCount;
    private final TicketSheetRenderer.TicketImageProvider provider;
    private final TicketSheetRenderer.DuplexEdge mirrorForEdge;

    public TicketSheets(double widthCm, double heightCm, int printableWidthPx, int printableHeightPx,
                        int ticketCount, TicketSheetRenderer.TicketImageProvider provider) {
        this(widthCm, heightCm, printableWidthPx, printableHeightPx, ticketCount, provider, null);
    }

    /**
     * @param mirrorForEdge {@code null} für die Vorderseite (keine Spiegelung); für die Rückseite die Kante,
     *                      an der das Blatt beim doppelseitigen Druck gewendet wird, damit Vorder- und
     *                      Rückseite deckungsgleich übereinanderliegen.
     */
    public TicketSheets(double widthCm, double heightCm, int printableWidthPx, int printableHeightPx,
                        int ticketCount, TicketSheetRenderer.TicketImageProvider provider,
                        TicketSheetRenderer.DuplexEdge mirrorForEdge) {
        this.layout = TicketSheetRenderer.computeLayout(widthCm, heightCm, printableWidthPx, printableHeightPx);
        this.ticketCount = ticketCount;
        this.provider = provider;
        this.mirrorForEdge = mirrorForEdge;
    }

    public TicketSheetRenderer.Layout getLayout() {
        return layout;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public int getPageCount() {
        return TicketSheetRenderer.pageCount(layout, ticketCount);
    }

    public boolean isBackSide() {
        return mirrorForEdge != null;
    }

    /** scale 1.0 = volle Auflösung (300 DPI, für den Druck), kleiner = Vorschau. */
    public BufferedImage renderPage(int pageIndex, double scale) throws WriterException {
        return TicketSheetRenderer.renderSheet(layout, pageIndex, ticketCount, provider, scale, mirrorForEdge);
    }
}