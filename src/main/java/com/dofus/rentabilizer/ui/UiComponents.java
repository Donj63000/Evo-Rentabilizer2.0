package com.dofus.rentabilizer.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UiComponents {
    private UiComponents() {
    }

    public static JButton primaryButton(String text) {
        return new GradientButton(text, ThemePalette.GOLD, ThemePalette.DARK_GOLD);
    }

    public static JButton ghostButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(ThemePalette.TEXT_PRIMARY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemePalette.GOLD, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setForeground(ThemePalette.GOLD);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setForeground(ThemePalette.TEXT_PRIMARY);
            }
        });
        return button;
    }

    public static JTable stylizedTable() {
        JTable table = new JTable();
        table.setOpaque(false);
        table.setFillsViewportHeight(true);
        table.setBackground(ThemePalette.NIGHT);
        table.setForeground(ThemePalette.TEXT_PRIMARY);
        table.setFont(ThemePalette.bodyFont());
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(40, 55, 64));
        table.setSelectionBackground(new Color(82, 122, 109));
        table.setSelectionForeground(ThemePalette.TEXT_PRIMARY);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(38, 65, 64));
        header.setForeground(ThemePalette.TEXT_PRIMARY);
        header.setReorderingAllowed(false);
        header.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 14f));
        return table;
    }

    private static class GradientButton extends JButton {
        private final Color start;
        private final Color end;
        private boolean hover;

        GradientButton(String text, Color start, Color end) {
            super(text);
            this.start = start;
            this.end = end;
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setForeground(Color.DARK_GRAY);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color hoverStart = hover ? start.brighter() : start;
            Color hoverEnd = hover ? end.brighter() : end;
            GradientPaint paint = new GradientPaint(0, 0, hoverStart, getWidth(), getHeight(), hoverEnd);
            g2.setPaint(paint);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
