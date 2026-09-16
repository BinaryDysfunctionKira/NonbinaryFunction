package com.binary_dysfunction.components;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Component {

    public static ImageIcon geticon(String fileName) {
         return new ImageIcon("nonbinary_function\\src\\main\\resources\\" + fileName);
    }

    public static Icon scaleImage(String path, int size) {
        BufferedImage original;
        try {
            original = ImageIO.read(new File(path));
            if (original == null) {
                throw new IOException("Unsupported or unreadable image format: " + path);
            }
        } catch (IOException e) {
            // System.err.println("Failed to load image at '" + path + "': " + e.getMessage());
            return scaleImage("nonbinary_function\\src\\main\\resources\\BinaryDysfunctionLogo.png", size); // fallback avatar
        }

        int w = original.getWidth();
        int h = original.getHeight();

        int cropSize = Math.min(w, h);
        int x = (w - cropSize) / 2;
        int y = (h - cropSize) / 2;

        BufferedImage cropped = original.getSubimage(x, y, cropSize, cropSize);

        Image scaledImage = cropped.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}
