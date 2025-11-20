package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.ZoneStatsRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ZoneStatsPanel extends JPanel {
    private static final String[] COLUMNS = {"Zone", "Sessions", "Temps", "Kamas", "K/h", "Derniere session"};
    private static final String CARD_TABLE = "table";
    private static final String CARD_EMPTY = "empty";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = UiComponents.stylizedTable();
    private final CardLayout tableCardLayout = new CardLayout();
    private final JPanel tableCardContainer = new JPanel(tableCardLayout);

    private final JLabel trackedZonesValue = createValueLabel("0");
    private final JLabel trackedZonesHelper = createHelperLabel("Aucune session enregistree");
    private final JLabel totalSessionsValue = createValueLabel("0");
    private final JLabel totalSessionsHelper = createHelperLabel("Temps cumule: 0 min");
    private final JLabel totalKamasValue = createValueLabel("0 K");
    private final JLabel totalKamasHelper = createHelperLabel("Somme verifiee sur toutes les zones");
    private final JLabel bestZoneValue = createValueLabel("---");
    private final JLabel bestZoneHelper = createHelperLabel("Aucune donnee disponible");

    public ZoneStatsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 18));
        add(buildHighlightsRow(), BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);
    }

    public void setStats(List<ZoneStatsRecord> stats) {
        model.setRowCount(0);
        long totalSessions = 0;
        long totalMinutes = 0;
        long totalKamas = 0;
        LocalDateTime latestSession = null;
        ZoneStatsRecord bestZone = null;

        for (ZoneStatsRecord stat : stats) {
            LocalDateTime lastSession = parseIsoDate(stat.lastSessionIso());
            model.addRow(new Object[]{
                    stat.zoneName(),
                    stat.sessionCount(),
                    stat.totalMinutes(),
                    stat.totalKamas(),
                    stat.averageKamasPerHour(),
                    lastSession
            });
            totalSessions += stat.sessionCount();
            totalMinutes += stat.totalMinutes();
            totalKamas += stat.totalKamas();
            if (bestZone == null || stat.averageKamasPerHour() > bestZone.averageKamasPerHour()) {
                bestZone = stat;
            }
            if (lastSession != null && (latestSession == null || lastSession.isAfter(latestSession))) {
                latestSession = lastSession;
            }
        }

        if (stats.isEmpty()) {
            tableCardLayout.show(tableCardContainer, CARD_EMPTY);
        } else {
            tableCardLayout.show(tableCardContainer, CARD_TABLE);
        }
        updateSummary(totalSessions, totalMinutes, totalKamas, bestZone, stats.size(), latestSession);
    }

    private void updateSummary(long totalSessions, long totalMinutes, long totalKamas, ZoneStatsRecord bestZone,
                               int trackedZones, LocalDateTime latestSession) {
        trackedZonesValue.setText(formatNumber(trackedZones));
        trackedZonesHelper.setText(latestSession != null
                ? "Dernier farm: " + formatDate(latestSession)
                : "Aucune session enregistree");
        totalSessionsValue.setText(formatNumber(totalSessions));
        totalSessionsHelper.setText("Temps cumule: " + formatDuration(totalMinutes));
        totalKamasValue.setText(formatNumber(totalKamas) + " K");
        double overallKph = totalMinutes > 0 ? (totalKamas * 60.0) / totalMinutes : 0.0;
        totalKamasHelper.setText(overallKph > 0
                ? String.format(java.util.Locale.US, "Moyenne globale: %.0f K/h", overallKph)
                : "Somme verifiee sur toutes les zones");

        if (bestZone != null) {
            bestZoneValue.setText(bestZone.zoneName());
            bestZoneHelper.setText(String.format("%.0f K/h en moyenne (%d sessions)",
                    bestZone.averageKamasPerHour(), bestZone.sessionCount()));
        } else {
            bestZoneValue.setText("---");
            bestZoneHelper.setText("Ajoutez des sessions pour voir un classement");
        }
    }

    private JComponent buildHighlightsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setOpaque(false);
        row.add(metricCard("Zones suivies", trackedZonesValue, trackedZonesHelper));
        row.add(metricCard("Sessions enregistrees", totalSessionsValue, totalSessionsHelper));
        row.add(metricCard("Kamas comptabilises", totalKamasValue, totalKamasHelper));
        row.add(metricCard("Zone la plus rentable", bestZoneValue, bestZoneHelper));
        return row;
    }

    private RoundedPanel metricCard(String title, JLabel valueLabel, JLabel helperLabel) {
        RoundedPanel card = new RoundedPanel(30);
        card.setGradient(new Color(36, 56, 66, 230), new Color(20, 30, 38, 220));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setLayout(new BorderLayout(0, 8));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setForeground(ThemePalette.TEXT_SECONDARY);
        titleLabel.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 13f));
        card.add(titleLabel, BorderLayout.NORTH);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(helperLabel, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildTableSection() {
        RoundedPanel wrapper = new RoundedPanel(32);
        wrapper.setGradient(new Color(18, 30, 38, 230), new Color(12, 20, 28, 220));
        wrapper.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        wrapper.setLayout(new BorderLayout());

        table.setModel(model);
        table.setDefaultRenderer(Object.class, statsRenderer());
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setComparator(1, Comparator.comparingInt(o -> ((Number) o).intValue()));
        sorter.setComparator(2, Comparator.comparingLong(o -> ((Number) o).longValue()));
        sorter.setComparator(3, Comparator.comparingLong(o -> ((Number) o).longValue()));
        sorter.setComparator(4, Comparator.comparingDouble(o -> ((Number) o).doubleValue()));
        sorter.setComparator(5, Comparator.nullsLast(LocalDateTime::compareTo));
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 90, 100)));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(ThemePalette.NIGHT);

        tableCardContainer.setOpaque(false);
        tableCardContainer.add(scrollPane, CARD_TABLE);
        tableCardContainer.add(buildEmptyState(), CARD_EMPTY);
        tableCardLayout.show(tableCardContainer, CARD_EMPTY);

        wrapper.add(tableCardContainer, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildEmptyState() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel("<html><center>Aucune donnee a afficher.<br/>Ajoutez vos premieres sessions pour construire ce classement.</center></html>");
        label.setForeground(ThemePalette.TEXT_SECONDARY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(ThemePalette.subtitleFont());
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemePalette.TEXT_PRIMARY);
        label.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 24f));
        return label;
    }

    private JLabel createHelperLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemePalette.TEXT_SECONDARY);
        label.setFont(ThemePalette.bodyFont().deriveFont(Font.ITALIC, 12f));
        return label;
    }

    private String formatDuration(long totalMinutes) {
        if (totalMinutes <= 0) {
            return "0 min";
        }
        long days = totalMinutes / (60 * 24);
        long hours = (totalMinutes % (60 * 24)) / 60;
        long minutes = totalMinutes % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" j");
        }
        if (hours > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(hours).append(" h");
        }
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(minutes).append(" min");
        }
        return sb.length() == 0 ? "0 min" : sb.toString();
    }

    private String formatNumber(long value) {
        return String.format(java.util.Locale.US, "%,d", value).replace(',', ' ');
    }

    private LocalDateTime parseIsoDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(iso);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_FORMAT);
    }

    private String formatCellValue(int modelColumn, Object value) {
        if (value == null) {
            return "-";
        }
        return switch (modelColumn) {
            case 1 -> formatNumber(((Number) value).longValue());
            case 2 -> formatDuration(((Number) value).longValue());
            case 3 -> formatNumber(((Number) value).longValue()) + " K";
            case 4 -> String.format(java.util.Locale.US, "%.0f K/h", ((Number) value).doubleValue());
            case 5 -> formatDate((LocalDateTime) value);
            default -> value.toString();
        };
    }

    private DefaultTableCellRenderer statsRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                int modelColumn = table.convertColumnIndexToModel(column);
                Object modelValue = table.getModel().getValueAt(modelRow, modelColumn);
                setText(formatCellValue(modelColumn, modelValue));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? new Color(18, 28, 36) : new Color(24, 34, 42));
                    setForeground(ThemePalette.TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(4, 6, 4, 6));
                return component;
            }
        };
    }
}
