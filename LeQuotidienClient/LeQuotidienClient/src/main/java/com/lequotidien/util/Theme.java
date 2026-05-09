package com.lequotidien.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Constantes visuelles et utilitaires d'interface pour Le•Quotidien.
 * Centralise couleurs, polices et helpers Swing pour garantir
 * une charte graphique cohérente.
 */
public final class Theme {

    // -------------------------------------------------------------------------
    // Palette
    // -------------------------------------------------------------------------
    public static final Color BACKGROUND      = new Color(0xF8F7F4);
    public static final Color SURFACE         = Color.WHITE;
    public static final Color PRIMARY         = new Color(0x1A1A2E);
    public static final Color ACCENT          = new Color(0xC0392B);
    public static final Color ACCENT_HOVER    = new Color(0x96281B);
    public static final Color TEXT_PRIMARY    = new Color(0x1A1A2E);
    public static final Color TEXT_SECONDARY  = new Color(0x6B6B7B);
    public static final Color TEXT_LIGHT      = new Color(0xAAAAAA);
    public static final Color BORDER_COLOR    = new Color(0xE0DDD8);
    public static final Color SUCCESS         = new Color(0x27AE60);
    public static final Color DANGER          = new Color(0xC0392B);
    public static final Color WARNING         = new Color(0xF39C12);
    public static final Color ROW_EVEN        = new Color(0xFAF9F6);
    public static final Color ROW_ODD         = Color.WHITE;
    public static final Color ROW_SELECTED    = new Color(0xFDE8E6);
    public static final Color HEADER_BG       = PRIMARY;
    public static final Color HEADER_FG       = Color.WHITE;

    // -------------------------------------------------------------------------
    // Polices
    // -------------------------------------------------------------------------
    public static final Font FONT_TITLE   = new Font("Serif",     Font.BOLD,  28);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD,  16);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  12);
    public static final Font FONT_MONO    = new Font("Monospaced",Font.PLAIN, 12);

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------
    public static final int BORDER_RADIUS = 8;
    public static final Insets PADDING_PANEL  = new Insets(20, 24, 20, 24);
    public static final Insets PADDING_BUTTON = new Insets(8, 18, 8, 18);

    private Theme() {}

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Crée un bouton stylisé avec la couleur d'accentuation. */
    public static JButton accentButton(String text) {
        return styledButton(text, ACCENT, Color.WHITE, ACCENT_HOVER);
    }

    /** Crée un bouton stylisé avec une couleur personnalisée. */
    public static JButton styledButton(String text, Color bg, Color fg, Color hover) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_LABEL);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(PADDING_BUTTON);
        return btn;
    }

    /** Crée un bouton secondaire (outline style). */
    public static JButton outlineButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, BORDER_RADIUS, BORDER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_LABEL);
        btn.setForeground(color);
        btn.setBackground(SURFACE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(PADDING_BUTTON);
        return btn;
    }

    /** Crée un champ de texte stylisé. */
    public static JTextField styledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    /** Crée un champ de mot de passe stylisé. */
    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    /** Crée un label de section. */
    public static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /** Crée un séparateur horizontal discret. */
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        return sep;
    }

    /** Crée un panneau avec fond blanc et bord arrondi simulé. */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 24, 20, 24)));
        return panel;
    }

    /** Configure l'apparence globale Swing (Look & Feel système). */
    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Utilise le L&F par défaut si le système n'est pas disponible
        }
        UIManager.put("Button.arc", BORDER_RADIUS);
        UIManager.put("Table.gridColor", BORDER_COLOR);
        UIManager.put("Table.selectionBackground", ROW_SELECTED);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("ScrollBar.width", 8);
    }

    /** Construit un Border vide à partir d'une valeur uniforme. */
    public static Border emptyBorder(int all) {
        return new EmptyBorder(all, all, all, all);
    }
}
