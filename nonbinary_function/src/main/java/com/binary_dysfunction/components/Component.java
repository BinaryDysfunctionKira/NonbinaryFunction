package com.binary_dysfunction.components;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Component {

    public static ImageIcon geticon(String fileName) {
         return new ImageIcon("nonbinary_function\\src\\main\\resources\\" + fileName);
    }

    public static Icon scaleImage(String path, int size) {
        ImageIcon originalImage = new ImageIcon(path);
        Image scaledImage = originalImage.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}
