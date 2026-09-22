package com.binary_dysfunction.panels;

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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.binary_dysfunction.components.Colors;
import com.binary_dysfunction.components.Component;
import com.binary_dysfunction.components.Toast;
import com.binary_dysfunction.config.Config;
import com.binary_dysfunction.config.JSONConfigurations;
import com.binary_dysfunction.frames.HomeFrame;
import com.binary_dysfunction.main.Main;
import com.binary_dysfunction.types.Account;

public class AdminPanel extends JPanel {

    private final Color backgroundColor = Colors.backgroundColor;
    private final Color backgroundColor2 = Colors.backgroundColorLighter;
    private final Color usrButtonColor = Colors.whiteButtonColor;
    private final Color greenButtonColor = Colors.greenButtonColor;
    private final Color redButtonColor = Colors.redButtonColor;
    private List<Account> registeredAccounts = new ArrayList<>();

    public static Account currentAccount = Main.loggedInAccount;

    HomeFrame currentFrame;

    @SuppressWarnings("UseSpecificCatch")
    public AdminPanel(HomeFrame currentFrame) {
        this.currentFrame = currentFrame;

        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setBackground(backgroundColor2);
        userListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            updateAccountList(userListPanel);
        } catch (IOException ex) {
            System.getLogger(AdminPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        JScrollPane userListScrollPane = new JScrollPane(userListPanel);
        userListScrollPane.setBackground(backgroundColor);

        JButton addUserButton = new JButton("+ Neuer Nutzer");
        addUserButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addUserButton.setForeground(Color.WHITE);
        addUserButton.setBackground(greenButtonColor);
        addUserButton.setFont(new Font("Arial", Font.BOLD, 13));
        addUserButton.addActionListener(e -> {
            JLabel usernameLabel = new JLabel("Nutzername");
            JTextField username = new JTextField();
            JLabel passwordLabel = new JLabel("Passwort");
            JTextField password = new JTextField();
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(usernameLabel);
            panel.add(username);
            panel.add(passwordLabel);
            panel.add(password);
            int result = JOptionPane.showConfirmDialog(null, panel, "Neuer Nutzer", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                if (!username.getText().equals("") && !password.getText().equals("")) {
                    try {
                        JSONConfigurations.addAccount(username.getText(), Config.hashPassword(password.getText()));
                        currentFrame.setAdminPanel();
                        JOptionPane.showMessageDialog(null, "Wir empfehlen Ihnen dem Nutzer, sobald er eine E-Mail angegeben hat, Zugriff auf den 'Personal-Vault' von " + username.getText() + " zu gewehrleisten.", "Hinweis.", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e1) {
                        System.out.println(e1);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Angaben unzureichend.", "Fehler beim erstellen eines neuen Nutzers", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton removeUserButton = new JButton("- Nutzer löschen");
        removeUserButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        removeUserButton.setForeground(Color.WHITE);
        removeUserButton.setBackground(redButtonColor);
        removeUserButton.setFont(new Font("Arial", Font.BOLD, 13));
        removeUserButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "Sind Sie sicher dass Sie '" + currentAccount.username + "' löschen wollen? Verlorene Daten können nicht wieder erlangt werden.", "Achtung!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (result == JOptionPane.YES_OPTION && (!currentAccount.assemblies.contains("Admin") || !currentAccount.assemblies.contains("Owner"))) {
                try {
                    JSONConfigurations.removeAccount(currentAccount.username);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "Der Nutzer konnte nicht gelöscht werden.", "Fehler beim löschen der Nutzers", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Der Nutzer konnte nicht gelöscht werden.", "Fehler beim löschen der Nutzers", JOptionPane.ERROR_MESSAGE);
            }
            currentAccount = Main.loggedInAccount;
            currentFrame.setAdminPanel();
        });

        JPanel addUserButtonPanel = new JPanel(new BorderLayout());
        addUserButtonPanel.add(addUserButton);

        JPanel removeUserButtonPanel = new JPanel(new BorderLayout());
        removeUserButtonPanel.add(removeUserButton);

        JPanel userButtonPanel = new JPanel();
        userButtonPanel.setLayout(new BoxLayout(userButtonPanel, BoxLayout.Y_AXIS));
        userButtonPanel.add(addUserButtonPanel);
        userButtonPanel.add(removeUserButtonPanel);

        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setAlignmentX(LEFT_ALIGNMENT);
        usersPanel.setBackground(backgroundColor);
        usersPanel.add(userListScrollPane, BorderLayout.CENTER);
        usersPanel.add(userButtonPanel, BorderLayout.SOUTH);


        JLabel pfpLabel = new JLabel(Component.scaleImage(Main.serverPath + currentAccount.profilePicturePath, 132));
        pfpLabel.setBorder(BorderFactory.createLineBorder(backgroundColor));

        JPanel pfpPanel = new JPanel();
        pfpPanel.setLayout(new BoxLayout(pfpPanel, BoxLayout.Y_AXIS));
        pfpPanel.setAlignmentX(CENTER_ALIGNMENT);
        pfpPanel.setBackground(backgroundColor2);
        pfpPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        pfpPanel.add(pfpLabel);

        JButton pfpButton = new JButton("Ändern");
        pfpButton.setAlignmentX(CENTER_ALIGNMENT);
        pfpButton.setBackground(greenButtonColor);
        pfpButton.setFont(new Font("Arial", Font.PLAIN, 13));
        pfpButton.addActionListener(e -> {
            System.out.println("Change PFP");
            JFileChooser filechooser = new JFileChooser();
            filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            filechooser.setDialogTitle("Profilbild auswählen");
            
            FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Bilder (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif");
            // filechooser.addChoosableFileFilter(new FileNameExtensionFilter("Bilder (*.png)", "png"));
            // filechooser.addChoosableFileFilter(new FileNameExtensionFilter("Bilder (*.jpg)", "jpg"));
            // filechooser.addChoosableFileFilter(new FileNameExtensionFilter("Bilder (*.jpeg)", "jpeg"));
            // filechooser.addChoosableFileFilter(new FileNameExtensionFilter("Bilder (*.gif)", "gif"));
            filechooser.setFileFilter(imageFilter);
            filechooser.setAcceptAllFileFilterUsed(false);

            int result = filechooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = filechooser.getSelectedFile();
                try {
                    File targetDir = new File(Main.serverPath + Config.ACCOUNTS_DIR + currentAccount.username + "/user-data/");
                    Files.createDirectories(targetDir.toPath()); // ensure it exists (and re-create it, since it may currently be a broken file from prior test runs)

                    File targetFile = new File(targetDir, selectedFile.getName());
                    Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    String finalPath = Config.ACCOUNTS_DIR + currentAccount.username + "/user-data/" + targetFile.getName();
                    currentAccount.profilePicturePath = Main.serverPath + finalPath;
                    if (currentAccount == Main.loggedInAccount) Main.loggedInAccount.profilePicturePath = Main.serverPath + finalPath;
                    JSONConfigurations.updateAccountField(currentAccount.username, "profilePicturePath", finalPath);
                    pfpLabel.setIcon(Component.scaleImage(currentAccount.profilePicturePath, 132));

                    if (currentAccount == Main.loggedInAccount) {
                        currentFrame.topBar.accountButton.setIcon(Component.scaleImage(Main.loggedInAccount.profilePicturePath, 40));
                    }
                } catch (IOException e1) {}
            }
        });

        JPanel changePfpPanel = new JPanel();
        changePfpPanel.setLayout(new BoxLayout(changePfpPanel, BoxLayout.Y_AXIS));
        changePfpPanel.setBackground(backgroundColor2);
        changePfpPanel.add(pfpButton);

        JPanel leftSideConfig = new JPanel();
        leftSideConfig.setBackground(backgroundColor2);
        leftSideConfig.setLayout(new BoxLayout(leftSideConfig, BoxLayout.Y_AXIS));
        leftSideConfig.add(pfpPanel);
        leftSideConfig.add(changePfpPanel);


        JTextField fullName = new JTextField(currentAccount.fullName, JLabel.LEFT);
        fullName.setFont(new Font("Arial", Font.BOLD, 26));
        fullName.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        fullName.setMaximumSize(new Dimension(Integer.MAX_VALUE, fullName.getPreferredSize().height));
        fullName.setBackground(backgroundColor2);
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
                        JSONConfigurations.updateAccountField(currentAccount.username, "fullName", fullName.getText());
                        currentAccount.fullName = fullName.getText();
                        if (currentAccount == Main.loggedInAccount) Main.loggedInAccount.fullName = fullName.getText();
                        Toast.show(null, "Admin", "Name changed.", 3000, Toast.Position.BOTTOM_RIGHT, false);
                        System.out.println("FullName updated");
                    } catch (IOException e1) {}
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JLabel username = new JLabel(currentAccount.username, JLabel.LEFT);
        username.setFont(new Font("Arial", Font.BOLD, 16));
        username.setForeground(new Color(160, 160, 160));

        JButton uid = new JButton("UID: " + currentAccount.uid);
        uid.setBorder(null);
        uid.setBackground(null);
        uid.setForeground(new Color(120, 120, 120));
        uid.setFont(new Font("Arial", Font.BOLD, 9));
        uid.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        uid.setToolTipText("Kopieren");
        uid.addActionListener(e -> {
            String myString = currentAccount.uid;
            StringSelection stringSelection = new StringSelection(myString);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            Toast.show(null, "UID", "Saved to clipboard.", 3000, Toast.Position.BOTTOM_RIGHT, false);
        });
        uid.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                uid.setForeground(new Color(51, 134, 55));
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                uid.setForeground(new Color(120, 120, 120));
            }
        });

        JLabel descriptionTitle = new JLabel("Beschreibung", JLabel.LEFT);
        descriptionTitle.setFont(new Font("Arial", Font.BOLD, 20));
        descriptionTitle.setForeground(new Color(180, 180, 180));

        JTextField descriptionArea = new JTextField(currentAccount.description, JLabel.LEFT);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 11));
        descriptionArea.setBackground(backgroundColor);
        descriptionArea.setForeground(new Color(200, 200, 200));
        // descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createLineBorder(Color.BLACK),
        //     BorderFactory.createEmptyBorder(5, 5, 2, 5)
        // ));
        descriptionArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        descriptionArea.setPreferredSize(new Dimension(500, 28));
        descriptionArea.setMaximumSize(new Dimension(500, descriptionArea.getPreferredSize().height));
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
                        JSONConfigurations.updateAccountField(currentAccount.username, "description", descriptionArea.getText());
                        currentAccount.description = descriptionArea.getText();
                        if (currentAccount == Main.loggedInAccount) Main.loggedInAccount.description = descriptionArea.getText();
                        Toast.show(null, "Admin", "Description changed.", 3000, Toast.Position.BOTTOM_RIGHT, false);
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
        descriptionPanel.setBackground(backgroundColor2);
        descriptionPanel.add(descriptionTitle);
        descriptionPanel.add(descriptionArea);


