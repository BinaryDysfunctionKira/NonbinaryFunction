package com.binary_dysfunction;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class ChatPanel {

    int count = 0;

    public ChatPanel() throws InterruptedException, InvocationTargetException {

        Runnable run = () -> {
            while (true) {
                System.out.println("Check" + count);
                count++;
                // try {
                //     Thread.sleep(1000);
                // } catch (InterruptedException ex) {
                //     System.getLogger(ChatPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                // }
            }
        };
        SwingUtilities.invokeAndWait(run);
    }

    public JPanel getChatPanel() {

        JPanel mainPanel = new JPanel(new BorderLayout());

        return mainPanel;
    }
}
