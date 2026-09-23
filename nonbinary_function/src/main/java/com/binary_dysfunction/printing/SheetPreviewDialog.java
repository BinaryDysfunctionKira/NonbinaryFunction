package com.binary_dysfunction.printing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class SheetPreviewDialog extends JDialog {

    private final List<BufferedImage> sheets;
    private int currentIndex = 0;
    private final JPanel sheetPanel;
    private final JLabel pageLabel;

    public SheetPreviewDialog(JFrame owner, List<BufferedImage> sheets) {
        super(owner, "A4-Vorschau", true);
        this.sheets = sheets;

        setLayout(new BorderLayout());

        sheetPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage sheet = sheets.get(currentIndex);
                double scale = Math.min(
                        (double) getWidth() / sheet.getWidth(),
                        (double) getHeight() / sheet.getHeight()
                );
                int w = (int) (sheet.getWidth() * scale);
                int h = (int) (sheet.getHeight() * scale);
                g.drawImage(sheet, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            }
        };
        sheetPanel.setBackground(Color.GRAY);
        sheetPanel.setPreferredSize(new Dimension(700, 900));

        JButton prevButton = new JButton("< Vorherige");
        JButton nextButton = new JButton("Nächste >");
        pageLabel = new JLabel();
        updatePageLabel();

        prevButton.addActionListener(e -> {
            if (currentIndex > 0) { currentIndex--; sheetPanel.repaint(); updatePageLabel(); }
        });
        nextButton.addActionListener(e -> {
            if (currentIndex < sheets.size() - 1) { currentIndex++; sheetPanel.repaint(); updatePageLabel(); }
        });

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navPanel.add(prevButton);
        navPanel.add(pageLabel);
        navPanel.add(nextButton);

        add(sheetPanel, BorderLayout.CENTER);
        add(navPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void updatePageLabel() {
        pageLabel.setText("Seite " + (currentIndex + 1) + " / " + sheets.size());
    }
}