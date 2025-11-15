package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.ZoneStatsRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ZoneStatsPanel extends JPanel {
    private static final String[] COLUMNS = {"Zone", "Sessions", "Minutes", "Kamas", "K/h"};
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ZoneStatsPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemePalette.PANEL);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(ThemePalette.NIGHT);
        table.setForeground(ThemePalette.TEXT_PRIMARY);
        table.setRowHeight(24);
        table.setGridColor(new Color(50, 72, 84));
        table.setSelectionBackground(new Color(90, 132, 110));
        table.getTableHeader().setBackground(ThemePalette.EMERALD);
        table.getTableHeader().setForeground(ThemePalette.TEXT_PRIMARY);
        table.setFont(ThemePalette.bodyFont());
        table.getTableHeader().setFont(ThemePalette.bodyFont().deriveFont(Font.BOLD));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ThemePalette.EMERALD));
        scrollPane.getViewport().setBackground(ThemePalette.NIGHT);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setStats(List<ZoneStatsRecord> stats) {
        model.setRowCount(0);
        for (ZoneStatsRecord stat : stats) {
            model.addRow(new Object[]{
                    stat.zoneName(),
                    stat.sessionCount(),
                    stat.totalMinutes(),
                    stat.totalKamas(),
                    String.format("%.0f", stat.averageKamasPerHour())
            });
        }
    }
}
