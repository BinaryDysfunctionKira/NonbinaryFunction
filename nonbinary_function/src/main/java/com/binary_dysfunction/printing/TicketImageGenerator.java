package com.binary_dysfunction.printing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.binary_dysfunction.types.Ticket;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class TicketImageGenerator {

    public static final double TICKET_WIDTH_CM = 14.0;
    public static final double TICKET_HEIGHT_CM = 5.1;
    private static final int RENDER_DPI = 300;

    public static int getRenderWidthPx() {
        return (int) Math.round(TICKET_WIDTH_CM / 2.54 * RENDER_DPI);
    }

    public static int getRenderHeightPx() {
        return (int) Math.round(TICKET_HEIGHT_CM / 2.54 * RENDER_DPI);
    }

    /** Einfache, weiße Standardvorlage – wird per "cover" ohnehin auf jede Zielgröße skaliert. */
    public static BufferedImage createBlankWhiteTemplate() {
        BufferedImage image = new BufferedImage(getRenderWidthPx(), getRenderHeightPx(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();
        return image;
    }

    private static class TextLayout {
        Font eventFont, headerFont, infoFont, idFont;
        List<String> eventLines, infoLines;
        int totalTextHeight; // Event + Header + Info + Name-Feld, ohne QR-Code
        int lineGap, sectionGap, nameFieldGap;
    }

    public static BufferedImage generateTicketOverlay(BufferedImage template, Ticket ticket) throws WriterException {

        int width = getRenderWidthPx();
        int height = getRenderHeightPx();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int overlayWidth = (int) (width * 0.20);
        int photoWidth = width - overlayWidth;
        int overlayX = photoWidth;

        drawImageCover(g2d, template, 0, 0, photoWidth, height);

        g2d.setColor(new Color(15, 15, 15, 245));
        g2d.fillRect(overlayX, 0, overlayWidth, height);

        int padding = (int) (height * 0.06);
        int textX = overlayX + padding;
        int contentWidth = overlayWidth - padding * 2;

        String eventName = ticket.eventName != null ? ticket.eventName : "";
        String location = ticket.location != null ? ticket.location : "";
        String id = ticket.id != null ? ticket.id : "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        int minQrSize = (int) (contentWidth * 0.25);
        int availableHeight = height - padding * 2;

        // Skalierungsfaktor von groß nach klein durchprobieren, bis Text + Name-Feld + Mindest-QR-Code passen
        TextLayout layout = null;
        for (double scale = 1.0; scale >= 0.3; scale -= 0.03) {
            TextLayout candidate = measureLayout(g2d, ticket, eventName, location, dateFormat, contentWidth, overlayWidth, scale);
            if (candidate.totalTextHeight + candidate.nameFieldGap + minQrSize <= availableHeight) {
                layout = candidate;
                break;
            }
        }
        if (layout == null) {
            layout = measureLayout(g2d, ticket, eventName, location, dateFormat, contentWidth, overlayWidth, 0.3);
        }

        int y = padding;

        // Event-Name
        g2d.setColor(Color.WHITE);
        g2d.setFont(layout.eventFont);
        FontMetrics eventMetrics = g2d.getFontMetrics(layout.eventFont);
        for (String line : layout.eventLines) {
            y += eventMetrics.getAscent();
            g2d.drawString(line, textX, y);
            y += eventMetrics.getDescent() + layout.lineGap;
        }
        y += layout.sectionGap;

        // "Information"-Header
        g2d.setFont(layout.headerFont);
        y += g2d.getFontMetrics(layout.headerFont).getAscent();
        g2d.drawString("Information", textX, y);
        y += layout.sectionGap;

        // Info-Zeilen
        g2d.setFont(layout.infoFont);
        g2d.setColor(new Color(210, 210, 210));
        FontMetrics infoMetrics = g2d.getFontMetrics(layout.infoFont);
        for (String line : layout.infoLines) {
            y += infoMetrics.getHeight();
            g2d.drawString(line, textX, y);
        }

        // Name-Feld: Label + graues Kästchen zum handschriftlichen Ausfüllen
        y += infoMetrics.getHeight() + layout.nameFieldGap;
        g2d.setColor(new Color(210, 210, 210));
        g2d.drawString("Name:", textX, y);

        int fieldTop = y + (int) (infoMetrics.getHeight() * 0.35);
        int fieldHeight = (int) (infoMetrics.getHeight() * 1.4);
        int fieldWidth = overlayWidth - padding * 2;
        int fieldX = textX;

        g2d.setColor(Color.GRAY);
        g2d.fillRect(fieldX, fieldTop, fieldWidth, fieldHeight);

        y = fieldTop + fieldHeight + layout.sectionGap;

        // QR-Code + ID: nimmt den kompletten verbleibenden Platz ein (nie mehr als contentWidth)
        FontMetrics idMetrics = g2d.getFontMetrics(layout.idFont);
        int remainingHeight = height - padding - y - layout.sectionGap - idMetrics.getHeight() - layout.lineGap;
        int maxQrSize = (int) (contentWidth * 0.85); // etwas kleiner als volle Sidebar-Breite
        int qrSize = Math.max(0, Math.min(remainingHeight, maxQrSize));

        if (qrSize > 10) {
            BufferedImage qrCode = generateTicketQrCode(id, qrSize);
            int qrX = overlayX + (overlayWidth - qrSize) / 2;
            int qrY = y + layout.sectionGap;
            g2d.drawImage(qrCode, qrX, qrY, null);

            int idY = qrY + qrSize + layout.lineGap;
            g2d.setFont(layout.idFont);
            g2d.setColor(Color.WHITE);
            String idText = "ID: " + id;
            int idX = overlayX + (overlayWidth - idMetrics.stringWidth(idText)) / 2;
            g2d.drawString(idText, idX, idY + idMetrics.getAscent());
        }

        g2d.dispose();
        return result;
    }

    /** Berechnet Zeilenumbrüche + Gesamthöhe des Textblocks für einen gegebenen Skalierungsfaktor, ohne zu zeichnen. */
    private static TextLayout measureLayout(Graphics2D g2d, Ticket ticket, String eventName, String location,
                                             SimpleDateFormat dateFormat, int contentWidth, int overlayWidth, double scale) {
        TextLayout layout = new TextLayout();

        layout.eventFont = new Font("Arial", Font.BOLD, (int) (overlayWidth * 0.13 * scale));
        layout.headerFont = new Font("Arial", Font.BOLD, (int) (overlayWidth * 0.07 * scale));
        layout.infoFont = new Font("Arial", Font.PLAIN, (int) (overlayWidth * 0.06 * scale));
        layout.idFont = new Font("Arial", Font.PLAIN, (int) (overlayWidth * 0.065 * scale));
        layout.lineGap = (int) (overlayWidth * 0.015 * scale);
        layout.sectionGap = (int) (overlayWidth * 0.025 * scale);
        layout.nameFieldGap = (int) (overlayWidth * 0.06 * scale);

        layout.eventLines = wrapText(g2d, eventName, layout.eventFont, contentWidth);
        layout.infoLines = new ArrayList<>();
        layout.infoLines.add("Datum: " + dateFormat.format(new Date(ticket.date)));
        layout.infoLines.addAll(wrapText(g2d, "Ort: " + location, layout.infoFont, contentWidth));
        layout.infoLines.add("Preis: " + ticket.price + " €");

        FontMetrics eventMetrics = g2d.getFontMetrics(layout.eventFont);
        FontMetrics headerMetrics = g2d.getFontMetrics(layout.headerFont);
        FontMetrics infoMetrics = g2d.getFontMetrics(layout.infoFont);

        int eventBlockHeight = layout.eventLines.size() * (eventMetrics.getAscent() + eventMetrics.getDescent() + layout.lineGap);
        int headerBlockHeight = headerMetrics.getAscent() + layout.sectionGap;
        int infoBlockHeight = layout.infoLines.size() * infoMetrics.getHeight();

        layout.totalTextHeight = eventBlockHeight + layout.sectionGap + headerBlockHeight + infoBlockHeight;

        return layout;
    }

    private static void drawImageCover(Graphics2D g2d, BufferedImage image, int x, int y, int targetWidth, int targetHeight) {
        double scale = Math.max(
                (double) targetWidth / image.getWidth(),
                (double) targetHeight / image.getHeight()
        );
        int drawWidth = (int) Math.ceil(image.getWidth() * scale);
        int drawHeight = (int) Math.ceil(image.getHeight() * scale);
        int offsetX = x + (targetWidth - drawWidth) / 2;
        int offsetY = y + (targetHeight - drawHeight) / 2;

        Shape oldClip = g2d.getClip();
        g2d.setClip(new Rectangle2D.Double(x, y, targetWidth, targetHeight));
        g2d.drawImage(image, offsetX, offsetY, drawWidth, drawHeight, null);
        g2d.setClip(oldClip);
    }

    private static List<String> wrapText(Graphics2D g2d, String text, Font font, int maxWidth) {
        FontMetrics metrics = g2d.getFontMetrics(font);
        List<String> lines = new ArrayList<>();

        if (metrics.stringWidth(text) <= maxWidth) {
            lines.add(text);
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (metrics.stringWidth(candidate) <= maxWidth) {
                currentLine = new StringBuilder(candidate);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    // currentLine = new StringBuilder();
                }
                if (metrics.stringWidth(word) > maxWidth) {
                    StringBuilder chunk = new StringBuilder();
                    for (char c : word.toCharArray()) {
                        if (metrics.stringWidth(chunk.toString() + c) > maxWidth && !chunk.isEmpty()) {
                            lines.add(chunk.toString());
                            chunk = new StringBuilder();
                        }
                        chunk.append(c);
                    }
                    currentLine = chunk;
                } else {
                    currentLine = new StringBuilder(word);
                }
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());

        return lines;
    }

    private static BufferedImage generateTicketQrCode(String ticketId, int size) throws WriterException {
        String url = "https://www.google.de/search?q=" + ticketId;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}