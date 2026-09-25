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

    // Deckkraft des dunklen Scrims auf der Rückseite, wenn dort ein eigenes Hintergrundbild verwendet wird
    // (Text bleibt so immer lesbar, egal wie hell/dunkel das Bild ist)
    private static final int BACK_IMAGE_SCRIM_ALPHA = 165;

    // Referenzgröße ("Konzertticket"), auf die die Rückseiten-Schriftgrößen kalibriert sind - siehe backFontBasisWidthPx().
    private static final double REFERENCE_WIDTH_CM = 21.0;
    private static final double REFERENCE_HEIGHT_CM = 7.4;
    private static final double REFERENCE_TEXT_COLUMN_FRACTION = 0.54; // muss zur Spaltenbreite in drawBackInfoPanel passen
    private static final double BACK_SIZE_FACTOR_MIN = 0.5;
    private static final double BACK_SIZE_FACTOR_MAX = 2.0;

    public static int getRenderWidthPx(double widthCm) {
        return (int) Math.round(widthCm / 2.54 * RENDER_DPI);
    }

    public static int getRenderHeightPx(double heightCm) {
        return (int) Math.round(heightCm / 2.54 * RENDER_DPI);
    }

    /**
     * Liefert die Breite, die measureLayout() für die Rückseite als Grundlage für die Schrift- und
     * Abstandsgrößen bekommt - bewusst NICHT die tatsächliche Panelbreite des Tickets, sondern die Breite
     * des Referenztickets ("Konzertticket", 21 x 7,4 cm), skaliert mit dem Flächenverhältnis (dessen Wurzel,
     * also gedämpft) zwischen Referenz- und tatsächlicher Ticketgröße.
     * <p>
     * Wichtig: Würde man stattdessen einfach die echte (mit der Ticketbreite wachsende) Panelbreite mit
     * diesem Faktor multiplizieren, würde sich der Größeneffekt bei reinem Breitenwachstum großteils selbst
     * aufheben (Panelbreite wächst linear mit der Breite, der Flächenfaktor schrumpft mit der Wurzel daraus) -
     * ein größeres Ticket hätte dann kaum kleinere Schrift. Durch die Entkopplung von der echten Panelbreite
     * bleibt der Effekt erhalten: ein größeres Ticket (mehr Fläche) bekommt eine kleinere Basisbreite und
     * damit spürbar kleinere Schrift, ein kleineres Ticket eine größere. Nach oben/unten geclampt, damit es
     * bei sehr kleinen bzw. sehr großen benutzerdefinierten Maßen nicht unleserlich klein bzw. unnötig riesig
     * wird. Der bestehende "shrink to fit"-Mechanismus (siehe Aufrufer) sorgt zusätzlich dafür, dass der Text
     * auf sehr kleinen Tickets trotzdem in die verfügbare Höhe passt.
     */
    private static int backFontBasisWidthPx(int ticketWidthPx, int ticketHeightPx) {
        double referenceAreaPx = (double) getRenderWidthPx(REFERENCE_WIDTH_CM) * getRenderHeightPx(REFERENCE_HEIGHT_CM);
        double actualAreaPx = Math.max(1.0, (double) ticketWidthPx * ticketHeightPx);
        double factor = Math.sqrt(referenceAreaPx / actualAreaPx);
        factor = Math.max(BACK_SIZE_FACTOR_MIN, Math.min(BACK_SIZE_FACTOR_MAX, factor));

        double referenceTextColumnWidthPx = getRenderWidthPx(REFERENCE_WIDTH_CM) * REFERENCE_TEXT_COLUMN_FRACTION;
        return Math.max(1, (int) Math.round(referenceTextColumnWidthPx * factor));
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

    // ------------------------------------------------------------------
    // Vorderseite (Foto + schmaler Informationsstreifen) - unverändertes Layout
    // ------------------------------------------------------------------

    public static BufferedImage generateTicketOverlay(BufferedImage template, Ticket ticket, double widthCm, double heightCm) throws WriterException {
        return generateTicketOverlay(template, ticket, widthCm, heightCm, true);
    }

    /**
     * @param includeNameField ob das "Name:"-Feld (Label + weißes Kästchen) mit gezeichnet wird. Bei doppelseitigem
     *                         Druck genügt das Namensfeld einmal (auf der Rückseite); auf der Vorderseite kann es
     *                         dann entfallen, damit es nicht doppelt erscheint.
     */
    public static BufferedImage generateTicketOverlay(BufferedImage template, Ticket ticket, double widthCm, double heightCm,
                                                        boolean includeNameField) throws WriterException {

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

        drawInfoPanel(g2d, ticket, overlayX, overlayWidth, height, false, includeNameField);

        g2d.dispose();
        return result;
    }

    // ------------------------------------------------------------------
    // Rückseite: standardmäßig schwarz (optional mit eigener Vorlage / weichgezeichnet), zeigt links alle
    // Ticketinformationen groß und deutlich an, rechts einen großen, gut lesbaren QR-Code.
    // ------------------------------------------------------------------

    /**
     * Erzeugt die Rückseite eines Tickets.
     *
     * @param backTemplate optionale Hintergrundvorlage für die Rückseite; {@code null} = einfarbig schwarz (Standard)
     * @param blurry       wenn true, wird der Hintergrund weichgezeichnet (bei reinem Schwarz sorgt das für einen
     *                     sanften, leicht strukturierten Verlauf statt einer flachen Fläche)
     */
    public static BufferedImage generateTicketBack(BufferedImage backTemplate, boolean blurry, Ticket ticket,
                                                     double widthCm, double heightCm) throws WriterException {

        int width = getRenderWidthPx(widthCm);
        int height = getRenderHeightPx(heightCm);

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (backTemplate != null) {
            BufferedImage background = blurry ? blurImage(backTemplate, width, height) : backTemplate;
            drawImageCover(g2d, background, 0, 0, width, height);
            // Dunkles Scrim, damit der weiße Text immer lesbar bleibt, egal wie hell die Vorlage ist
            g2d.setColor(new Color(0, 0, 0, BACK_IMAGE_SCRIM_ALPHA));
            g2d.fillRect(0, 0, width, height);
        } else if (blurry) {
            drawBlurrySolidBackground(g2d, width, height);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
        }

        drawBackInfoPanel(g2d, ticket, width, height);

        g2d.dispose();
        return result;
    }

    /** Weicher, leicht strukturierter Schwarzverlauf (statt einer komplett flachen Fläche) für die "weichgezeichnete" Rückseite. */
    private static void drawBlurrySolidBackground(Graphics2D g2d, int width, int height) {
        BufferedImage seed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D sg = seed.createGraphics();
        sg.setColor(new Color(8, 8, 8));
        sg.fillRect(0, 0, width, height);
        sg.setColor(new Color(55, 55, 60));
        sg.fillOval(-width / 4, -height / 2, width, height * 2);
        sg.dispose();

        BufferedImage blurred = blurImage(seed, width, height);
        g2d.drawImage(blurred, 0, 0, null);
    }

    /** Schneller Weichzeichner: Bild stark verkleinern und wieder bilinear hochskalieren erzeugt einen sauberen Blur-Effekt. */
    private static BufferedImage blurImage(BufferedImage src, int targetWidth, int targetHeight) {
        int smallW = Math.max(1, targetWidth / 40);
        int smallH = Math.max(1, targetHeight / 40);

        BufferedImage small = new BufferedImage(smallW, smallH, BufferedImage.TYPE_INT_RGB);
        Graphics2D sg = small.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        drawImageCover(sg, src, 0, 0, smallW, smallH);
        sg.dispose();

        BufferedImage result = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D rg = result.createGraphics();
        rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        rg.drawImage(small, 0, 0, targetWidth, targetHeight, null);
        rg.dispose();
        return result;
    }

    // ------------------------------------------------------------------
    // Gemeinsames Infopanel (Eventname, Informationen, Name-Feld, QR-Code + ID)
    // panelX/panelWidth bestimmen den Bereich; bei centered=true wird jede Zeile horizontal zentriert
    // (Rückseite), sonst linksbündig (Vorderseite).
    // ------------------------------------------------------------------

    private static void drawInfoPanel(Graphics2D g2d, Ticket ticket, int panelX, int panelWidth, int height,
                                       boolean centered, boolean includeNameField) throws WriterException {

        int padding = (int) (height * 0.06);
        int textX = panelX + padding;
        int contentWidth = panelWidth - padding * 2;

        String eventName = ticket.eventName != null ? ticket.eventName : "";
        String location = ticket.location != null ? ticket.location : "";
        String id = ticket.id != null ? ticket.id : "";
        // Datum und Uhrzeit stehen in einem gemeinsamen Format, damit die Uhrzeit direkt neben dem Datum steht
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm 'Uhr'");

        // Mindestgröße, damit der QR-Code noch scanbar bleibt (ca. 1,7 cm bei 21 x 7,4 cm)
        int minQrSize = (int) (contentWidth * 0.5);
        int availableHeight = height - padding * 2;

        TextLayout layout = null;
        for (double scale = 1.0; scale >= 0.3; scale -= 0.03) {
            TextLayout candidate = measureLayout(g2d, ticket, eventName, location, dateTimeFormat, contentWidth, panelWidth, scale);
            if (candidate.totalTextHeight + candidate.belowInfoHeight + minQrSize <= availableHeight) {
                layout = candidate;
                break;
            }
        }
        if (layout == null) {
            layout = measureLayout(g2d, ticket, eventName, location, dateTimeFormat, contentWidth, panelWidth, 0.3);
        }

        int y = padding;

        g2d.setColor(Color.WHITE);
        g2d.setFont(layout.eventFont);
        FontMetrics eventMetrics = g2d.getFontMetrics(layout.eventFont);
        for (String line : layout.eventLines) {
            y += eventMetrics.getAscent();
            drawLine(g2d, line, textX, y, contentWidth, eventMetrics, centered);
            y += eventMetrics.getDescent() + layout.lineGap;
        }
        y += layout.sectionGap;

        g2d.setFont(layout.headerFont);
        y += g2d.getFontMetrics(layout.headerFont).getAscent();
        drawLine(g2d, "Information", textX, y, contentWidth, g2d.getFontMetrics(layout.headerFont), centered);
        y += layout.sectionGap;

        g2d.setFont(layout.infoFont);
        g2d.setColor(new Color(210, 210, 210));
        FontMetrics infoMetrics = g2d.getFontMetrics(layout.infoFont);
        for (String line : layout.infoLines) {
            y += infoMetrics.getHeight();
            drawLine(g2d, line, textX, y, contentWidth, infoMetrics, centered);
        }

        // Name-Feld: Label + weißes Kästchen (nur wenn gewünscht - z. B. nicht auf der Vorderseite beim
        // doppelseitigen Druck, wo das Namensfeld bereits auf der Rückseite steht). Die Platzberechnung
        // (fieldTop/fieldHeight/y) bleibt bewusst unverändert, egal ob gezeichnet wird oder nicht, damit
        // Layout und QR-Code-Größe unabhängig von dieser Einstellung identisch bleiben.
        y += infoMetrics.getHeight() + layout.nameFieldGap;
        if (includeNameField) {
            g2d.setColor(new Color(210, 210, 210));
            drawLine(g2d, "Name:", textX, y, contentWidth, infoMetrics, centered);
        }

        int fieldTop = y + (int) (infoMetrics.getHeight() * 0.35);
        int fieldHeight = (int) (infoMetrics.getHeight() * 1.4);
        int fieldWidth = centered ? (int) (contentWidth * 0.7) : panelWidth - padding * 2;
        int fieldX = centered ? panelX + (panelWidth - fieldWidth) / 2 : textX;

        if (includeNameField) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(fieldX, fieldTop, fieldWidth, fieldHeight);
        }

        y = fieldTop + fieldHeight + layout.sectionGap;

        FontMetrics idMetrics = g2d.getFontMetrics(layout.idFont);
        int remainingHeight = height - padding - y - layout.sectionGap - idMetrics.getHeight() - layout.lineGap;
        int maxQrSize = (int) (contentWidth * 0.85);
        int qrSize = Math.max(0, Math.min(remainingHeight, maxQrSize));

        if (qrSize > 10) {
            BufferedImage qrCode = generateTicketQrCode(id, qrSize);
            int qrX = panelX + (panelWidth - qrSize) / 2;
            int qrY = y + layout.sectionGap;
            g2d.drawImage(qrCode, qrX, qrY, null);

            int idY = qrY + qrSize + layout.lineGap;
            g2d.setFont(layout.idFont);
            g2d.setColor(Color.WHITE);
            String idText = "ID: " + id;
            int idX = panelX + (panelWidth - idMetrics.stringWidth(idText)) / 2;
            g2d.drawString(idText, idX, idY + idMetrics.getAscent());
        }
    }

    private static void drawLine(Graphics2D g2d, String line, int leftX, int baselineY, int contentWidth,
                                  FontMetrics metrics, boolean centered) {
        int x = leftX;
        if (centered) {
            x = leftX + Math.max(0, (contentWidth - metrics.stringWidth(line)) / 2);
        }
        g2d.drawString(line, x, baselineY);
    }

    // ------------------------------------------------------------------
    // Rückseiten-Layout: Text (Eventname, Informationen, Name-Feld) links, großer, gut lesbarer QR-Code rechts.
    // Der QR-Code darf die ganze verfügbare Höhe ausnutzen, statt wie auf der Vorderseite unter dem Text zu stehen.
    // ------------------------------------------------------------------

    private static void drawBackInfoPanel(Graphics2D g2d, Ticket ticket, int width, int height) throws WriterException {
        int paddingY = (int) (height * 0.07);
        int paddingX = (int) (width * 0.035);
        int gap = (int) (width * 0.03);

        int textColumnWidth = (int) (width * 0.54);
        int textX = paddingX;
        int contentWidth = textColumnWidth - paddingX;

        String eventName = ticket.eventName != null ? ticket.eventName : "";
        String location = ticket.location != null ? ticket.location : "";
        String id = ticket.id != null ? ticket.id : "";
        // Datum und Uhrzeit stehen in einem gemeinsamen Format, damit die Uhrzeit direkt neben dem Datum steht
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm 'Uhr'");

        int availableHeight = height - paddingY * 2;

        // Anders als auf der Vorderseite muss hier kein Platz für den QR-Code freigehalten werden -
        // der steht in der eigenen, rechten Spalte.
        // fontBasisWidth (statt der echten textColumnWidth) treibt die Schriftgröße: kleinere Tickets bekommen
        // dadurch spürbar größere, größere Tickets spürbar kleinere Schrift (siehe backFontBasisWidthPx()).
        // Für das Zeilenumbruch (wrapText) wird weiterhin die echte contentWidth verwendet, damit der Text
        // tatsächlich in die vorhandene Breite passt.
        int fontBasisWidth = backFontBasisWidthPx(width, height);
        TextLayout layout = null;
        for (double scale = 1.0; scale >= 0.3; scale -= 0.03) {
            TextLayout candidate = measureLayout(g2d, ticket, eventName, location, dateTimeFormat, contentWidth, fontBasisWidth, scale);
            if (candidate.totalTextHeight + candidate.belowInfoHeight <= availableHeight) {
                layout = candidate;
                break;
            }
        }
        if (layout == null) {
            layout = measureLayout(g2d, ticket, eventName, location, dateTimeFormat, contentWidth, fontBasisWidth, 0.3);
        }

        int y = paddingY;

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
        int fieldWidth = contentWidth;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(textX, fieldTop, fieldWidth, fieldHeight);

        // ---- Rechte Spalte: großer, gut lesbarer QR-Code (vertikal zentriert über die ganze Ticket-Höhe) ----
        int qrColumnX = paddingX + textColumnWidth + gap;
        int qrColumnWidth = width - qrColumnX - paddingX;

        FontMetrics idMetrics = g2d.getFontMetrics(layout.idFont);
        int maxQrByWidth = qrColumnWidth;
        int maxQrByHeight = availableHeight - idMetrics.getHeight() - layout.lineGap;
        int qrSize = Math.max(0, Math.min(maxQrByWidth, maxQrByHeight));

        if (qrSize > 10) {
            BufferedImage qrCode = generateTicketQrCode(id, qrSize);
            int blockHeight = qrSize + layout.lineGap + idMetrics.getHeight();
            int blockTop = paddingY + Math.max(0, (availableHeight - blockHeight) / 2);
            int qrX = qrColumnX + (qrColumnWidth - qrSize) / 2;

            g2d.drawImage(qrCode, qrX, blockTop, null);

            int idY = blockTop + qrSize + layout.lineGap;
            g2d.setFont(layout.idFont);
            g2d.setColor(Color.WHITE);
            String idText = "ID: " + id;
            int idX = qrColumnX + (qrColumnWidth - idMetrics.stringWidth(idText)) / 2;
            g2d.drawString(idText, idX, idY + idMetrics.getAscent());
        }
    }

    private static TextLayout measureLayout(Graphics2D g2d, Ticket ticket, String eventName, String location,
                                             SimpleDateFormat dateTimeFormat, int contentWidth, int panelWidth, double scale) {
        TextLayout layout = new TextLayout();

        layout.eventFont = new Font("Arial", Font.BOLD, (int) (panelWidth * 0.13 * scale));
        layout.headerFont = new Font("Arial", Font.BOLD, (int) (panelWidth * 0.07 * scale));
        layout.infoFont = new Font("Arial", Font.PLAIN, (int) (panelWidth * 0.06 * scale));
        layout.idFont = new Font("Arial", Font.PLAIN, (int) (panelWidth * 0.065 * scale));
        layout.lineGap = (int) (panelWidth * 0.015 * scale);
        layout.sectionGap = (int) (panelWidth * 0.025 * scale);
        layout.nameFieldGap = (int) (panelWidth * 0.06 * scale);

        layout.eventLines = wrapText(g2d, eventName, layout.eventFont, contentWidth);
        layout.infoLines = new ArrayList<>();
        // Datum + Uhrzeit zusammen in einer Zeile ("Datum: dd.MM.yyyy, HH:mm Uhr")
        layout.infoLines.addAll(wrapText(g2d, "Datum: " + dateTimeFormat.format(new Date(ticket.date)), layout.infoFont, contentWidth));
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