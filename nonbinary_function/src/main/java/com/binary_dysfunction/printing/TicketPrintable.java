package com.binary_dysfunction.printing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Druckt bereits fertig erzeugte Seiten (als Bilddateien) - pro Seite wird nur eine Datei in den Speicher geladen.
 * Einträge mit null sind Seiten außerhalb des gewählten Druckbereichs; sie werden nicht gerendert.
 * Für den doppelseitigen Druck enthält die Dateiliste Vorder- und Rückseiten abwechselnd
 * (Vorne 1, Hinten 1, Vorne 2, Hinten 2, ...) - diese Klasse selbst kennt den Unterschied nicht,
 * sie spielt einfach die übergebenen Seiten in Reihenfolge ab.
 */
public class TicketPrintable implements Printable {

    private final List<File> sheetFiles;

    // Java ruft print() teils mehrfach für dieselbe Seite auf -> nur die zuletzt geladene Seite merken
    private int cachedPageIndex = -1;
    private BufferedImage cachedSheet;

    public TicketPrintable(List<File> sheetFiles) {
        this.sheetFiles = sheetFiles;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex < 0 || pageIndex >= sheetFiles.size()) return NO_SUCH_PAGE;

        // Seite außerhalb des gewählten Bereichs: wird normalerweise nie angefordert, dann lieber leer als Absturz
        if (sheetFiles.get(pageIndex) == null) return PAGE_EXISTS;

        BufferedImage sheet;
        try {
            sheet = getSheet(pageIndex);
        } catch (IOException ex) {
            throw new PrinterException("Seite " + (pageIndex + 1) + " konnte nicht geladen werden: " + ex.getMessage());
        }

        Graphics2D g2d = (Graphics2D) graphics;

        // Sheet wurde bereits exakt auf den bedruckbaren Bereich (in Pixeln bei DPI) zugeschnitten -
        // daher hier nur noch von Pixel (DPI) zu Punkt (72/Zoll) umrechnen, nicht nochmal separat verschieben.
        double scale = pageFormat.getImageableWidth() / sheet.getWidth();

        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.scale(scale, scale);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(sheet, 0, 0, null);

        return PAGE_EXISTS;
    }

    private synchronized BufferedImage getSheet(int pageIndex) throws IOException {
        if (cachedSheet == null || cachedPageIndex != pageIndex) {
            cachedSheet = null; // alte Seite freigeben, bevor die neue geladen wird
            BufferedImage image = ImageIO.read(sheetFiles.get(pageIndex));
            if (image == null) {
                throw new IOException("Datei ist kein lesbares Bild: " + sheetFiles.get(pageIndex).getName());
            }
            cachedSheet = image;
            cachedPageIndex = pageIndex;
        }
        return cachedSheet;
    }
}