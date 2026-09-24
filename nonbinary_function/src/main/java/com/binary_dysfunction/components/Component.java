package com.binary_dysfunction.components;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.binary_dysfunction.main.Main;

public class Component {

    public static ImageIcon geticon(String path) {
        // try {
            return new ImageIcon(Component.class.getResource(path));
        // } catch (Exception e) {
        //     System.out.println("Error");
        // }
        // return null;
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
            return scaleImage(Main.defaultImage, size); // fallback avatar
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

    public static Icon scaleImage(ImageIcon imageIcon, int size) {
        BufferedImage original = new BufferedImage(size, size,  BufferedImage.OPAQUE);
        try {
            Image tmpImage = imageIcon.getImage();
            original = (BufferedImage) tmpImage;
            if (original == null) {
                throw new IOException("Unsupported or unreadable image format: " + imageIcon);
            }
        } catch (IOException e) {
            // System.err.println("Failed to load image at '" + path + "': " + e.getMessage());
            // return scaleIcon(Component.class.getResource("/BinaryDysfunctionLogo.png"), size); // fallback avatar
            try {
                original = ImageIO.read(Component.class.getResource("/BinaryDysfunctionLogo.png"));
            } catch (IOException e1) {}
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

    public static Icon resizeImageProportional(ImageIcon imageIcon, int width, int height) {
        BufferedImage original = toBufferedImage(imageIcon.getImage());

        if (original == null || original.getWidth() <= 0 || original.getHeight() <= 0) {
            try {
                original = ImageIO.read(Component.class.getResource("/BinaryDysfunctionLogo.png"));
            } catch (IOException e1) {
                original = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
        }

        int w = original.getWidth();
        int h = original.getHeight();

        double scale = Math.min((double) width / w, (double) height / h);
        int targetWidth = (int) Math.round(w * scale);
        int targetHeight = (int) Math.round(h * scale);

        Image scaledImage = original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }

        BufferedImage buffered = new BufferedImage(
                image.getWidth(null) > 0 ? image.getWidth(null) : 1,
                image.getHeight(null) > 0 ? image.getHeight(null) : 1,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return buffered;
    }
}
