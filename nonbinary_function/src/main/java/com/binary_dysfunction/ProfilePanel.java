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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

                } catch (IOException e1) {
                    e1.printStackTrace(); // don't swallow this silently while debugging
                }
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

        JPanel rightSide = new JPanel();
        rightSide.setAlignmentX(LEFT_ALIGNMENT);
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBackground(backgroundColor);
        rightSide.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        rightSide.add(fullName);
        rightSide.add(username);
        rightSide.add(uid);
        rightSide.add(descriptionPanel);

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
