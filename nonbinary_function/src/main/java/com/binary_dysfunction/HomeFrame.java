package com.binary_dysfunction;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.binary_dysfunction.components.SideBar;
import com.binary_dysfunction.components.TopBar;

public class HomeFrame {

    public static JFrame frame;
    public JPanel contentPanel = new JPanel(new BorderLayout());
    public TopBar topBar;

    @SuppressWarnings("FieldMayBeFinal")
    private static String programName = "Nonbinary Function";

    public void startup() {

        Main.updater = new Updater();

        frame = new JFrame(programName + " - Home");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1013, 608);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        topBar = new TopBar(this);
        SideBar sideBar = new SideBar(this);

        contentPanel.add(new HomePanel(this));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(sideBar, BorderLayout.WEST);
        mainPanel.add(contentPanel);

        frame.getContentPane().add(mainPanel);
    }

    public void setHomePanel() {
        contentPanel.removeAll();
        contentPanel.add(new HomePanel(this));
        frame.setTitle(programName + " - Home");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setProfilePanel() {
        contentPanel.removeAll();
        contentPanel.add(new ProfilePanel(this));
        frame.setTitle(programName + " - Profil");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setAdminPanel() {
        contentPanel.removeAll();
        contentPanel.add(new AdminPanel(this));
        frame.setTitle(programName + " - Admin Zone");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setCalendarPanel() {
        contentPanel.removeAll();
        contentPanel.add(new CalendarPanel());
        frame.setTitle(programName + " - Kalendar");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setChatPanel() {
        contentPanel.removeAll();
        contentPanel.add(new ChatPanel(this));
        frame.setTitle(programName + " - Chat");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
