package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.service.SessionService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindow extends JFrame {
    private static final String CARD_MENU = "menu";
    private static final String CARD_HISTORY = "history";
    private static final String CARD_STATS = "stats";
    private static final String CARD_OPTIONS = "options";

    private final SessionService sessionService = new SessionService();
    private final SessionHistoryPanel historyPanel = new SessionHistoryPanel();
    private final ZoneStatsPanel statsPanel = new ZoneStatsPanel();
    private final JPanel cardPanel = new JPanel(new CardLayout());

    public MainWindow() {
        super("Dofus Retro Rentabilizer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);
        setContentPane(buildRoot());
        showMenu();
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemePalette.NIGHT);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCards(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint paint = new GradientPaint(
                        0, 0, ThemePalette.JADE,
                        getWidth(), getHeight(), ThemePalette.EMERALD);
                g2.setPaint(paint);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(100, 150));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        JLabel title = new JLabel("Rentabilizer 2.0");
        title.setFont(ThemePalette.titleFont());
        title.setForeground(ThemePalette.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Guilde Evolution • Serveur Boune • Dofus 1.29");
        subtitle.setFont(ThemePalette.subtitleFont());
        subtitle.setForeground(ThemePalette.TEXT_SECONDARY);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(6));
        texts.add(subtitle);
        header.add(texts, BorderLayout.WEST);

        return header;
    }

    private JComponent buildCards() {
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));
        cardPanel.add(buildMenuScreen(), CARD_MENU);
        cardPanel.add(buildHistoryScreen(), CARD_HISTORY);
        cardPanel.add(buildStatsScreen(), CARD_STATS);
        cardPanel.add(buildPlaceholderScreen("Options", "Reglages et exports arrives tres prochainement."), CARD_OPTIONS);
        return cardPanel;
    }

    private JComponent buildMenuScreen() {
        JPanel screen = new JPanel(new GridBagLayout());
        screen.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(2, 2, 28, 28));
        grid.setOpaque(false);
        grid.add(createMenuCard("Mode farm", "Lancer une session instantanement", this::openSessionDialog));
        grid.add(createMenuCard("Infos zones", "Classement des meilleures zones", this::showStats));
        grid.add(createMenuCard("Historique des farm", "Consultez vos 25 dernieres sessions", this::showHistory));
        grid.add(createMenuCard("Options", "Theme, export, sauvegardes", () -> showCard(CARD_OPTIONS)));

        screen.add(grid);
        return screen;
    }

    private JPanel createMenuCard(String title, String subtitle, Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(240, 190));
        card.setBackground(new Color(32, 49, 57));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 108, 97), 2, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ThemePalette.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("<html><div style='width:180px;'>" + subtitle + "</div></html>");
        subtitleLabel.setFont(ThemePalette.bodyFont());
        subtitleLabel.setForeground(ThemePalette.TEXT_SECONDARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(subtitleLabel, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(45, 68, 80));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(new Color(32, 49, 57));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        return card;
    }

    private JComponent buildHistoryScreen() {
        return buildContentScreen("Historique des sessions",
                "Suivez vos derniers runs en detail et analysez les ratios K/h.",
                historyPanel,
                null);
    }

    private JComponent buildStatsScreen() {
        return buildContentScreen("Informations sur les zones",
                "Classement dynamique des zones en fonction de votre experience.",
                statsPanel,
                null);
    }

    private JComponent buildPlaceholderScreen(String title, String description) {
        JTextArea text = new JTextArea(description);
        text.setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setForeground(ThemePalette.TEXT_SECONDARY);
        text.setFont(ThemePalette.bodyFont());

        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setOpaque(false);
        placeholder.add(text, BorderLayout.CENTER);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemePalette.PANEL);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.add(placeholder, BorderLayout.CENTER);

        return buildContentScreen(title, "Cette section arrive bientot.", card, null);
    }

    private JComponent buildContentScreen(String title, String description, JComponent content, Runnable backAction) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(26, 38, 46));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JButton backButton = new JButton("← Retour au menu");
        backButton.setBackground(ThemePalette.GOLD);
        backButton.setForeground(Color.DARK_GRAY);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            } else {
                showMenu();
            }
        });

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(ThemePalette.TEXT_PRIMARY);
        titleLabel.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 20f));
        JLabel descLabel = new JLabel(description);
        descLabel.setForeground(ThemePalette.TEXT_SECONDARY);
        descLabel.setFont(ThemePalette.bodyFont());

        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(4));
        titles.add(descLabel);

        topBar.add(backButton, BorderLayout.WEST);
        topBar.add(titles, BorderLayout.CENTER);

        JPanel bodyWrapper = new JPanel(new BorderLayout());
        bodyWrapper.setBackground(ThemePalette.PANEL);
        bodyWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bodyWrapper.add(content, BorderLayout.CENTER);

        container.add(topBar, BorderLayout.NORTH);
        container.add(bodyWrapper, BorderLayout.CENTER);

        return container;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(ThemePalette.OBSIDIAN);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel("Dofus Retro Rentabilizer • Toutes les donnees restent sur votre machine");
        label.setForeground(ThemePalette.TEXT_SECONDARY);
        label.setFont(ThemePalette.bodyFont());
        footer.add(label);
        return footer;
    }

    private void openSessionDialog() {
        SessionFormDialog dialog = new SessionFormDialog(this, sessionService, this::refreshData);
        dialog.setVisible(true);
    }

    private void showHistory() {
        refreshData();
        showCard(CARD_HISTORY);
    }

    private void showStats() {
        refreshData();
        showCard(CARD_STATS);
    }

    private void showMenu() {
        showCard(CARD_MENU);
    }

    private void showCard(String card) {
        CardLayout layout = (CardLayout) cardPanel.getLayout();
        layout.show(cardPanel, card);
    }

    public final void refreshData() {
        try {
            historyPanel.setSessions(sessionService.latestSessions(25));
            statsPanel.setStats(sessionService.zoneStats());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les donnees: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
