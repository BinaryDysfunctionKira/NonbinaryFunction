package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.binary_dysfunction.components.Component;

public class ProfilePanel extends JScrollPane {

    private final Color backgroundColor = new Color(40, 40, 40);

    public ProfilePanel(HomeFrame hf) {

        JLabel pfp = new JLabel(Component.scaleImage(Main.loggedInAccount.profilePicturePath, 200));
        pfp.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        pfp.setAlignmentX(CENTER_ALIGNMENT);

        JButton pfpButton = new JButton("Ändern");
        pfpButton.setBackground(new Color(51, 134, 55));
        pfpButton.setAlignmentX(CENTER_ALIGNMENT);
        pfpButton.addActionListener(e -> {
            System.out.println("Change PFP");
            JFileChooser filechooser = new JFileChooser();
            filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            filechooser.setDialogTitle("Profilbild auswählen");
            
            FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG-Bilder (*.png)", "png");
            filechooser.setFileFilter(pngFilter);
            filechooser.setAcceptAllFileFilterUsed(false);

            int result = filechooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = filechooser.getSelectedFile();
                try {
                    File targetDir = new File(Main.serverPath + "/users/" + Main.loggedInAccount.username + "/user-data/");
                    Files.createDirectories(targetDir.toPath()); // ensure it exists (and re-create it, since it may currently be a broken file from prior test runs)

                    File targetFile = new File(targetDir, selectedFile.getName());
                    Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    Main.loggedInAccount.profilePicturePath = targetFile.getPath();
                    JSONConfigurations.updateAccountField(Main.loggedInAccount.username, "profilePicturePath", targetFile.getPath());
                    pfp.setIcon(Component.scaleImage(Main.loggedInAccount.profilePicturePath, 200));
                    hf.topBar.accountButton.setIcon(Component.scaleImage(Main.loggedInAccount.profilePicturePath, 40));

                } catch (IOException e1) {}
            }
        });

        JPanel leftSide = new JPanel();
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
        leftSide.setBackground(backgroundColor);
        leftSide.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        leftSide.add(pfp);
        leftSide.add(pfpButton);


        JTextField fullName = new JTextField(Main.loggedInAccount.fullName, JLabel.LEFT);
        fullName.setFont(new Font("Arial", Font.BOLD, 36));
        fullName.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        fullName.setMaximumSize(new Dimension(Integer.MAX_VALUE, fullName.getPreferredSize().height));
        fullName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fullName.setFocusable(false);
                    fullName.setFocusable(true);
                    try {
                        JSONConfigurations.updateAccountField(Main.loggedInAccount.username, "fullName", fullName.getText());
                        Main.loggedInAccount.fullName = fullName.getText();
                        System.out.println("FullName updated");
                    } catch (IOException e1) {}
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JLabel username = new JLabel(Main.loggedInAccount.username, JLabel.LEFT);
        username.setFont(new Font("Arial", Font.BOLD, 24));
        username.setForeground(new Color(160, 160, 160));

