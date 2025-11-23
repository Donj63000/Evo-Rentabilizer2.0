package com.dofus.rentabilizer.ui;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int arc;
    private Color fill = ThemePalette.PANEL;
    private Color gradientStart;
    private Color gradientEnd;
    private boolean shadow = true;

    public RoundedPanel(int arc) {
        this.arc = arc;
        setOpaque(false);
    }

    public void setFill(Color fill) {
        this.fill = fill;
        this.gradientStart = null;
        this.gradientEnd = null;
        repaint();
    }

    public void setGradient(Color start, Color end) {
        this.gradientStart = start;
        this.gradientEnd = end;
        repaint();
    }

    public void setShadowEnabled(boolean shadow) {
        this.shadow = shadow;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int shadowOffset = shadow ? 6 : 0;
        if (shadow) {
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(shadowOffset, shadowOffset, getWidth() - shadowOffset * 2, getHeight() - shadowOffset * 2, arc, arc);
        }

        if (gradientStart != null && gradientEnd != null) {
            GradientPaint paint = new GradientPaint(0, 0, gradientStart, getWidth(), getHeight(), gradientEnd);
            g2.setPaint(paint);
        } else {
            g2.setColor(fill);
        }
        g2.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
