package com.binary_dysfunction.printing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.List;

public class TicketPrintable implements Printable {

    private final List<BufferedImage> sheets;

    public TicketPrintable(List<BufferedImage> sheets) {
        this.sheets = sheets;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex >= sheets.size()) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) graphics;
        BufferedImage sheet = sheets.get(pageIndex);

        double scale = Math.min(
                pageFormat.getImageableWidth() / sheet.getWidth(),
                pageFormat.getImageableHeight() / sheet.getHeight()
        );

        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.scale(scale, scale);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(sheet, 0, 0, null);

        return PAGE_EXISTS;
    }
}