        JButton uid = new JButton("UID: " + Main.loggedInAccount.uid);
        uid.setBorder(null);
        uid.setBackground(null);
        uid.setForeground(new Color(120, 120, 120));
        uid.setFont(new Font("Arial", Font.BOLD, 9));
        uid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        uid.setToolTipText("Kopieren");
        uid.addActionListener(e -> {
            String myString = Main.loggedInAccount.uid;
            StringSelection stringSelection = new StringSelection(myString);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        uid.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                uid.setForeground(new Color(51, 134, 55));
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                uid.setForeground(new Color(120, 120, 120));
            }
        });


        JLabel descriptionTitle = new JLabel("Beschreibung");
        descriptionTitle.setFont(new Font("Arial", Font.BOLD, 20));
        descriptionTitle.setForeground(new Color(160, 160, 160));

        JTextField descriptionArea = new JTextField(Main.loggedInAccount.description, JLabel.LEFT);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descriptionArea.setBackground(new Color(20, 20, 20));
        descriptionArea.setForeground(new Color(200, 200, 200));
        // descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createLineBorder(Color.BLACK),
        //     BorderFactory.createEmptyBorder(5, 5, 2, 5)
        // ));
        descriptionArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        descriptionArea.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        descriptionArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, descriptionArea.getPreferredSize().height));
        descriptionArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    descriptionArea.setFocusable(false);
                    descriptionArea.setFocusable(true);
                    try {
                        JSONConfigurations.updateAccountField(Main.loggedInAccount.username, "description", descriptionArea.getText());
                        Main.loggedInAccount.description = descriptionArea.getText();
                        System.out.println("Description updated");
                    } catch (IOException e1) {}
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
        descriptionPanel.setBackground(backgroundColor);
        descriptionPanel.add(descriptionTitle);
        descriptionPanel.add(descriptionArea);


        JLabel oldPasswordTitle = new JLabel("Altes Passwort", JLabel.LEFT);
        oldPasswordTitle.setAlignmentX(LEFT_ALIGNMENT);
        oldPasswordTitle.setFont(new Font("Arial", Font.BOLD, 12));

        JPasswordField oldPasswordField = new JPasswordField("", JLabel.LEFT);
        oldPasswordField.setAlignmentX(LEFT_ALIGNMENT);
        oldPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        oldPasswordField.setBackground(new Color(20, 20, 20));
        oldPasswordField.setForeground(new Color(200, 200, 200));
        // descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createLineBorder(Color.BLACK),
        //     BorderFactory.createEmptyBorder(5, 5, 2, 5)
        // ));
        oldPasswordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        oldPasswordField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        oldPasswordField.setMaximumSize(new Dimension(150, oldPasswordField.getPreferredSize().height));
        oldPasswordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    oldPasswordField.setFocusable(false);
                    oldPasswordField.setFocusable(true);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JPanel oldPasswordPanel = new JPanel();
        oldPasswordPanel.setAlignmentX(LEFT_ALIGNMENT);
        oldPasswordPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        oldPasswordPanel.setBackground(backgroundColor);
        oldPasswordPanel.setLayout(new BoxLayout(oldPasswordPanel, BoxLayout.Y_AXIS));
        oldPasswordPanel.add(oldPasswordTitle);
        oldPasswordPanel.add(oldPasswordField);

        JPanel passwordPanelRow1 = new JPanel();
        passwordPanelRow1.setAlignmentX(LEFT_ALIGNMENT);
        passwordPanelRow1.setBackground(backgroundColor);
        passwordPanelRow1.setLayout(new BoxLayout(passwordPanelRow1, BoxLayout.X_AXIS));
        passwordPanelRow1.add(oldPasswordPanel);


        JLabel newPasswordTitle = new JLabel("Neues Passwort", JLabel.LEFT);
        newPasswordTitle.setAlignmentX(LEFT_ALIGNMENT);
        newPasswordTitle.setFont(new Font("Arial", Font.BOLD, 12));

        JTextField newPasswordField = new JTextField("", JLabel.LEFT);
        newPasswordField.setAlignmentX(LEFT_ALIGNMENT);
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        newPasswordField.setBackground(new Color(20, 20, 20));
        newPasswordField.setForeground(new Color(200, 200, 200));
        // descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createLineBorder(Color.BLACK),
        //     BorderFactory.createEmptyBorder(5, 5, 2, 5)
        // ));
        newPasswordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        newPasswordField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        newPasswordField.setMaximumSize(new Dimension(150, newPasswordField.getPreferredSize().height));
        newPasswordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    newPasswordField.setFocusable(false);
                    newPasswordField.setFocusable(true);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JPanel newPasswordPanel = new JPanel();
        newPasswordPanel.setAlignmentX(LEFT_ALIGNMENT);
        newPasswordPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        newPasswordPanel.setBackground(backgroundColor);
        newPasswordPanel.setLayout(new BoxLayout(newPasswordPanel, BoxLayout.Y_AXIS));
        newPasswordPanel.add(newPasswordTitle);
        newPasswordPanel.add(newPasswordField);


        JLabel newPasswordRepeatTitle = new JLabel("Neues Passwort wiederholen", JLabel.LEFT);
        newPasswordRepeatTitle.setAlignmentX(LEFT_ALIGNMENT);
        newPasswordRepeatTitle.setFont(new Font("Arial", Font.BOLD, 12));

        JTextField newPasswordRepeatField = new JTextField("", JLabel.LEFT);
        newPasswordRepeatField.setAlignmentX(LEFT_ALIGNMENT);
        newPasswordRepeatField.setFont(new Font("Arial", Font.PLAIN, 13));
        newPasswordRepeatField.setBackground(new Color(20, 20, 20));
        newPasswordRepeatField.setForeground(new Color(200, 200, 200));
        // descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createLineBorder(Color.BLACK),
        //     BorderFactory.createEmptyBorder(5, 5, 2, 5)
        // ));
        newPasswordRepeatField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        newPasswordRepeatField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        newPasswordRepeatField.setMaximumSize(new Dimension(150, newPasswordRepeatField.getPreferredSize().height));
        newPasswordRepeatField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    newPasswordRepeatField.setFocusable(false);
                    newPasswordRepeatField.setFocusable(true);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JPanel newPasswordRepeatPanel = new JPanel();
        newPasswordRepeatPanel.setAlignmentX(LEFT_ALIGNMENT);
        newPasswordRepeatPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 0));
        newPasswordRepeatPanel.setBackground(backgroundColor);
        newPasswordRepeatPanel.setLayout(new BoxLayout(newPasswordRepeatPanel, BoxLayout.Y_AXIS));
        newPasswordRepeatPanel.add(newPasswordRepeatTitle);
        newPasswordRepeatPanel.add(newPasswordRepeatField);

        JPanel passwordPanelRow2 = new JPanel();
        passwordPanelRow2.setAlignmentX(LEFT_ALIGNMENT);
        passwordPanelRow2.setBackground(backgroundColor);
        passwordPanelRow2.setLayout(new BoxLayout(passwordPanelRow2, BoxLayout.X_AXIS));
        passwordPanelRow2.add(newPasswordPanel);
        passwordPanelRow2.add(newPasswordRepeatPanel);

        JLabel passwordPanelTitle = new JLabel("Passwort");
        passwordPanelTitle.setAlignmentX(LEFT_ALIGNMENT);
        passwordPanelTitle.setFont(new Font("Arial", Font.BOLD, 20));
        passwordPanelTitle.setForeground(new Color(160, 160, 160));

        JLabel errorMessage = new JLabel("ERROR!"); // Needs to be implemented in check
        errorMessage.setAlignmentX(LEFT_ALIGNMENT);
        errorMessage.setFont(new Font("Arial", Font.BOLD, 14));
        errorMessage.setForeground(new Color(206, 0, 0));
        errorMessage.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        errorMessage.setVisible(false);

        JButton passwordChangeButton = new JButton("Passwort ändern");
        passwordChangeButton.setAlignmentX(LEFT_ALIGNMENT);
        passwordChangeButton.setBackground(new Color(51, 134, 55));
        // Function of changing password
        passwordChangeButton.addActionListener(e -> {
            String oldPasswordHash = Config.hashPassword(Arrays.toString(oldPasswordField.getPassword()));
            
            if (oldPasswordHash.equals(Main.loggedInAccount.passwordHash) && newPasswordField.getText().equals(newPasswordRepeatField.getText())) {
                String newHashedPassword = Config.hashPassword(newPasswordField.getText());
                try {
                    JSONConfigurations.updateAccountField(Main.loggedInAccount.username, "passwordHash", newHashedPassword);
                    System.out.println("Password changed");
                } catch (IOException e1) {
                    System.out.println("Failed changing PasswordHash");
                }
            } else { System.out.println("ERROR AT PASSWORD CHANGING");}
        });

        JPanel passwordChangeButtonPanel = new JPanel();
        passwordChangeButtonPanel.setLayout(new BoxLayout(passwordChangeButtonPanel, BoxLayout.Y_AXIS));
        passwordChangeButtonPanel.setAlignmentX(LEFT_ALIGNMENT);
        passwordChangeButtonPanel.setBackground(backgroundColor);
        passwordChangeButtonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0 ,0));
        passwordChangeButtonPanel.add(passwordChangeButton);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setAlignmentX(LEFT_ALIGNMENT);
        passwordPanel.setBackground(backgroundColor);
        passwordPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.add(passwordPanelTitle);
        passwordPanel.add(passwordPanelRow1);
        passwordPanel.add(passwordPanelRow2);
        passwordPanel.add(errorMessage);
        passwordPanel.add(passwordChangeButtonPanel);

        JPanel rightSide = new JPanel();
        rightSide.setAlignmentX(LEFT_ALIGNMENT);
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBackground(backgroundColor);
        rightSide.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        rightSide.add(fullName);
        rightSide.add(username);
        rightSide.add(uid);
        rightSide.add(descriptionPanel);
        rightSide.add(passwordPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(backgroundColor);
        contentPanel.add(leftSide, BorderLayout.WEST);
        contentPanel.add(rightSide);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));
        mainPanel.add(contentPanel);

        this.setViewportView(mainPanel);
        this.setHorizontalScrollBar(null);
    }
}