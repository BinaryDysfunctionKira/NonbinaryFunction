package com.binary_dysfunction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.binary_dysfunction.components.Colors;

public class Toast {

    public enum Position { TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT, CENTER }

    public static void show(Window owner, String title, String message, int durationMs, Position position, boolean playSound) {
        SwingUtilities.invokeLater(() -> {
            JWindow window = new JWindow(owner);
            window.setAlwaysOnTop(true);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(30, 30, 30));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            panel.setPreferredSize(new Dimension(230, 80));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(Colors.lighterFontColor);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(titleLabel);

            JLabel label = new JLabel(message);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(label);

            window.setContentPane(panel);
            window.pack();

            // Position on screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension toastSize = window.getSize();
            int margin = 20;
            int x, y;

            switch (position) {
                case TOP_RIGHT:
                    x = screenSize.width - toastSize.width - margin;
                    y = margin;
                    break;
                case TOP_LEFT:
                    x = margin;
                    y = margin;
                    break;
                case BOTTOM_LEFT:
                    x = margin;
                    y = screenSize.height - toastSize.height - margin - 40;
                    break;
                case CENTER:
                    x = (screenSize.width - toastSize.width) / 2;
                    y = (screenSize.height - toastSize.height) / 2;
                    break;
                case BOTTOM_RIGHT:
                default:
                    x = screenSize.width - toastSize.width - margin;
                    y = screenSize.height - toastSize.height - margin - 40;
                    break;
            }
            window.setLocation(x, y);

            // Fade-in / fade-out using opacity
            window.setOpacity(0f);
            window.setVisible(true);

            if (playSound)
                try {
                    playNotificationSound();
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {}

            Timer fadeIn = new Timer(15, null);
            fadeIn.addActionListener(new java.awt.event.ActionListener() {
                float opacity = 0f;
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    opacity += 0.05f;
                    if (opacity >= 1f) {
                        opacity = 1f;
                        fadeIn.stop();
                        scheduleFadeOut(window, durationMs);
                    }
                    window.setOpacity(opacity);
                }
            });
            fadeIn.start();
        });
    }

    private static void scheduleFadeOut(JWindow window, int durationMs) {
        Timer wait = new Timer(durationMs, e -> {
            Timer fadeOut = new Timer(15, null);
            fadeOut.addActionListener(new java.awt.event.ActionListener() {
                float opacity = 1f;
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e2) {
                    opacity -= 0.05f;
                    if (opacity <= 0f) {
                        opacity = 0f;
                        fadeOut.stop();
                        window.dispose();
                    }
                    window.setOpacity(opacity);
                }
            });
            fadeOut.start();
        });
        wait.setRepeats(false);
        wait.start();
    }

    private static void playNotificationSound() throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("nonbinary_function\\src\\main\\resources\\notification-sound.wav"));
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
    }
}