package com.binary_dysfunction;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class HomeFrame {

    public static JFrame frame;

    public void startup() {


        JPanel mainPanel = new JPanel(new BorderLayout());

        frame = new JFrame("Nonbinary Function - Home");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1013, 608);
        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
