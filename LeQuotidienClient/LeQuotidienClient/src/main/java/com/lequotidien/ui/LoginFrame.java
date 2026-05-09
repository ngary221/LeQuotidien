package com.lequotidien.ui;

import com.lequotidien.model.SoapResponse;
import com.lequotidien.model.Utilisateur;
import com.lequotidien.soap.SoapClient;
import com.lequotidien.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Fenêtre de connexion.
 * Saisit l'URL du serveur, l'email et le mot de passe,
 * puis invoque authentifierUtilisateur via le service SOAP.
 */
public class LoginFrame extends JFrame {

    private JTextField    serverField;
    private JTextField    emailField;
    private JPasswordField passwordField;
    private JButton       loginButton;
    private JLabel        statusLabel;

    public LoginFrame() {
        super("Le•Quotidien — Connexion");
        Theme.applyLookAndFeel();
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    // -------------------------------------------------------------------------
    // Construction de l'interface
    // -------------------------------------------------------------------------

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildForm(),    BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setContentPane(root);
    }

    /** Bandeau supérieur avec logo textuel. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(28, 36, 22, 36));

        JLabel logo = new JLabel("Le•Quotidien");
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Administration des utilisateurs");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(new Color(0xCCCCDD));

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 4));
        texts.setOpaque(false);
        texts.add(logo);
        texts.add(sub);
        header.add(texts, BorderLayout.WEST);

        return header;
    }

    /** Formulaire de connexion. */
    private JPanel buildForm() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.BACKGROUND);
        outer.setBorder(new EmptyBorder(32, 40, 16, 40));

        JPanel card = Theme.cardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(400, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.insets    = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 2;
        gbc.weightx   = 1.0;

        // Titre
        JLabel title = new JLabel("Connexion");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        card.add(title, gbc);

        card.add(Theme.separator(), setGrid(gbc, 1));

        // Serveur
        card.add(Theme.sectionLabel("URL du serveur"), setGrid(gbc, 2));
        serverField = Theme.styledTextField(28);
        serverField.setText("http://localhost/newsite/public");
        serverField.setToolTipText("Ex : http://localhost/newsite/public");
        card.add(serverField, setGrid(gbc, 3));

        // Email
        card.add(Theme.sectionLabel("Email"), setGrid(gbc, 4));
        emailField = Theme.styledTextField(28);
        emailField.setToolTipText("Adresse email de l'administrateur");
        card.add(emailField, setGrid(gbc, 5));

        // Mot de passe
        card.add(Theme.sectionLabel("Mot de passe"), setGrid(gbc, 6));
        passwordField = Theme.styledPasswordField(28);
        card.add(passwordField, setGrid(gbc, 7));

        // Bouton
        gbc.gridy  = 8;
        gbc.insets = new Insets(16, 0, 4, 0);
        loginButton = Theme.accentButton("  Se connecter  ");
        loginButton.setPreferredSize(new Dimension(400, 38));
        card.add(loginButton, gbc);

        // Statut
        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.DANGER);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel, setGrid(gbc, 9));

        outer.add(card);
        return outer;
    }

    /** Pied de page discret. */
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Theme.BACKGROUND);
        footer.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel note = new JLabel("Accès réservé aux administrateurs");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.TEXT_LIGHT);
        footer.add(note);
        return footer;
    }

    private GridBagConstraints setGrid(GridBagConstraints gbc, int row) {
        gbc.gridy  = row;
        gbc.insets = new Insets(4, 0, 4, 0);
        return gbc;
    }

    // -------------------------------------------------------------------------
    // Logique de connexion
    // -------------------------------------------------------------------------

    /** Branche les listeners après construction. */
    public void attachListeners() {
        ActionListener doLogin = e -> attemptLogin();
        loginButton.addActionListener(doLogin);

        // Touche Entrée sur n'importe quel champ
        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        };
        serverField.addKeyListener(enterKey);
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);
    }

    private void attemptLogin() {
        String server   = serverField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validation basique côté client
        if (server.isEmpty()) {
            showError("Veuillez saisir l'URL du serveur.");
            serverField.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            showError("Veuillez saisir votre email.");
            emailField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Veuillez saisir votre mot de passe.");
            passwordField.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setText("Connexion en cours…");

        // Appel SOAP dans un thread séparé pour ne pas bloquer l'EDT
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            SoapResponse response;
            Utilisateur  connectedUser;
            String       token;

            @Override
            protected Void doInBackground() {
                try {
                    SoapClient client = new SoapClient(server);
                    response = client.authentifierUtilisateur(email, password);

                    if (response.isSucces()) {
                        // Extrait les données JSON de l'utilisateur authentifié
                        connectedUser = parseAuthData(response.getData());
                        // Le token est demandé séparément (saisi par l'admin)
                    }
                } catch (Exception ex) {
                    response = new SoapResponse(false, "Erreur : " + ex.getMessage(), null);
                }
                return null;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                if (response == null || !response.isSucces()) {
                    showError(response != null ? response.getMessage() : "Erreur inconnue.");
                    return;
                }
                if (connectedUser == null || !"administrateur".equalsIgnoreCase(connectedUser.getRole())) {
                    showError("Accès refusé. Seuls les administrateurs peuvent se connecter.");
                    return;
                }
                // Succès : demande le token API puis ouvre le dashboard
                askForToken(server, connectedUser);
            }
        };
        worker.execute();
    }

    /**
     * Après authentification réussie, demande le token API à l'utilisateur.
     * Le token est distinct des credentials de connexion.
     */
    private void askForToken(String server, Utilisateur user) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 8));
        panel.setBorder(new EmptyBorder(8, 0, 0, 0));
        panel.add(new JLabel("<html>Saisissez le <b>token API</b> fourni par l'administrateur du site :</html>"));
        JPasswordField tokenField = Theme.styledPasswordField(30);
        panel.add(tokenField);

        int result = JOptionPane.showConfirmDialog(
                this, panel,
                "Token API requis",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            showError("Token requis pour accéder à la gestion des utilisateurs.");
            return;
        }

        String token = new String(tokenField.getPassword()).trim();
        if (token.isEmpty()) {
            showError("Le token ne peut pas être vide.");
            return;
        }

        // Ouvre le dashboard
        dispose();
        new MainFrame(server, user, token).setVisible(true);
    }

    /** Parse le JSON minimal retourné dans le champ data de authentifierUtilisateur. */
    private Utilisateur parseAuthData(String json) {
        if (json == null) return null;
        Utilisateur u = new Utilisateur();
        u.setId(    parseInt(extractJson(json, "id"), 0));
        u.setNom(   extractJson(json, "nom"));
        u.setEmail( extractJson(json, "email"));
        u.setRole(  extractJson(json, "role"));
        return u;
    }

    private String extractJson(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|([\\w.@+-]+))");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) return m.group(1) != null ? m.group(1) : m.group(2);
        return "";
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private void showError(String msg) {
        statusLabel.setForeground(Theme.DANGER);
        statusLabel.setText(msg);
    }
}
