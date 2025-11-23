package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.ZoneStatsRecord;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ZoneStatsChartDialog extends JDialog {
    private final ZoneStatsChartPanel chartPanel = new ZoneStatsChartPanel();
    private final JLabel summaryLabel = new JLabel("Comparez vos zones a partir des K/h moyens.");

    public ZoneStatsChartDialog(Window owner) {
        super(owner, "Graphique des zones", ModalityType.APPLICATION_MODAL);
        setContentPane(buildContent());
        setPreferredSize(new Dimension(1080, 720));
        setMinimumSize(new Dimension(960, 620));
        pack();
        setLocationRelativeTo(owner);
    }

    private JComponent buildContent() {
        RoundedPanel container = new RoundedPanel(26);
        container.setGradient(
                new Color(ThemePalette.NIGHT.getRed(), ThemePalette.NIGHT.getGreen(), ThemePalette.NIGHT.getBlue(), 235),
                new Color(ThemePalette.OBSIDIAN.getRed(), ThemePalette.OBSIDIAN.getGreen(), ThemePalette.OBSIDIAN.getBlue(), 225));
        container.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        container.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Graphique Kamas/heure par zone");
        title.setForeground(ThemePalette.TEXT_PRIMARY);
        title.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 19f));

        summaryLabel.setForeground(ThemePalette.TEXT_SECONDARY);
        summaryLabel.setFont(ThemePalette.bodyFont());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(summaryLabel);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        legend.add(buildLegendChip("Zone la plus rentable", ThemePalette.GOLD, ThemePalette.DARK_GOLD));
        legend.add(buildLegendChip("Autres zones", ThemePalette.EMERALD, ThemePalette.JADE));
        legend.add(buildLegendChip("Ratio K/h", new Color(255, 255, 255, 120), new Color(255, 255, 255, 120)));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(header);
        top.add(Box.createVerticalStrut(6));
        top.add(legend);

        JScrollPane scrollPane = new JScrollPane(chartPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(64, 88, 98, 180)));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel tip = new JLabel("Astuce: le graphique se met a jour selon vos donnees locales uniquement.");
        tip.setForeground(ThemePalette.TEXT_SECONDARY);
        tip.setFont(ThemePalette.bodyFont().deriveFont(Font.ITALIC, 12f));
        JButton close = UiComponents.primaryButton("Fermer");
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(close);
        footer.add(tip, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);

        container.add(top, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private JComponent buildLegendChip(String text, Color start, Color end) {
        RoundedPanel chip = new RoundedPanel(14);
        chip.setShadowEnabled(false);
        chip.setGradient(withAlpha(start, 200), withAlpha(end, 200));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JLabel label = new JLabel(text);
        Color textColor = averageBrightness(start) > 170 ? ThemePalette.OBSIDIAN : ThemePalette.TEXT_PRIMARY;
        label.setForeground(textColor);
        label.setFont(ThemePalette.bodyFont().deriveFont(Font.BOLD, 12f));
        chip.add(label);
        return chip;
    }

    public void setStats(List<ZoneStatsRecord> stats) {
        chartPanel.setStats(stats);
        if (stats == null || stats.isEmpty()) {
            summaryLabel.setText(wrapSummary("Ajoutez des sessions pour comparer vos zones."));
            return;
        }
        ZoneStatsRecord best = stats.stream()
                .max(Comparator.comparingDouble(ZoneStatsRecord::averageKamasPerHour))
                .orElse(null);
        long totalSessions = stats.stream().mapToLong(ZoneStatsRecord::sessionCount).sum();
        long totalMinutes = stats.stream().mapToLong(ZoneStatsRecord::totalMinutes).sum();
        long totalKamas = stats.stream().mapToLong(ZoneStatsRecord::totalKamas).sum();
        double overallKph = totalMinutes > 0 ? (totalKamas * 60.0) / totalMinutes : 0.0;
        if (best != null) {
            summaryLabel.setText(wrapSummary(String.format(Locale.US,
                    "%d zones • %d sessions • Moyenne globale: %.0f K/h • Meilleure: %s (%.0f K/h)",
                    stats.size(), totalSessions, overallKph, best.zoneName(), best.averageKamasPerHour())));
        } else {
            summaryLabel.setText(wrapSummary(String.format(Locale.US, "%d zones • %d sessions", stats.size(), totalSessions)));
        }
    }

    private Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private int averageBrightness(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
    }

    private String wrapSummary(String text) {
        return "<html><div style='width: 780px'>" + text + "</div></html>";
    }
}
