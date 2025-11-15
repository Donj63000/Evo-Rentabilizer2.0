package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.SessionRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SessionHistoryPanel extends JPanel {
    private static final String[] COLUMNS = {"ID", "Zone", "Debut", "Fin", "Minutes", "Kamas", "K/h"};
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public SessionHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemePalette.PANEL);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(ThemePalette.NIGHT);
        table.setForeground(ThemePalette.TEXT_PRIMARY);
        table.setRowHeight(24);
        table.setGridColor(new Color(45, 68, 80));
        table.setSelectionBackground(new Color(74, 112, 92));
        table.getTableHeader().setBackground(ThemePalette.EMERALD);
        table.getTableHeader().setForeground(ThemePalette.TEXT_PRIMARY);
        table.setFont(ThemePalette.bodyFont());
        table.getTableHeader().setFont(ThemePalette.bodyFont().deriveFont(Font.BOLD));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ThemePalette.EMERALD));
        scrollPane.getViewport().setBackground(ThemePalette.NIGHT);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setSessions(List<SessionRecord> sessions) {
        model.setRowCount(0);
        for (SessionRecord session : sessions) {
            model.addRow(new Object[]{
                    session.id(),
                    session.zoneName(),
                    session.startedAtIso(),
                    session.endedAtIso(),
                    session.durationMinutes(),
                    session.kamasTotal(),
                    String.format("%.0f", session.kamasPerHour())
            });
        }
    }
}
