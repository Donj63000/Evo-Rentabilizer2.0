package com.dofus.rentabilizer.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Custom label that renders its text using a soft gradient and drop shadow so hero titles
 * can better match the premium UI theme.
 */
public class GradientTitleLabel extends JLabel {
    private Color gradientStart = ThemePalette.GOLD;
    private Color gradientEnd = ThemePalette.DARK_GOLD;
    private Color shadowColor = new Color(0, 0, 0, 120);
    private int shadowOffset = 3;

    public GradientTitleLabel(String text) {
        super(text);
        setOpaque(false);
        setFont(ThemePalette.titleFont().deriveFont(Font.BOLD, 42f));
        setForeground(ThemePalette.TEXT_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void setGradient(Color start, Color end) {
        this.gradientStart = start;
        this.gradientEnd = end;
        repaint();
    }

    public void setShadowColor(Color color) {
        this.shadowColor = color;
        repaint();
    }

    public void setShadowOffset(int offset) {
        this.shadowOffset = Math.max(0, offset);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        if (isOpaque()) {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        String text = getText();
        if (text != null && !text.isEmpty()) {
            Insets insets = getInsets();
            FontMetrics metrics = g2.getFontMetrics(getFont());
            int x = insets.left;
            int y = insets.top + metrics.getAscent();

            if (shadowColor != null && shadowOffset > 0) {
                g2.setColor(shadowColor);
                g2.drawString(text, x + shadowOffset, y + shadowOffset);
            }

            Paint previous = g2.getPaint();
            if (gradientStart != null && gradientEnd != null) {
                g2.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
            } else {
                g2.setPaint(getForeground());
            }
            g2.drawString(text, x, y);
            g2.setPaint(previous);
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        String text = getText();
        if (text == null || text.isEmpty()) {
            return super.getPreferredSize();
        }
        Insets insets = getInsets();
        FontMetrics metrics = getFontMetrics(getFont());
        int width = metrics.stringWidth(text) + insets.left + insets.right + shadowOffset;
        int height = metrics.getHeight() + insets.top + insets.bottom + shadowOffset;
        return new Dimension(width, height);
    }
}
