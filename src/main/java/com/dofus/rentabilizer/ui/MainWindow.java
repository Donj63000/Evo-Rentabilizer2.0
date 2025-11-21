package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.SessionRecord;
import com.dofus.rentabilizer.service.SessionService;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private static final String CARD_MENU = "menu";
    private static final String CARD_HISTORY = "history";
    private static final String CARD_STATS = "stats";
    private static final String CARD_OPTIONS = "options";
    private static final String CARD_FARM = "farm";

    private final SessionService sessionService = new SessionService();
    private final SessionHistoryPanel historyPanel = new SessionHistoryPanel();
    private final ZoneStatsPanel statsPanel = new ZoneStatsPanel();
    private final ModeFarmPanel modeFarmPanel =
            new ModeFarmPanel(sessionService, this::showMenu, this::handleSessionSaved);
    private final JPanel cardPanel = new JPanel(new CardLayout());

    public MainWindow() {
        super("Dofus Retro Rentabilizer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);
        setContentPane(buildRoot());
        showMenu();
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, ThemePalette.NIGHT, 0, getHeight(), ThemePalette.OBSIDIAN));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCards(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(12, 34, 40),
                        getWidth(), getHeight(), ThemePalette.JADE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                GradientPaint highlight = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 60),
                        0, getHeight() / 2f, new Color(255, 255, 255, 0));
                g2.setPaint(highlight);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 216, 120, 90));
                g2.fillRoundRect(20, getHeight() - 40, getWidth() - 40, 30, 60, 60);
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRect(0, getHeight() - 6, getWidth(), 6);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(100, 160));
        header.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        GradientTitleLabel title = new GradientTitleLabel("Rentabilizer 2.0");
        title.setFont(ThemePalette.titleFont().deriveFont(Font.BOLD, 46f));
        title.setGradient(new Color(255, 240, 209), ThemePalette.DARK_GOLD);
        title.setShadowColor(new Color(0, 0, 0, 140));
        title.setShadowOffset(4);

        JLabel subtitle = new JLabel("Guilde Evolution • Serveur Boune • Dofus 1.29");
        subtitle.setFont(ThemePalette.subtitleFont());
        subtitle.setForeground(ThemePalette.TEXT_SECONDARY);

        JLabel tagline = new JLabel("Optimisez chaque session de farm grace a vos propres donnees");
        tagline.setFont(ThemePalette.bodyFont());
        tagline.setForeground(ThemePalette.TEXT_PRIMARY);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(6));
        texts.add(subtitle);
        texts.add(Box.createVerticalStrut(6));
        texts.add(tagline);
        header.add(texts, BorderLayout.WEST);

        RoundedPanel heroChip = new RoundedPanel(30);
        heroChip.setShadowEnabled(false);
        heroChip.setGradient(new Color(245, 208, 116, 220), new Color(236, 175, 73, 220));
        heroChip.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel heroText = new JLabel("Optimisation temps réel • Données 100% locales");
        heroText.setForeground(new Color(40, 30, 16));
        heroText.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 15f));
        heroChip.add(heroText);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(heroChip);
        header.add(center, BorderLayout.CENTER);

        Icon logo = ThemePalette.logoIcon(180, 120);
        if (logo != null) {
            JLabel logoLabel = new JLabel(logo);
            header.add(logoLabel, BorderLayout.EAST);
        }

        return header;
    }

    private JComponent buildCards() {
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cardPanel.add(buildMenuScreen(), CARD_MENU);
        cardPanel.add(buildHistoryScreen(), CARD_HISTORY);
        cardPanel.add(buildStatsScreen(), CARD_STATS);
        cardPanel.add(buildPlaceholderScreen("Options", "Reglages et exports arrives tres prochainement."), CARD_OPTIONS);
        cardPanel.add(modeFarmPanel, CARD_FARM);
        return cardPanel;
    }

    private JComponent buildMenuScreen() {
        JPanel screen = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Image bg = ThemePalette.menuBackgroundTexture();
                if (bg != null) {
                    int w = getWidth();
                    int h = getHeight();
                    int imgW = bg.getWidth(null);
                    int imgH = bg.getHeight(null);
                    if (w > 0 && h > 0 && imgW > 0 && imgH > 0) {
                        double scale = Math.max((double) w / imgW, (double) h / imgH);
                        int drawW = (int) (imgW * scale);
                        int drawH = (int) (imgH * scale);
                        int x = (w - drawW) / 2;
                        int y = (h - drawH) / 2;
                        g2.drawImage(bg, x, y, drawW, drawH, null);
                    }
                }
                g2.setPaint(new GradientPaint(0, 0, new Color(10, 14, 18, 120),
                        0, getHeight(), new Color(12, 20, 28, 160)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        screen.setOpaque(true);

        RoundedPanel gridWrapper = new RoundedPanel(30);
        gridWrapper.setFill(new Color(22, 32, 42, 180));
        gridWrapper.setLayout(new BorderLayout());
        gridWrapper.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 24));
        grid.setOpaque(false);
        grid.add(new MenuCard("Mode farm", "Lancer une session instantanement", "\u2694", this::showFarm));
        grid.add(new MenuCard("Infos zones", "Classement des meilleures zones", "\u2699", this::showStats));
        grid.add(new MenuCard("Historique des farm", "Consultez vos 25 dernieres sessions", "\u23F2", this::showHistory));
        grid.add(new MenuCard("Options", "Theme, export, sauvegardes", "\u2692", () -> showCard(CARD_OPTIONS)));

        JLabel callout = new JLabel("Choisissez un mode pour commencer votre session");
        callout.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 18f));
        callout.setForeground(ThemePalette.TEXT_PRIMARY);
        callout.setBorder(BorderFactory.createEmptyBorder(0, 6, 18, 0));
        gridWrapper.add(callout, BorderLayout.NORTH);
        gridWrapper.add(grid, BorderLayout.CENTER);
        screen.add(gridWrapper);
        return screen;
    }

    private JComponent buildHistoryScreen() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(buildHistoryToolbar(), BorderLayout.NORTH);
        container.add(historyPanel, BorderLayout.CENTER);
        return buildContentScreen("Historique des sessions",
                "Suivez vos derniers runs en detail et analysez les ratios K/h.",
                container,
                null,
                false);
    }

    private JComponent buildStatsScreen() {
        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Image bg = ThemePalette.statsBackgroundTexture();
                if (bg != null) {
                    int w = getWidth();
                    int h = getHeight();
                    int imgW = bg.getWidth(null);
                    int imgH = bg.getHeight(null);
                    if (w > 0 && h > 0 && imgW > 0 && imgH > 0) {
                        double scale = Math.max((double) w / imgW, (double) h / imgH);
                        int drawW = (int) (imgW * scale);
                        int drawH = (int) (imgH * scale);
                        int x = (w - drawW) / 2;
                        int y = (h - drawH) / 2;
                        g2.drawImage(bg, x, y, drawW, drawH, null);
                    }
                }
                g2.setPaint(new GradientPaint(0, 0, new Color(8, 14, 20, 90),
                        0, getHeight(), new Color(12, 20, 28, 130)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        content.setOpaque(true);
        content.setLayout(new BorderLayout());
        content.add(buildStatsToolbar(), BorderLayout.NORTH);
        content.add(statsPanel, BorderLayout.CENTER);
        return buildContentScreen("Informations sur les zones",
                "Classement dynamique des zones en fonction de votre experience.",
                content,
                null,
                true);
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

        RoundedPanel card = new RoundedPanel(22);
        card.setFill(new Color(30, 40, 52));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        card.add(placeholder, BorderLayout.CENTER);

        return buildContentScreen(title, "Cette section arrive bientot.", card, null, false);
    }

    private JComponent buildContentScreen(String title, String description, JComponent content, Runnable backAction, boolean translucent) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        RoundedPanel topBar = new RoundedPanel(24);
        topBar.setFill(translucent ? new Color(30, 44, 53, 170) : new Color(30, 44, 53, 235));
        topBar.setLayout(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JButton backButton = UiComponents.ghostButton("< Retour au menu");
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

        RoundedPanel bodyWrapper = new RoundedPanel(26);
        bodyWrapper.setFill(translucent ? new Color(24, 32, 40, 170) : new Color(24, 32, 40, 235));
        bodyWrapper.setLayout(new BorderLayout());
        bodyWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bodyWrapper.add(content, BorderLayout.CENTER);

        container.add(topBar, BorderLayout.NORTH);
        container.add(bodyWrapper, BorderLayout.CENTER);

        return container;
    }

    private JComponent buildHistoryToolbar() {
        RoundedPanel panel = new RoundedPanel(20);
        panel.setGradient(new Color(46, 78, 86, 200), new Color(35, 52, 62, 220));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        panel.setLayout(new BorderLayout(12, 0));
        JLabel info = new JLabel("Conseil: ajoute une session apres chaque run pour garder un historique fiable.");
        info.setForeground(ThemePalette.TEXT_PRIMARY);
        panel.add(info, BorderLayout.CENTER);
        JButton addSession = UiComponents.primaryButton("Nouvelle session");
        addSession.addActionListener(e -> openSessionDialog());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton editSession = UiComponents.ghostButton("Modifier");
        JButton deleteSession = UiComponents.ghostButton("Supprimer");
        editSession.setEnabled(false);
        deleteSession.setEnabled(false);

        historyPanel.addSelectionListener(() -> {
            boolean hasSelection = historyPanel.getSelectedSession() != null;
            editSession.setEnabled(hasSelection);
            deleteSession.setEnabled(hasSelection);
        });

        editSession.addActionListener(e -> openEditDialog());
        deleteSession.addActionListener(e -> deleteSelectedSession());

        actions.add(addSession);
        actions.add(editSession);
        actions.add(deleteSession);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JComponent buildStatsToolbar() {
        RoundedPanel panel = new RoundedPanel(20);
        panel.setGradient(new Color(54, 85, 96, 200), new Color(38, 60, 70, 220));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        panel.setLayout(new BorderLayout(12, 0));
        JLabel info = new JLabel("Classement calcule uniquement sur vos donnees enregistrées.");
        info.setForeground(ThemePalette.TEXT_PRIMARY);
        panel.add(info, BorderLayout.WEST);
        JButton refresh = UiComponents.ghostButton("Actualiser");
        refresh.addActionListener(e -> refreshData());
        panel.add(refresh, BorderLayout.EAST);
        return panel;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(ThemePalette.OBSIDIAN);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel("Dofus Retro Rentabilizer • Toutes les donnees restent sur votre machine • © Guilde Evolution");
        label.setForeground(ThemePalette.TEXT_SECONDARY);
        label.setFont(ThemePalette.bodyFont());
        footer.add(label);
        return footer;
    }

    private void openSessionDialog() {
        SessionFormDialog dialog = new SessionFormDialog(this, sessionService, this::refreshData);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        var session = historyPanel.getSelectedSession();
        if (session == null) {
            return;
        }
        SessionEditDialog dialog = new SessionEditDialog(this, sessionService, session, this::refreshData);
        dialog.setVisible(true);
    }

    private void deleteSelectedSession() {
        var session = historyPanel.getSelectedSession();
        if (session == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Supprimer la session #%d (%s) ?", session.id(), session.zoneName()),
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sessionService.deleteSession(session.id());
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de supprimer la session: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHistory() {
        refreshData();
        showCard(CARD_HISTORY);
    }

    private void showStats() {
        refreshData();
        showCard(CARD_STATS);
    }

    private void showFarm() {
        modeFarmPanel.prepareForNewSession();
        showCard(CARD_FARM);
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

    private void handleSessionSaved() {
        refreshData();
    }
}
