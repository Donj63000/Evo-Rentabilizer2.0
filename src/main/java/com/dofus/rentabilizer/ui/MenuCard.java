package com.dofus.rentabilizer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuCard extends RoundedPanel {
    private final Runnable action;

    public MenuCard(String title, String subtitle, String emblem, Runnable action) {
        super(32);
        this.action = action;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 120, 90), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        setGradient(new Color(42, 66, 78, 200), new Color(28, 44, 54, 200));
        setPreferredSize(new Dimension(280, 140));

        JLabel emblemLabel = new JLabel(emblem);
        emblemLabel.setFont(ThemePalette.titleFont().deriveFont(Font.BOLD, 16f));
        emblemLabel.setForeground(ThemePalette.GOLD);
        emblemLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ThemePalette.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("<html><div style='width:170px;'>" + subtitle + "</div></html>");
        subtitleLabel.setFont(ThemePalette.bodyFont());
        subtitleLabel.setForeground(ThemePalette.TEXT_SECONDARY);

        JLabel arrow = new JLabel("➜", SwingConstants.RIGHT);
        arrow.setForeground(ThemePalette.GOLD);
        arrow.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 18f));

        JPanel textContainer = new JPanel();
        textContainer.setOpaque(false);
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.add(titleLabel);
        textContainer.add(Box.createVerticalStrut(6));
        textContainer.add(subtitleLabel);

        add(emblemLabel, BorderLayout.WEST);
        add(textContainer, BorderLayout.CENTER);

        JPanel arrowWrapper = new JPanel(new BorderLayout());
        arrowWrapper.setOpaque(false);
        arrowWrapper.add(arrow, BorderLayout.CENTER);
        arrowWrapper.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        add(arrowWrapper, BorderLayout.EAST);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setGradient(new Color(54, 80, 92, 220), new Color(34, 50, 60, 210));
                arrow.setForeground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 215, 120, 140), 1, true),
                        BorderFactory.createEmptyBorder(14, 18, 14, 18)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setGradient(new Color(42, 66, 78, 200), new Color(28, 44, 54, 200));
                arrow.setForeground(ThemePalette.GOLD);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 215, 120, 90), 1, true),
                        BorderFactory.createEmptyBorder(14, 18, 14, 18)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
    }
}
