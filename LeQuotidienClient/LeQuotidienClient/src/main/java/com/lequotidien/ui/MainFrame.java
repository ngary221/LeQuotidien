package com.lequotidien.ui;

import com.lequotidien.model.SoapResponse;
import com.lequotidien.model.Utilisateur;
import com.lequotidien.soap.SoapClient;
import com.lequotidien.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Tableau de bord principal de gestion des utilisateurs.
 * Accessible uniquement après authentification en tant qu'administrateur.
 */
public class MainFrame extends JFrame {

    // -------------------------------------------------------------------------
    // État
    // -------------------------------------------------------------------------
    private final String       serverUrl;
    private final Utilisateur  currentUser;
    private final String       apiToken;
    private final SoapClient   soapClient;

    private List<Utilisateur> utilisateurs = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Composants UI
    // -------------------------------------------------------------------------
    private JTable           table;
    private UserTableModel   tableModel;
    private JLabel           statusBar;
    private JTextField       searchField;
    private JButton          btnAjouter;
    private JButton          btnModifier;
    private JButton          btnSupprimer;
    private JButton          btnRefresh;
    private JLabel           totalLabel;

    public MainFrame(String serverUrl, Utilisateur currentUser, String apiToken) {
        super("Le•Quotidien — Gestion des utilisateurs");
        this.serverUrl   = serverUrl;
        this.currentUser = currentUser;
        this.apiToken    = apiToken;
        this.soapClient  = new SoapClient(serverUrl);

        buildUI();
        setSize(900, 580);
        setMinimumSize(new Dimension(750, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Chargement initial
        SwingUtilities.invokeLater(this::loadUtilisateurs);
    }

    // -------------------------------------------------------------------------
    // Construction UI
    // -------------------------------------------------------------------------

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildContent(),   BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    /** Barre supérieure : logo + info utilisateur + déconnexion. */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PRIMARY);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Gauche : logo
        JLabel logo = new JLabel("Le•Quotidien");
        logo.setFont(new Font("Serif", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        bar.add(logo, BorderLayout.WEST);

        // Droite : info utilisateur + déconnexion
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel userInfo = new JLabel("👤  " + currentUser.getNom() + "  ·  " + currentUser.getRole());
        userInfo.setFont(Theme.FONT_SMALL);
        userInfo.setForeground(new Color(0xCCCCDD));

        JButton logout = Theme.outlineButton("Déconnexion", new Color(0xAAAAAA));
        logout.setFont(Theme.FONT_SMALL);
        logout.addActionListener(e -> onLogout());

        right.add(userInfo);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    /** Zone principale : toolbar + tableau. */
    private JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 20, 0, 20));

        // Titre section
        JLabel title = new JLabel("Utilisateurs");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);

        totalLabel = new JLabel("");
        totalLabel.setFont(Theme.FONT_SMALL);
        totalLabel.setForeground(Theme.TEXT_SECONDARY);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(title,      BorderLayout.WEST);
        titleRow.add(totalLabel, BorderLayout.EAST);
        panel.add(titleRow, BorderLayout.NORTH);

        panel.add(buildToolbar(), BorderLayout.NORTH);  // remplace titleRow ci-dessus
        panel.add(buildTablePanel(), BorderLayout.CENTER);

        // Reconstruction avec le bon ordre
        panel.removeAll();
        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(titleRow,       BorderLayout.NORTH);
        topSection.add(buildToolbar(), BorderLayout.SOUTH);

        panel.add(topSection,         BorderLayout.NORTH);
        panel.add(buildTablePanel(),  BorderLayout.CENTER);

