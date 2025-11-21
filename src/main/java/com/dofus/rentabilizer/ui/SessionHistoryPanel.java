package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.SessionRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SessionHistoryPanel extends JPanel {
    private static final String[] COLUMNS = {"ID", "Zone", "Pos", "Debut", "Fin", "Minutes", "Kamas", "K/h"};
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = UiComponents.stylizedTable();
    private List<SessionRecord> currentSessions = new ArrayList<>();

    public SessionHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemePalette.PANEL);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setModel(model);
        table.setDefaultRenderer(Object.class, zebraRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ThemePalette.EMERALD));
        scrollPane.getViewport().setBackground(ThemePalette.NIGHT);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setSessions(List<SessionRecord> sessions) {
        this.currentSessions = new ArrayList<>(sessions);
        model.setRowCount(0);
        for (SessionRecord session : sessions) {
            model.addRow(new Object[]{
                    session.id(),
                    session.zoneName(),
                    session.position() == null ? "-" : session.position(),
                    formatDate(session.startedAtIso()),
                    formatDate(session.endedAtIso()),
                    session.durationMinutes(),
                    session.kamasTotal(),
                    String.format("%.0f", session.kamasPerHour())
            });
        }
        table.clearSelection();
    }

    public SessionRecord getSelectedSession() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentSessions.size()) {
            return null;
        }
        return currentSessions.get(modelRow);
    }

    public void addSelectionListener(Runnable listener) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                listener.run();
            }
        });
    }

    public void addSessionActivateListener(Consumer<SessionRecord> listener) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    SessionRecord session = getSelectedSession();
                    if (session != null) {
                        listener.accept(session);
                    }
                }
            }
        });
    }

    private DefaultTableCellRenderer zebraRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(20, 30, 38) : new Color(24, 36, 44));
                    c.setForeground(ThemePalette.TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(4, 6, 4, 6));
                return c;
            }
        };
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return "-";
        }
        try {
            return LocalDateTime.parse(iso).format(DISPLAY_DATE);
        } catch (DateTimeParseException e) {
            return iso;
        }
    }
}
