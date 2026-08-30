package com.binary_dysfunction;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

public class PasswordFrame {

    public static JFrame frame;

    private void start() {
        Main.serverPath = Main.config.loadServerPath();
        Main.ownerSet = Config.hasServerConfig(new File(Main.serverPath));
        Main.isServerNew = !Config.hasServerConfig(new File(Main.serverPath));
    }

    public void startup() {

        start();

        JLabel title = new JLabel("Herzlich Willkommen!", JLabel.CENTER);
        title.setAlignmentX(Container.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.PLAIN, 24));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Benutzername und Passwort eingeben.", JLabel.CENTER);
        subtitle.setAlignmentX(Container.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setOpaque(false);
        subtitle.setForeground(new Color(0, 0, 0, 127));

        JLabel userText = new JLabel("Benutzername");
        userText.setAlignmentX(Container.CENTER_ALIGNMENT);
        userText.setFont(new Font("Arial", Font.PLAIN, 14));
        userText.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        userText.setOpaque(false);
        userText.setForeground(new Color(0, 0, 0, 127));

        JTextField userField = new JTextField();
        userField.setAlignmentX(Container.CENTER_ALIGNMENT);
        userField.setBounds(0, 0, subtitle.getWidth(), 20);
        userField.setFont(new Font("Arial", Font.PLAIN, 14));
        userField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));
        userField.setBackground(Color.WHITE);
        userField.setForeground(Color.BLACK);

        JLabel passwordText = new JLabel("Passwort");
        passwordText.setAlignmentX(Container.CENTER_ALIGNMENT);
        passwordText.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordText.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        passwordText.setOpaque(false);
        passwordText.setForeground(new Color(0, 0, 0, 127));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setAlignmentX(Container.CENTER_ALIGNMENT);
        passwordField.setBounds(0, 0, subtitle.getWidth(), 20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Color.BLACK);

        JLabel emptyLabel = new JLabel(" ");
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));


        JLabel errorMessage = new JLabel();
        errorMessage.setAlignmentX(Container.CENTER_ALIGNMENT);
        errorMessage.setFont(new Font("Arial", Font.BOLD, 14));
        errorMessage.setForeground(new Color(206, 0, 0));
        errorMessage.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        errorMessage.setVisible(false);

        JButton submitButton = new JButton("Anmelden");
        submitButton.setAlignmentX(Container.CENTER_ALIGNMENT);
        submitButton.setFont(new Font("Arial", Font.PLAIN, 20));
        submitButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(116, 197, 105), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        submitButton.setBackground(new Color(51, 134, 55));
        submitButton.addActionListener(e -> {
            // errorMessage.setVisible(true);
            // Check if User is available in system
            String usrName = userField.getText();
            char[] pswd = null;
            boolean notLoadable = false;

            if (passwordField.getPassword().length != 0) {
                pswd = passwordField.getPassword();
            }

            if (Main.isServerNew) {
                try {
                    JSONConfigurations.addAccount(usrName, Config.hashPassword(new String(pswd)));
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if (usrName.equals("")) {
                errorMessage.setText("Kein Benutzername eingetragen!");
                notLoadable = true;
            }
            if (pswd == null) {
                errorMessage.setText("Kein Passwort eingetragen!");
                notLoadable = true;
            }
            if (usrName.equals("") && pswd == null) {
                errorMessage.setText("Kein Benutzername und Passwort eingetragen!");
                notLoadable = true;
            }

            if (notLoadable) {
                errorMessage.setVisible(true);
                return;
            }

            try {
                if (areAccountDetailsCorrect(usrName, pswd)) {
                    // Log User in with Account Details
                    errorMessage.setVisible(false);
                    Main.loggedInAccount = JSONConfigurations.logInAccount(usrName, Config.hashPassword(Arrays.toString(pswd)));
                    System.out.println(Main.loggedInAccount.toString());
                    SwingUtilities.invokeLater(new HomeFrame()::startup);
                    frame.dispose();
                    return;
                } else {
                    errorMessage.setText("Passwort oder Benutzername inkorrekt!");
                }
            } catch (IOException ex) {
                System.getLogger(PasswordFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            
            errorMessage.setVisible(true);
        });
        submitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                submitButton.setBackground(new Color(70, 127, 75));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                submitButton.setBackground(new Color(51, 134, 55));
            }
        });

        JLabel currentServer = new JLabel("Aktueller Server: '" + Main.serverName + "'");
        currentServer.setAlignmentX(Container.CENTER_ALIGNMENT);
        currentServer.setFont(new Font("Arial", Font.PLAIN, 11));
        currentServer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));



        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(47, 80, 20, 80));
        contentPanel.setBackground(new Color(161, 161, 161));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(47, 127, 47 , 127));
        mainPanel.add(contentPanel);

        frame = new JFrame("Nonbinary Function - Anmeldung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(718, 600);
        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);


        JLabel searchPathHeader = new JLabel("Sie haben ihren Server nicht gefunden?");
        searchPathHeader.setAlignmentX(Container.CENTER_ALIGNMENT);
        searchPathHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton searchPathButton = new JButton("Suche Server-Ordner");
        searchPathButton.setAlignmentX(Container.CENTER_ALIGNMENT);
        searchPathButton.setBounds(0, 0, 30, 30);
        searchPathButton.addActionListener(e -> {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.setDialogTitle("Server auswählen");
            int returnVal = directoryChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                Main.serverPath = directoryChooser.getSelectedFile().getPath();
                File path = new File(Main.serverPath);
                if (path.isDirectory() && path.list().length == 0 && Config.hasServerConfig(path) == false) {
                    int result = JOptionPane.showConfirmDialog(null, "Kein Server in dem ausgewähltem Ordner vorhanden. Einen neuen erstellen?", "Kein Server gefunden",
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.ERROR_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        // Erstelle neuen Server
                        System.out.println("Server wird erstellt.");
                        try {
                            Config.setupServer(path);
                        } catch (IOException ex) {
                            System.getLogger(PasswordFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                            JOptionPane.showMessageDialog(null, "Fehler beim Löschen: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                            return; // don't continue as if setup succeeded
                        }
                        Main.isServerNew = true;
                    } else {
                        System.out.println("Server-Auswahl abgebrochen");
                        return;
                    }
                } else if (path.isDirectory() && (path.list().length != 0 && Config.hasServerConfig(path) == false)) {
                    int result = JOptionPane.showConfirmDialog(null, "Kein Server in dem ausgewähltem Ordner vorhanden. Einen neuen erstellen? Vorhandene Daten würden verloren gehen.", "Kein Server gefunden",
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.ERROR_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        // Erstelle neuen Server
                        int secondresult = JOptionPane.showConfirmDialog(null, "Vorhandene Daten werden unwiderruflich gelöscht. Wollen sie fortfahren?", "ACHTUNG!!!",
                            JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (secondresult == JOptionPane.YES_OPTION) {
                            // Erstelle neuen Server
                            System.out.println("Server wird erstellt.");
                            try {
                                Config.setupServer(path);
                            } catch (IOException ex) {
                                System.getLogger(PasswordFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                                JOptionPane.showMessageDialog(null, "Fehler beim Löschen: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
                                ex.printStackTrace();
                                return; // don't continue as if setup succeeded
                            }
                            Main.isServerNew = true;
                        } else {
                            System.out.println("Server-Auswahl abgebrochen");
                            return;
                        }
                    } else {
                        System.out.println("Server-Auswahl abgebrochen");
                        return;
                    }
                } else if (Config.hasServerConfig(path)) {
                    
                }

                Main.serverName = directoryChooser.getSelectedFile().getName();
                currentServer.setText("Aktueller Server: '" + Main.serverName + "'");
                Main.config.saveServerPath(path.getPath());

                Main.start();
                frame.dispose();
            }
        });

        
        contentPanel.add(title);
        contentPanel.add(subtitle);
        contentPanel.add(userText);
        contentPanel.add(userField);
        contentPanel.add(passwordText);
        contentPanel.add(passwordField);
        contentPanel.add(emptyLabel);
        contentPanel.add(errorMessage);
        contentPanel.add(submitButton);
        contentPanel.add(searchPathHeader);
        contentPanel.add(searchPathButton);
        contentPanel.add(currentServer);

        
    }

    private boolean areAccountDetailsCorrect(String usrName, char[] pswd) throws IOException {

        String password = new String(pswd);

        if (!Files.exists(JSONConfigurations.ACCOUNT_PATH)) {
            return false;
        }

        String content = Files.readString(JSONConfigurations.ACCOUNT_PATH);
        JSONArray accounts = new JSONArray(content);

        String typedHash = Config.hashPassword(password);

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc = accounts.getJSONObject(i);

            String storedUsername = acc.getString("username");
            String storedHash = acc.getString("passwordHash");
            
            if (storedUsername.equals(usrName) && storedHash.equals(typedHash)) {
                return true;
            }
        }
        return false;
    }
}
