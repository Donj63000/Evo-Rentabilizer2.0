package com.dofus.rentabilizer.ui;

import javax.swing.*;
import java.awt.*;

public class HelpDialog extends JDialog {
    public HelpDialog(Window owner) {
        super(owner, "Aide \u00e0 l'utilisation", ModalityType.APPLICATION_MODAL);
        setContentPane(buildContent());
        setPreferredSize(new Dimension(920, 680));
        setMinimumSize(new Dimension(820, 560));
        pack();
        setLocationRelativeTo(owner);
    }

    private JComponent buildContent() {
        RoundedPanel container = new RoundedPanel(24);
        container.setGradient(
                new Color(ThemePalette.NIGHT.getRed(), ThemePalette.NIGHT.getGreen(), ThemePalette.NIGHT.getBlue(), 240),
                new Color(ThemePalette.OBSIDIAN.getRed(), ThemePalette.OBSIDIAN.getGreen(), ThemePalette.OBSIDIAN.getBlue(), 225)
        );
        container.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        container.setLayout(new BorderLayout(0, 14));

        JLabel title = new JLabel("Guide d'utilisation complet");
        title.setForeground(ThemePalette.TEXT_PRIMARY);
        title.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Tout ce qu'il faut pour ajouter, consulter et optimiser vos sessions de farm.");
        subtitle.setForeground(ThemePalette.TEXT_SECONDARY);
        subtitle.setFont(ThemePalette.bodyFont());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        JTextPane body = new JTextPane();
        body.setContentType("text/html");
        body.setEditable(false);
        body.setOpaque(false);
        body.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        body.setFont(ThemePalette.bodyFont());
        body.setForeground(ThemePalette.TEXT_PRIMARY);
        body.setText(buildHtml());

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(64, 88, 98, 160)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        JLabel tip = new JLabel("Astuce: le classement et le graphique se basent uniquement sur vos données locales.");
        tip.setForeground(ThemePalette.TEXT_SECONDARY);
        tip.setFont(ThemePalette.bodyFont().deriveFont(Font.ITALIC, 12f));

        JButton close = UiComponents.primaryButton("Fermer");
        close.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(tip, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(close);
        footer.add(actions, BorderLayout.EAST);

        container.add(header, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private String buildHtml() {
        String color = "#f8f4e8";
        String secondary = "#cec8b6";
        return """
                <html><body style='color:%s;font-family:Sans-Serif;font-size:13px;'>
                <div style='margin-bottom:12px;'>
                    <b>1. Lancer l'application</b><br/>
                    - Via l'interface: lancez le JAR ou la commande Gradle/Maven depuis votre machine.<br/>
                    - Via le CLI: ajoutez directement des sessions avec les sous-commandes Picocli fournies.
                </div>
                <div style='margin-bottom:12px;'>
                    <b>2. Ajouter une session</b><br/>
                    - Ouvrez <u>Mode farm</u> depuis le menu.<br/>
                    - Renseignez la zone, la durée et les kamas gagnés, puis validez.<br/>
                    - Conseils: gardez des noms de zone cohérents (ex: "Porcos", "Cania Nord") pour de meilleures stats.
                </div>
                <div style='margin-bottom:12px;'>
                    <b>3. Consulter vos statistiques</b><br/>
                    - Ouvrez <u>Infos zones</u> pour voir le tableau triable et le classement K/h.<br/>
                    - Cliquez sur <u>Graphique</u> pour comparer visuellement vos zones et détecter les meilleures.
                </div>
                <div style='margin-bottom:12px;'>
                    <b>4. Historique et corrections</b><br/>
                    - Dans <u>Historique des sessions</u>, sélectionnez une ligne pour la modifier ou la supprimer.<br/>
                    - Utilisez les boutons <i>Modifier</i> ou <i>Supprimer</i> pour corriger rapidement vos données.
                </div>
                <div style='margin-bottom:12px;'>
                    <b>5. Utilisation en ligne de commande</b><br/>
                    - Ajouter: <code>./gradlew run --args="add --zone 'Porcos' --minutes 45 --kamas 120000"</code><br/>
                    - Statistiques: <code>./gradlew run --args="stats"</code><br/>
                    - Historique: <code>./gradlew run --args="history -n 10"</code>
                </div>
                <div style='margin-bottom:12px;'>
                    <b>6. Données et sauvegardes</b><br/>
                    - Toutes les données restent locales dans <code>~/.dofus-rentabilizer/data.db</code>.<br/>
                    - Conservez ce fichier pour vos sauvegardes ou transferts de machine.
                </div>
                <div style='margin-bottom:12px; color:%s;'>
                    <b>Bon à savoir</b><br/>
                    - Rafraîchissez les vues après un ajout pour mettre à jour le tableau et le graphique.<br/>
                    - Utilisez la molette sur les écrans longs (Mode farm) pour accéder à tous les champs.<br/>
                    - Les couleurs et polices sont optimisées pour Dofus 1.29: gardez le plein écran pour un rendu net.
                </div>
                </body></html>
                """.formatted(color, secondary);
    }
}
