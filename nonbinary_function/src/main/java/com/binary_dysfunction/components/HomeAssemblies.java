package com.binary_dysfunction.components;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HomeAssemblies extends JScrollPane {

    public HomeAssemblies() {

        JPanel mainpanel = new JPanel(new BorderLayout());
        mainpanel.setBackground(Colors.backgroundColor);

        this.setViewportView(mainpanel);
        this.setBackground(Colors.backgroundColor);
        this.setBorder(BorderFactory.createTitledBorder("Ihre Assemblies"));
    }
}
