package com.binary_dysfunction;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

    public static TrayIcon trayIcon;

    public void startup() {

        Main.updater = new Updater();

        frame = new JFrame(programName + " - Home");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1013, 608);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(Main.appIcon);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                
                setHomePanel();
                frame.setVisible(false); // Fenster unsichtbar machen
            }
        });
        if (SystemTray.isSupported()) {
            setupTrayIcon();
        } else {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fallback
        }
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
        openFrame();
        ChatPanel.currentTargetUser = null;
        contentPanel.removeAll();
        contentPanel.add(new HomePanel(this));
        frame.setTitle(programName + " - Home");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setProfilePanel() {
        openFrame();
        ChatPanel.currentTargetUser = null;
        contentPanel.removeAll();
        contentPanel.add(new ProfilePanel(this));
        frame.setTitle(programName + " - Profil");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setAdminPanel() {
        ChatPanel.currentTargetUser = null;
        contentPanel.removeAll();
        contentPanel.add(new AdminPanel(this));
        frame.setTitle(programName + " - Admin Zone");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setCalendarPanel() {
        ChatPanel.currentTargetUser = null;
        contentPanel.removeAll();
        contentPanel.add(new CalendarPanel());
        frame.setTitle(programName + " - Kalendar");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setChatPanel() {
        openFrame();
        contentPanel.removeAll();
        contentPanel.add(new ChatPanel(this));
        frame.setTitle(programName + " - Chat");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setEventPanel() {
        openFrame();
        contentPanel.removeAll();
        contentPanel.add(new EventPanel(this));
        frame.setTitle(programName + " - Event Funktionen");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    public void setAddTicketPanel() {
        openFrame();
        contentPanel.removeAll();
        contentPanel.add(new AddTicketPanel(this));
        frame.setTitle(programName + " - Tickets Hinzufügen");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    public void setConfigureTicketPanel() {
        openFrame();
        contentPanel.removeAll();
        contentPanel.add(new ConfigureTicketPanel(this));
        frame.setTitle(programName + " - Tickets Bearbeiten");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    public void setRegisterTicketPanel() {
        openFrame();
        contentPanel.removeAll();
        contentPanel.add(new RegisterTicketPanel(this));
        frame.setTitle(programName + " - Tickets Registrieren");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    public void setStatisticsPanel() {
        openFrame();
        contentPanel.removeAll();
        contentPanel.add(new StatisticPanel(this));
        frame.setTitle(programName + " - Statistik");
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void setupTrayIcon() {
        SystemTray tray = SystemTray.getSystemTray();

        Image image = Main.appIcon;

        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Öffnen");
        MenuItem homeItem = new MenuItem("Home");
        MenuItem profileItem = new MenuItem(Main.loggedInAccount.username);
        MenuItem chatItem = new MenuItem("Nachrichten");
        MenuItem logoutItem = new MenuItem("Abmelden");
        MenuItem exitItem = new MenuItem("Beenden");
        MenuItem newWindowItem = new MenuItem("Neues Fenster");

        showItem.addActionListener(e -> openFrame());
        logoutItem.addActionListener(e -> Account.logOut(frame));
        homeItem.addActionListener(e -> setHomePanel());
        profileItem.addActionListener(e -> setProfilePanel());
        chatItem.addActionListener(e -> setChatPanel());
        exitItem.addActionListener(e -> System.exit(0));
        newWindowItem.addActionListener(e -> Main.launchNewInstance());

        popup.add(profileItem);
        popup.addSeparator();
        popup.add(showItem);
        popup.add(homeItem);
        popup.add(chatItem);
        popup.add(logoutItem);
        popup.addSeparator();
        popup.add(newWindowItem);
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, Main.serverName, popup);
        trayIcon.setImageAutoSize(true);

        // Klicks auf das Tray-Icon abfangen
        trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Linksklick: Fenster wieder öffnen
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    openFrame();
                }
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {}
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Fehler beim Hinzufügen zum Tray");
        }
    }
    public static void openFrame() {
        frame.setVisible(true);
        frame.toFront();
    }
}
