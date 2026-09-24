package com.binary_dysfunction.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.binary_dysfunction.components.Component;
import com.binary_dysfunction.main.Main;

public class SetupTutorialFrame extends JFrame {

    int pageCount = 1;
    int maxPages = 6;

    JLabel image = new JLabel();
    JTextPane textPane = new JTextPane();

    public void startup() {
        image.setSize(new Dimension(500, 300));
        image.setHorizontalAlignment(JLabel.CENTER);
        image.setIcon(Component.resizeImageProportional(new ImageIcon(Main.appIcon), 500, 300));

        textPane.setEditable(false);
        textPane.setHighlighter(null);
        textPane.setFocusable(false);

        JLabel currentPageLabel = new JLabel(pageCount + "/" + maxPages, JLabel.CENTER);

        JButton nextButton = new JButton("Nächstes >");
        nextButton.addActionListener(e -> {
            if (pageCount <= maxPages) {
                pageCount++;
                currentPageLabel.setText(pageCount + "/" + maxPages);
                // switch-case for switching pages
                setPage(pageCount);
            }
        });

        JButton previousButton = new JButton("< Zurück");
        previousButton.addActionListener(e -> {
            if (pageCount > 1) {
                pageCount--;
                currentPageLabel.setText(pageCount + "/" + maxPages);
                // switch-case for switching pages
                setPage(pageCount);
            }
        });

        JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.add(nextButton, BorderLayout.EAST);
        buttonsPanel.add(previousButton, BorderLayout.WEST);
        buttonsPanel.add(currentPageLabel);

        setPage(1);
        JScrollPane contentScrollPane = new JScrollPane(textPane);
        contentScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        mainPanel.add(contentScrollPane);
        mainPanel.add(image, BorderLayout.NORTH);

        this.setTitle("Setup Tutorial");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getContentPane().add(mainPanel);
        this.setSize(500, 500);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void setPage(int i) {
        switch (i) {
            case 1 -> {
                image.setIcon(Component.resizeImageProportional(new ImageIcon(Main.appIcon), 500, 300));
                textPane.setText("Diese Applikation ist erstellt worden, um kleineren Event-Management Teams zu ermöglichen deren Projekte umzusetzen. Auf Grund, dass sie kostenlos ist, läuft sie *momentan* nur über Dritten, bspw. Google Drive oder Dropbox. Der gemeinsame Zugriff wird ermöglicht dadurch, dass alle Teilnehmer Zugriff auf den selben Drive haben und auf den Server Ordner zugreifen können.\n\nWir danken Ihnen dafür, dass Sie sich für uns entschieden haben!");
            }
            case 2 -> {
                try {
                    image.setIcon(Component.resizeImageProportional(new ImageIcon(ImageIO.read(Main.class.getResource("/setuptutorial/Google_Drive_logo.png"))), 500, 300));
                } catch (IOException ex) {}
                textPane.setText("Für dieses Tutorial verwenden wir den Google Drive.\nGoogle Drive ist mit 15GB Speicherplatz kostenlos nutzbar und einfach einzurichten.\n\nFalls Sie eine andere Methode verwenden wollen, können Sie dies ebenfalls machen, dennoch wird nur der Google Drive in diesem Tutorial behandelt.");
            }
            case 3 -> {
                try {
                    image.setIcon(Component.resizeImageProportional(new ImageIcon(ImageIO.read(Main.class.getResource("/setuptutorial/google-drive-download.png"))), 500, 300));
                } catch (IOException ex) {}
                textPane.setText("Als erstes, falls Sie es noch nicht gemacht haben, downloaden Sie die Desktop App von Google Drive.\n\nhttps://ipv4.google.com/intl/de/drive/download/");
            }
            case 4 -> {
                try {
                    image.setIcon(Component.resizeImageProportional(new ImageIcon(ImageIO.read(Main.class.getResource("/setuptutorial/server-select.png"))), 500, 300));
                } catch (IOException ex) {}
                textPane.setText("Zunächst sind Sie fertig und müssen darauf warten, dass Sie von einem Admin eines Servers eingeladen werden und wählen im Server-Sucher den Ordner, in dem der Server vorhanden ist, aus oder erstellen einen eigenen Server, indem Sie bei der Serversuche einen neuen Ordner aussuchen und ihn als Server auswählen. Dadurch wird der Server geleert und ein neuer Server wird angelegt, deshalb passen Sie auf, dass keine wichtigen Dateien in diesem Ordner sind. Außerdem zur öffentlichen Verwendung der Servers empfehlen wir Ihnen, den Server in ihrem Drive zu erstellen, damit später auch andere Nutzer darauf zugreifen können.");
            }
            case 5 -> {
                try {
                    image.setIcon(Component.resizeImageProportional(new ImageIcon(ImageIO.read(Main.class.getResource("/setuptutorial/server-for-all.png"))), 500, 300));
                } catch (IOException ex) {}
                textPane.setText("Als Admin können Sie nun von der Website vom Google Drive weitere Nutzer einladen, in dem Sie Ihnen den Ordner des Servers freigeben, bspw. mit Hilfe eines Links.");
            }
            case 6 -> {
                try {
                    image.setIcon(Component.resizeImageProportional(new ImageIcon(ImageIO.read(Main.class.getResource("/setuptutorial/connect-server.png"))), 500, 300));
                } catch (IOException ex) {}
                textPane.setText("Sie als Nutzer können zunächst dann, wenn sie Zugriff haben, ebenfalls auf der Google Drive Seite sich anmelden, den freigegebenen Ordner suchen und mit Google Drive Desktop verknüpfen.\n\nDanach können Sie einfach durch die Suche innerhalb des Startbildschirms innerhalb den hinzugefügten Server suchen und sich einloggen, insofern, dass Sie einen Account haben, der von einem Admin für Sie erstellt wurde.\n\n\n Wir wünschen Ihnen viel Spaß und Erfolg bei ihren Events!\n-Ihr Development-Team");
            }
            case 7 -> this.dispose();
            default -> setEmptyPage();
        }
    }
    private void setEmptyPage() {
        image.setIcon(Component.resizeImageProportional(Main.defaultImage, 500, 300));
        textPane.setText("");
    }
}