        JLabel emailTitle = new JLabel("E-Mail");
        emailTitle.setFont(new Font("Arial", Font.BOLD, 20));
        emailTitle.setForeground(new Color(180, 180, 180));

        JTextField emailArea = new JTextField(currentAccount.email, JLabel.LEFT);
        emailArea.setFont(new Font("Arial", Font.PLAIN, 11));
        emailArea.setBackground(backgroundColor);
        emailArea.setForeground(new Color(200, 200, 200));
        // descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createLineBorder(Color.BLACK),
        //     BorderFactory.createEmptyBorder(5, 5, 2, 5)
        // ));
        emailArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        emailArea.setPreferredSize(new Dimension(500, 28));
        emailArea.setMaximumSize(new Dimension(500, emailArea.getPreferredSize().height));
        emailArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!emailArea.getText().contains("@")) {
                        // JOptionPane.showMessageDialog(null, "Die E-Mail muss ein '@' beinhalten.", "Fehlerhafte E-Mail", JOptionPane.ERROR_MESSAGE);
                        Toast.show(null, "Admin", "Ungültige E-Mail", 3000, Toast.Position.BOTTOM_RIGHT, false);
                        return;
                    }
                    emailArea.setFocusable(false);
                    emailArea.setFocusable(true);
                    try {
                        JSONConfigurations.updateAccountField(currentAccount.username, "email", emailArea.getText());
                        currentAccount.email = emailArea.getText();
                        if (currentAccount == Main.loggedInAccount) Main.loggedInAccount.email = emailArea.getText();
                        Toast.show(null, "Admin", "E-Mail changed.", 3000, Toast.Position.BOTTOM_RIGHT, false);
                        System.out.println("E-Mail updated for: " + currentAccount.username);
                    } catch (IOException e1) {}
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JPanel emailPanel = new JPanel();
        emailPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.Y_AXIS));
        emailPanel.setBackground(backgroundColor2);
        emailPanel.add(emailTitle);
        emailPanel.add(emailArea);


        JLabel newPasswordTitle = new JLabel("Neues Passwort", JLabel.LEFT);
        newPasswordTitle.setFont(new Font("Arial", Font.BOLD, 20));
        newPasswordTitle.setForeground(new Color(180, 180, 180));

        JTextField newPasswordField = new JTextField("", JLabel.LEFT);
        newPasswordField.setAlignmentX(LEFT_ALIGNMENT);
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 11));
        newPasswordField.setBackground(backgroundColor);
        newPasswordField.setForeground(new Color(200, 200, 200));
        // descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createLineBorder(Color.BLACK),
        //     BorderFactory.createEmptyBorder(5, 5, 2, 5)
        // ));
        newPasswordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        newPasswordField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 28));
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

                    String newHashedPassword = Config.hashPassword(newPasswordField.getText());
                    try {
                        JSONConfigurations.updateAccountField(currentAccount.username, "passwordHash", newHashedPassword);
                        System.out.println("Password changed for: " + currentAccount.username);
                        Toast.show(null, "Admin", "Password changed.", 3000, Toast.Position.BOTTOM_RIGHT, false);
                        newPasswordField.setText("");
                    } catch (IOException e1) {
                        System.out.println("Failed changing PasswordHash");
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        JPanel newPasswordPanel = new JPanel();
        newPasswordPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        newPasswordPanel.setBackground(backgroundColor2);
        newPasswordPanel.setLayout(new BoxLayout(newPasswordPanel, BoxLayout.Y_AXIS));
        newPasswordPanel.add(newPasswordTitle);
        newPasswordPanel.add(newPasswordField);


        JLabel cloudTitle = new JLabel("Cloud Settings", JLabel.LEFT);
        cloudTitle.setFont(new Font("Arial", Font.BOLD, 20));
        cloudTitle.setForeground(new Color(180, 180, 180));

        JButton cloudActivatedButton = new JButton("Cloud aktiviert: " + currentAccount.cloudActivated);
        cloudActivatedButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cloudActivatedButton.setBackground(backgroundColor);
        cloudActivatedButton.addActionListener(e -> {
            if (currentAccount.email.equals("")) {
                JOptionPane.showMessageDialog(null, "Cloud-Aktivierung für den Nutzer: '" + currentAccount.username + "', nicht möglich. Hier für muss eine E-Mail angegeben sein und berechtigt sein auf deren privaten 'Vault' zugreifen zu können.", "Cloud-Aktivierung nicht möglich!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (currentAccount.cloudActivated) {
                currentAccount.cloudActivated = false;
                Main.loggedInAccount.cloudActivated = false;
                try {
                    JSONConfigurations.updateAccountField(currentAccount.username, "cloudActivated", false);
                } catch (IOException e1) {}
            } else {
                currentAccount.cloudActivated = true;
                Main.loggedInAccount.cloudActivated = true;
                try {
                    JSONConfigurations.updateAccountField(currentAccount.username, "cloudActivated", true);
                } catch (IOException e1) {}
            }
            currentFrame.setAdminPanel();
        });

        JPanel cloudPanel = new JPanel();
        cloudPanel.setLayout(new BoxLayout(cloudPanel, BoxLayout.Y_AXIS));
        cloudPanel.setBackground(backgroundColor2);
        cloudPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0 ,0));
        cloudPanel.add(cloudTitle);
        cloudPanel.add(cloudActivatedButton);

        JPanel rightSideConfig = new JPanel();
        rightSideConfig.setBackground(backgroundColor2);
        rightSideConfig.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        rightSideConfig.setLayout(new BoxLayout(rightSideConfig, BoxLayout.Y_AXIS));
        rightSideConfig.add(fullName);
        rightSideConfig.add(username);
        rightSideConfig.add(uid);
        rightSideConfig.add(descriptionPanel);
        rightSideConfig.add(emailPanel);
        rightSideConfig.add(newPasswordPanel);
        rightSideConfig.add(cloudPanel);


        JPanel usrConfigPanel = new JPanel(new BorderLayout());
        usrConfigPanel.setBackground(backgroundColor2);
        usrConfigPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        usrConfigPanel.add(leftSideConfig, BorderLayout.WEST);
        usrConfigPanel.add(rightSideConfig);

        JScrollPane usrConfigScrollPane = new JScrollPane(usrConfigPanel);
        usrConfigScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        usrConfigScrollPane.setHorizontalScrollBar(null);

        JPanel fullConfigPanel = new JPanel(new BorderLayout());
        fullConfigPanel.setAlignmentX(LEFT_ALIGNMENT);
        fullConfigPanel.setBackground(backgroundColor);
        fullConfigPanel.add(usersPanel, BorderLayout.WEST);
        fullConfigPanel.add(usrConfigScrollPane);

        JLabel pageTitle = new JLabel("Admin Zone");
        pageTitle.setAlignmentX(LEFT_ALIGNMENT);
        pageTitle.setFont(new Font("Arial", Font.BOLD, 30));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(backgroundColor);
        contentPanel.add(pageTitle);
        contentPanel.add(fullConfigPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));
        mainPanel.add(contentPanel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }

    private void updateAccountList(JPanel target) throws IOException {

        String content = Files.readString(JSONConfigurations.getAccountPath());
        JSONArray accounts = new JSONArray(content);

        System.out.println("Registered accounts:");
        for (int i = 0; i < accounts.length(); i++) {
            JSONObject acc =  accounts.getJSONObject(i);

            String username = acc.getString("username");
            String passwordHash = acc.getString("passwordHash");
            String fullName = acc.getString("fullName");
            String description = acc.getString("description");
            String profilePicturePath = acc.getString("profilePicturePath");
            String uid = acc.getString("uid");
            List<Object> assemblies = acc.getJSONArray("assemblies").toList();
            boolean cloudActivated = acc.getBoolean("cloudActivated");
            String email = acc.getString("email");
            List<Object> chats = acc.getJSONArray("chats").toList();

            registeredAccounts.add(new Account(username, passwordHash, fullName, description, profilePicturePath, uid, assemblies, cloudActivated, email, chats));
            System.out.println("-" + username);
        }

        registeredAccounts.sort(Comparator.comparing(Account::getUsername));

        for (Account acc : registeredAccounts) {
            JButton userButton = new JButton(acc.username);
            userButton.setBackground(usrButtonColor);
            userButton.setForeground(Color.BLACK);
            userButton.setFont(new Font("Arial", Font.BOLD, 13));
            userButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(backgroundColor),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            userButton.addActionListener(e -> {
                for (Account account : registeredAccounts) {
                    if (account.equals(acc)) {
                        currentAccount = account;
                        System.out.println(currentAccount.username);
                        currentFrame.setAdminPanel();
                    }
                }
            });
            JPanel userButtonPanel = new JPanel(new BorderLayout());
            userButtonPanel.setBackground(backgroundColor2);
            userButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            userButtonPanel.add(userButton);
            target.add(userButtonPanel);
        }
        target.invalidate();
        target.revalidate();
        target.repaint();
    }
}
