package com.binary_dysfunction.components;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HomeAssemblies extends JScrollPane {

    public HomeAssemblies() {

        JPanel mainpanel = new JPanel(new BorderLayout());
        mainpanel.setBackground(new Color(40, 40, 40));

        this.setViewportView(mainpanel);
        this.setBackground(new Color(40, 40, 40));
        this.setBorder(BorderFactory.createTitledBorder("Ihre Assemblies"));
    }
}
