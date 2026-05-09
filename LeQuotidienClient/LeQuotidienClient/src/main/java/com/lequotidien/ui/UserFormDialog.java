package com.lequotidien.ui;

import com.lequotidien.model.Utilisateur;
import com.lequotidien.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialogue modal de création ou de modification d'un utilisateur.
 * En mode édition, les champs sont pré-remplis et le mot de passe est facultatif.
 */
public class UserFormDialog extends JDialog {

    public enum Mode { AJOUTER, MODIFIER }

    private final Mode mode;

    private JTextField    nomField;
    private JTextField    emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JLabel        passwordNote;

    private boolean confirmed = false;

    public UserFormDialog(Frame parent, Mode mode, Utilisateur prefill) {
        super(parent, mode == Mode.AJOUTER ? "Ajouter un utilisateur" : "Modifier l'utilisateur", true);
        this.mode = mode;
        buildUI(prefill);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------

    private void buildUI(Utilisateur prefill) {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.SURFACE);

        root.add(buildFormPanel(prefill), BorderLayout.CENTER);
        root.add(buildButtonPanel(),      BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildFormPanel(Utilisateur u) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(new EmptyBorder(24, 28, 16, 28));

        GridBagConstraints lc = labelConstraints();
        GridBagConstraints fc = fieldConstraints();

        // Nom
        panel.add(Theme.sectionLabel("Nom *"), setRow(lc, 0));
        nomField = Theme.styledTextField(24);
        if (u != null) nomField.setText(u.getNom());
        panel.add(nomField, setRow(fc, 0));

        // Email
        panel.add(Theme.sectionLabel("Email *"), setRow(lc, 1));
        emailField = Theme.styledTextField(24);
        if (u != null) emailField.setText(u.getEmail());
        panel.add(emailField, setRow(fc, 1));

        // Mot de passe
        String pwdLabel = mode == Mode.MODIFIER ? "Mot de passe" : "Mot de passe *";
        panel.add(Theme.sectionLabel(pwdLabel), setRow(lc, 2));
        passwordField = Theme.styledPasswordField(24);
        panel.add(passwordField, setRow(fc, 2));

        if (mode == Mode.MODIFIER) {
            GridBagConstraints nc = fieldConstraints();
            nc.gridy = 3;
            nc.insets = new Insets(0, 0, 8, 0);
            passwordNote = new JLabel("Laisser vide pour conserver le mot de passe actuel");
            passwordNote.setFont(Theme.FONT_SMALL);
            passwordNote.setForeground(Theme.TEXT_LIGHT);
            panel.add(passwordNote, nc);
        }

        // Rôle
        int roleRow = mode == Mode.MODIFIER ? 4 : 3;
        panel.add(Theme.sectionLabel("Rôle *"), setRow(lc, roleRow));
        roleCombo = new JComboBox<>(new String[]{"editeur", "administrateur"});
        roleCombo.setFont(Theme.FONT_BODY);
        if (u != null) roleCombo.setSelectedItem(u.getRole());
        GridBagConstraints cc = fieldConstraints();
        cc.gridy = roleRow;
        panel.add(roleCombo, cc);

        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        JButton cancel = Theme.outlineButton("Annuler", Theme.TEXT_SECONDARY);
        cancel.addActionListener(e -> dispose());

        String label = mode == Mode.AJOUTER ? "Ajouter" : "Enregistrer";
        JButton confirm = Theme.accentButton("  " + label + "  ");
        confirm.addActionListener(e -> onConfirm());

        // Entrée = confirmer
        getRootPane().setDefaultButton(confirm);

        panel.add(cancel);
        panel.add(confirm);
        return panel;
    }

    // -------------------------------------------------------------------------
    // Logique
    // -------------------------------------------------------------------------

    private void onConfirm() {
        String nom   = nomField.getText().trim();
        String email = emailField.getText().trim();
        String pwd   = new String(passwordField.getPassword());
        String role  = (String) roleCombo.getSelectedItem();

        if (nom.isEmpty()) {
            showFieldError("Le nom est obligatoire.", nomField);
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            showFieldError("Veuillez saisir un email valide.", emailField);
            return;
        }
        if (mode == Mode.AJOUTER && pwd.isEmpty()) {
            showFieldError("Le mot de passe est obligatoire.", passwordField);
            return;
        }

        confirmed = true;
        dispose();
    }

    private void showFieldError(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "Champ requis", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
    }

    // -------------------------------------------------------------------------
    // Accesseurs résultats
    // -------------------------------------------------------------------------

    public boolean isConfirmed()   { return confirmed; }
    public String  getNom()        { return nomField.getText().trim(); }
    public String  getEmail()      { return emailField.getText().trim(); }
    public String  getPassword()   { return new String(passwordField.getPassword()); }
    public String  getRole()       { return (String) roleCombo.getSelectedItem(); }

    // -------------------------------------------------------------------------
    // GridBagConstraints helpers
    // -------------------------------------------------------------------------

    private GridBagConstraints labelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 0;
        c.anchor  = GridBagConstraints.WEST;
        c.insets  = new Insets(8, 0, 4, 12);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.3;
        return c;
    }

    private GridBagConstraints fieldConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 1;
        c.anchor  = GridBagConstraints.WEST;
        c.insets  = new Insets(8, 0, 4, 0);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.7;
        return c;
    }

    private GridBagConstraints setRow(GridBagConstraints c, int row) {
        c.gridy = row;
        return c;
    }
}
