package com.binary_dysfunction.printing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TicketSheetRenderer {

    public static final int DPI = 300;
    public static final int A4_WIDTH_PX = (int) (8.27 * DPI);
    public static final int A4_HEIGHT_PX = (int) (11.69 * DPI);
    private static final int MARGIN_PX = (int) (0.4 * DPI);
    private static final int GAP_PX = (int) (0.15 * DPI);

    public static class Layout {
        public final int columns, rows, itemsPerPage, ticketWidthPx, ticketHeightPx;
        Layout(int columns, int rows, int ticketWidthPx, int ticketHeightPx) {
            this.columns = columns;
            this.rows = rows;
            this.itemsPerPage = columns * rows;
            this.ticketWidthPx = ticketWidthPx;
            this.ticketHeightPx = ticketHeightPx;
        }
    }

    public static Layout computeLayout() {
        int ticketWidthPx = (int) Math.round(TicketImageGenerator.TICKET_WIDTH_CM / 2.54 * DPI);
        int ticketHeightPx = (int) Math.round(TicketImageGenerator.TICKET_HEIGHT_CM / 2.54 * DPI);

        int printableWidth = A4_WIDTH_PX - MARGIN_PX * 2;
        int printableHeight = A4_HEIGHT_PX - MARGIN_PX * 2;

        int columns = Math.max(1, (printableWidth + GAP_PX) / (ticketWidthPx + GAP_PX));
        int rows = Math.max(1, (printableHeight + GAP_PX) / (ticketHeightPx + GAP_PX));

        return new Layout(columns, rows, ticketWidthPx, ticketHeightPx);
    }

    public static List<BufferedImage> renderSheets(List<BufferedImage> tickets) {
        Layout layout = computeLayout();
        List<BufferedImage> sheets = new ArrayList<>();
        int pageCount = (int) Math.ceil((double) tickets.size() / layout.itemsPerPage);
        for (int page = 0; page < pageCount; page++) {
            sheets.add(renderSheet(tickets, layout, page));
        }
        return sheets;
    }

    private static BufferedImage renderSheet(List<BufferedImage> tickets, Layout layout, int pageIndex) {
        BufferedImage sheet = new BufferedImage(A4_WIDTH_PX, A4_HEIGHT_PX, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = sheet.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, A4_WIDTH_PX, A4_HEIGHT_PX);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int startIndex = pageIndex * layout.itemsPerPage;
        for (int i = 0; i < layout.itemsPerPage; i++) {
            int ticketIndex = startIndex + i;
            if (ticketIndex >= tickets.size()) break;

            int col = i % layout.columns;
            int row = i / layout.columns;
            int x = MARGIN_PX + col * (layout.ticketWidthPx + GAP_PX);
            int y = MARGIN_PX + row * (layout.ticketHeightPx + GAP_PX);

            g2d.drawImage(tickets.get(ticketIndex), x, y, layout.ticketWidthPx, layout.ticketHeightPx, null);
        }
        g2d.dispose();
        return sheet;
    }
}