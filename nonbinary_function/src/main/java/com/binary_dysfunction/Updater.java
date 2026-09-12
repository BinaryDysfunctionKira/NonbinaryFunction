package com.binary_dysfunction;

import javax.swing.SwingUtilities;

public final class Updater {

    // private final AtomicInteger count = new AtomicInteger(0);

    private Thread updateThread;
    private volatile boolean running = false;
    private volatile int counter = 0;

    public Updater() {
        startUpdating();
    }

    public void startUpdating() {
        if (updateThread != null && updateThread.isAlive()) {
            return;
        }

        running = true;

        updateThread = new Thread(() -> {
            while (running) {

                // Push the update onto the EDT — never touch Swing components directly here
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Update"+counter);
                    if (counter % 20 == 0) Toast.show(HomeFrame.frame, "Message", "You got mail!", 5000, Toast.Position.BOTTOM_RIGHT, true);
                    counter++;
                });
                try {
                    Thread.sleep(500); // throttle — adjust to whatever update rate you need
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "updater");

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void stopUpdating() {
        running = false;
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }
}