        return panel;
    }

    /** Barre d'outils : recherche + actions. */
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, 0, 10, 0));

        // Recherche
        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍");
        searchField = Theme.styledTextField(20);
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher…");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        searchPanel.add(searchIcon,  BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        btnRefresh   = Theme.outlineButton("↻  Actualiser", Theme.TEXT_SECONDARY);
        btnAjouter   = Theme.accentButton("＋  Ajouter");
        btnModifier  = Theme.outlineButton("✎  Modifier",  Theme.PRIMARY);
        btnSupprimer = Theme.outlineButton("✕  Supprimer", Theme.DANGER);

        btnModifier.setEnabled(false);
        btnSupprimer.setEnabled(false);

        btnRefresh.addActionListener(e -> loadUtilisateurs());
        btnAjouter.addActionListener(e -> onAjouter());
        btnModifier.addActionListener(e -> onModifier());
        btnSupprimer.addActionListener(e -> onSupprimer());

        actions.add(btnRefresh);
        actions.add(btnModifier);
        actions.add(btnSupprimer);
        actions.add(btnAjouter);

        bar.add(searchPanel, BorderLayout.WEST);
        bar.add(actions,     BorderLayout.EAST);
        return bar;
    }

    /** Tableau des utilisateurs. */
    private JScrollPane buildTablePanel() {
        tableModel = new UserTableModel();
        table = new JTable(tableModel);
        styleTable();

        // Sélection → activer boutons
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() >= 0;
            btnModifier.setEnabled(selected);
            btnSupprimer.setEnabled(selected);
        });

        // Double-clic → modifier
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) onModifier();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        scroll.getViewport().setBackground(Theme.SURFACE);
        return scroll;
    }

    private void styleTable() {
        table.setRowHeight(40);
        table.setFont(Theme.FONT_BODY);
        table.setGridColor(Theme.BORDER_COLOR);
        table.setSelectionBackground(Theme.ROW_SELECTED);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        // En-tête
        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_LABEL);
        header.setBackground(Theme.HEADER_BG);
        header.setForeground(Theme.HEADER_FG);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        // Largeurs colonnes
        int[] widths = {50, 200, 220, 120, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        // Renderer alterné + centrage colonne statut
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
    }

    /** Barre de statut en bas. */
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xEEEDEA));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(5, 16, 5, 16)));

        statusBar = new JLabel("Prêt");
        statusBar.setFont(Theme.FONT_SMALL);
        statusBar.setForeground(Theme.TEXT_SECONDARY);
        bar.add(statusBar, BorderLayout.WEST);

        JLabel serverLabel = new JLabel("Serveur : " + serverUrl);
        serverLabel.setFont(Theme.FONT_SMALL);
        serverLabel.setForeground(Theme.TEXT_LIGHT);
        bar.add(serverLabel, BorderLayout.EAST);

        return bar;
    }

    // -------------------------------------------------------------------------
    // Opérations SOAP
    // -------------------------------------------------------------------------

    private void loadUtilisateurs() {
        setStatus("Chargement…", false);
        btnRefresh.setEnabled(false);

        SwingWorker<List<Utilisateur>, Void> worker = new SwingWorker<>() {
            Exception error;

            @Override
            protected List<Utilisateur> doInBackground() throws Exception {
                return soapClient.listerUtilisateurs(apiToken);
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    utilisateurs = get();
                    tableModel.setData(utilisateurs);
                    totalLabel.setText(utilisateurs.size() + " utilisateur(s)");
                    setStatus("✔  " + utilisateurs.size() + " utilisateurs chargés.", false);
                } catch (Exception e) {
                    setStatus("Erreur : " + e.getCause().getMessage(), true);
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Impossible de charger les utilisateurs :\n" + e.getCause().getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onAjouter() {
        UserFormDialog dlg = new UserFormDialog(this, UserFormDialog.Mode.AJOUTER, null);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;

        setStatus("Ajout en cours…", false);
        SwingWorker<SoapResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected SoapResponse doInBackground() {
                return soapClient.ajouterUtilisateur(
                        apiToken, dlg.getNom(), dlg.getEmail(),
                        dlg.getPassword(), dlg.getRole());
            }
            @Override protected void done() {
                try {
                    SoapResponse r = get();
                    if (r.isSucces()) {
                        setStatus("✔  Utilisateur ajouté.", false);
                        loadUtilisateurs();
                    } else {
                        setStatus("Erreur : " + r.getMessage(), true);
                        JOptionPane.showMessageDialog(MainFrame.this,
                                r.getMessage(), "Échec de l'ajout", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { handleWorkerException(e); }
            }
        };
        worker.execute();
    }

    private void onModifier() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        Utilisateur u = tableModel.getUserAt(convertRow(row));
        UserFormDialog dlg = new UserFormDialog(this, UserFormDialog.Mode.MODIFIER, u);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;

        setStatus("Modification en cours…", false);
        SwingWorker<SoapResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected SoapResponse doInBackground() {
                return soapClient.modifierUtilisateur(
                        apiToken, u.getId(),
                        dlg.getNom(), dlg.getEmail(), dlg.getRole());
            }
            @Override protected void done() {
                try {
                    SoapResponse r = get();
                    if (r.isSucces()) {
                        setStatus("✔  Utilisateur modifié.", false);
                        loadUtilisateurs();
                    } else {
                        setStatus("Erreur : " + r.getMessage(), true);
                        JOptionPane.showMessageDialog(MainFrame.this,
                                r.getMessage(), "Échec de la modification", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { handleWorkerException(e); }
            }
        };
        worker.execute();
    }

    private void onSupprimer() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        Utilisateur u = tableModel.getUserAt(convertRow(row));
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Voulez-vous vraiment supprimer <b>" + u.getNom() + "</b> ?<br>" +
                "Cette action est irréversible.</html>",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        setStatus("Suppression en cours…", false);
        SwingWorker<SoapResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected SoapResponse doInBackground() {
                return soapClient.supprimerUtilisateur(apiToken, u.getId());
            }
            @Override protected void done() {
                try {
                    SoapResponse r = get();
                    if (r.isSucces()) {
                        setStatus("✔  Utilisateur supprimé.", false);
                        loadUtilisateurs();
                    } else {
                        setStatus("Erreur : " + r.getMessage(), true);
                        JOptionPane.showMessageDialog(MainFrame.this,
                                r.getMessage(), "Échec de la suppression", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { handleWorkerException(e); }
            }
        };
        worker.execute();
    }

    private void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vous déconnecter ?",
                "Déconnexion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        dispose();
        LoginFrame login = new LoginFrame();
        login.attachListeners();
        login.setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Recherche / filtre
    // -------------------------------------------------------------------------

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            tableModel.setData(utilisateurs);
        } else {
            List<Utilisateur> filtered = utilisateurs.stream()
                    .filter(u -> u.getNom().toLowerCase().contains(query)
                              || u.getEmail().toLowerCase().contains(query)
                              || u.getRole().toLowerCase().contains(query))
                    .toList();
            tableModel.setData(filtered);
        }
        totalLabel.setText(tableModel.getRowCount() + " utilisateur(s)");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private int convertRow(int viewRow) {
        return table.convertRowIndexToModel(viewRow);
    }

    private void setStatus(String msg, boolean error) {
        statusBar.setForeground(error ? Theme.DANGER : Theme.TEXT_SECONDARY);
        statusBar.setText(msg);
    }

    private void handleWorkerException(Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        setStatus("Erreur : " + cause.getMessage(), true);
        JOptionPane.showMessageDialog(this,
                cause.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    // Modèle de tableau
    // =========================================================================

    private static class UserTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"ID", "Nom", "Email", "Rôle", "Actif"};
        private List<Utilisateur> data = new ArrayList<>();

        void setData(List<Utilisateur> list) {
            this.data = new ArrayList<>(list);
            fireTableDataChanged();
        }

        Utilisateur getUserAt(int row) { return data.get(row); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }
        @Override public boolean isCellEditable(int row, int col) { return false; }

        @Override
        public Object getValueAt(int row, int col) {
            Utilisateur u = data.get(row);
            return switch (col) {
                case 0 -> u.getId();
                case 1 -> u.getNom();
                case 2 -> u.getEmail();
                case 3 -> u.getRole();
                case 4 -> u.isActif();
                default -> "";
            };
        }
    }

    // =========================================================================
    // Renderers personnalisés
    // =========================================================================

    /** Lignes alternées claire/blanche avec padding. */
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, col);
            setFont(Theme.FONT_BODY);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            if (!selected) {
                setBackground(row % 2 == 0 ? Theme.ROW_EVEN : Theme.ROW_ODD);
                setForeground(Theme.TEXT_PRIMARY);
            }
            return this;
        }
    }

    /** Badge coloré pour la colonne Actif. */
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int col) {

            boolean actif = value instanceof Boolean && (Boolean) value;
            JLabel label = new JLabel(actif ? "Actif" : "Inactif");
            label.setFont(Theme.FONT_SMALL);
            label.setForeground(Color.WHITE);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBackground(actif ? Theme.SUCCESS : Theme.TEXT_LIGHT);
            label.setBorder(new EmptyBorder(3, 10, 3, 10));

            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setBackground(selected ? Theme.ROW_SELECTED : (row % 2 == 0 ? Theme.ROW_EVEN : Theme.ROW_ODD));
            wrapper.add(label);
            return wrapper;
        }
    }
}
