package com.lequotidien;

import com.lequotidien.ui.LoginFrame;

import javax.swing.*;

/**
 * Point d'entrée de l'application cliente Le•Quotidien.
 * Lance la fenêtre de connexion sur l'EDT Swing.
 */
public class App {

    public static void main(String[] args) {
        // Propriétés système pour un rendu plus net sur macOS et HiDPI
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("sun.java2d.uiScale", "1");

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.attachListeners();
            frame.setVisible(true);
        });
    }
}
