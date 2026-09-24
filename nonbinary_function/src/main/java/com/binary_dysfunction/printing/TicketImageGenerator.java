package com.binary_dysfunction.printing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private static final int RENDER_DPI = 300;

    public static int getRenderWidthPx(double widthCm) {
        return (int) Math.round(widthCm / 2.54 * RENDER_DPI);
    }

    public static int getRenderHeightPx(double heightCm) {
        return (int) Math.round(heightCm / 2.54 * RENDER_DPI);
    }

    public static BufferedImage createBlankWhiteTemplate(double widthCm, double heightCm) {
        BufferedImage image = new BufferedImage(getRenderWidthPx(widthCm), getRenderHeightPx(heightCm), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();
        return image;
    }

    private static class TextLayout {
        Font eventFont, headerFont, infoFont, idFont;
        List<String> eventLines, infoLines;
        int totalTextHeight;
        int lineGap, sectionGap, nameFieldGap;
        int belowInfoHeight; // Name-Feld + Abstände + ID-Zeile (ohne QR-Code selbst)
    }

    public static BufferedImage generateTicketOverlay(BufferedImage template, Ticket ticket, double widthCm, double heightCm) throws WriterException {

        int width = getRenderWidthPx(widthCm);
        int height = getRenderHeightPx(heightCm);

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

        // Mindestgröße, damit der QR-Code noch scanbar bleibt (ca. 1,7 cm bei 21 x 7,4 cm)
        int minQrSize = (int) (contentWidth * 0.5);
        int availableHeight = height - padding * 2;

        TextLayout layout = null;
        for (double scale = 1.0; scale >= 0.3; scale -= 0.03) {
            TextLayout candidate = measureLayout(g2d, ticket, eventName, location, dateFormat, contentWidth, overlayWidth, scale);
            if (candidate.totalTextHeight + candidate.belowInfoHeight + minQrSize <= availableHeight) {
                layout = candidate;
                break;
            }
        }
        if (layout == null) {
            layout = measureLayout(g2d, ticket, eventName, location, dateFormat, contentWidth, overlayWidth, 0.3);
        }

        int y = padding;

        g2d.setColor(Color.WHITE);
        g2d.setFont(layout.eventFont);
        FontMetrics eventMetrics = g2d.getFontMetrics(layout.eventFont);
        for (String line : layout.eventLines) {
            y += eventMetrics.getAscent();
            g2d.drawString(line, textX, y);
            y += eventMetrics.getDescent() + layout.lineGap;
        }
        y += layout.sectionGap;

        g2d.setFont(layout.headerFont);
        y += g2d.getFontMetrics(layout.headerFont).getAscent();
        g2d.drawString("Information", textX, y);
        y += layout.sectionGap;

        g2d.setFont(layout.infoFont);
        g2d.setColor(new Color(210, 210, 210));
        FontMetrics infoMetrics = g2d.getFontMetrics(layout.infoFont);
        for (String line : layout.infoLines) {
            y += infoMetrics.getHeight();
            g2d.drawString(line, textX, y);
        }

        // Name-Feld: Label + weißes Kästchen
        y += infoMetrics.getHeight() + layout.nameFieldGap;
        g2d.setColor(new Color(210, 210, 210));
        g2d.drawString("Name:", textX, y);

        int fieldTop = y + (int) (infoMetrics.getHeight() * 0.35);
        int fieldHeight = (int) (infoMetrics.getHeight() * 1.4);
        int fieldWidth = overlayWidth - padding * 2;
        int fieldX = textX;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(fieldX, fieldTop, fieldWidth, fieldHeight);

        y = fieldTop + fieldHeight + layout.sectionGap;

        FontMetrics idMetrics = g2d.getFontMetrics(layout.idFont);
        int remainingHeight = height - padding - y - layout.sectionGap - idMetrics.getHeight() - layout.lineGap;
        int maxQrSize = (int) (contentWidth * 0.85);
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
        FontMetrics idMetrics = g2d.getFontMetrics(layout.idFont);

        int eventBlockHeight = layout.eventLines.size() * (eventMetrics.getAscent() + eventMetrics.getDescent() + layout.lineGap);
        int headerBlockHeight = headerMetrics.getAscent() + layout.sectionGap;
        int infoBlockHeight = layout.infoLines.size() * infoMetrics.getHeight();

        layout.totalTextHeight = eventBlockHeight + layout.sectionGap + headerBlockHeight + infoBlockHeight;

        // Alles, was unterhalb der Infozeilen noch Platz braucht (außer dem QR-Code selbst):
        // Label-Zeile + Kästchen (1 + 0,35 + 1,4 Zeilenhöhen), Abstände und ID-Zeile
        layout.belowInfoHeight = (int) Math.ceil(infoMetrics.getHeight() * 2.75)
                + layout.nameFieldGap
                + layout.sectionGap * 2
                + layout.lineGap
                + idMetrics.getHeight();

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
                    currentLine = new StringBuilder();
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
        String url = "https://www.google.de/search?q=" + URLEncoder.encode(ticketId, StandardCharsets.UTF_8);
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}