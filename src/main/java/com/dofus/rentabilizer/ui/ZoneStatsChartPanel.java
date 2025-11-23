package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.ZoneStatsRecord;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

class ZoneStatsChartPanel extends JPanel {
    private static final int LEFT_MARGIN = 190;
    private static final int RIGHT_MARGIN = 140;
    private static final int TOP_MARGIN = 56;
    private static final int BOTTOM_MARGIN = 46;
    private static final int BAR_HEIGHT = 26;
    private static final int ROW_GAP = 24;

    private List<ZoneStatsRecord> stats = List.of();

    ZoneStatsChartPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    void setStats(List<ZoneStatsRecord> stats) {
        if (stats == null) {
            this.stats = List.of();
        } else {
            List<ZoneStatsRecord> sorted = new ArrayList<>(stats);
            sorted.sort(Comparator.comparingDouble(ZoneStatsRecord::averageKamasPerHour).reversed());
            this.stats = sorted;
        }
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        int rowsHeight = stats.size() * (BAR_HEIGHT + ROW_GAP);
        int height = TOP_MARGIN + BOTTOM_MARGIN + rowsHeight;
        return new Dimension(980, Math.max(360, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, withAlpha(ThemePalette.NIGHT, 230),
                0, getHeight(), withAlpha(ThemePalette.OBSIDIAN, 220)));
        g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 18, 18);

        if (stats.isEmpty()) {
            drawEmptyState(g2);
            g2.dispose();
            return;
        }

        double maxKph = stats.stream()
                .mapToDouble(ZoneStatsRecord::averageKamasPerHour)
                .max()
                .orElse(0d);
        double safeMax = maxKph > 0 ? maxKph : 1d;

        int chartWidth = Math.max(200, getWidth() - LEFT_MARGIN - RIGHT_MARGIN);
        int labelWidth = LEFT_MARGIN - 40;
        drawGrid(g2, safeMax, chartWidth);

        int y = TOP_MARGIN;
        for (int i = 0; i < stats.size(); i++) {
            ZoneStatsRecord stat = stats.get(i);
            double ratio = Math.max(0d, stat.averageKamasPerHour()) / safeMax;
            int barWidth = (int) (ratio * chartWidth);
            int barY = y + i * (BAR_HEIGHT + ROW_GAP);
            drawRow(g2, stat, i, barWidth, barY, chartWidth, labelWidth);
        }

        g2.dispose();
    }

    private void drawRow(Graphics2D g2, ZoneStatsRecord stat, int index, int barWidth, int barY, int chartWidth, int labelWidth) {
        int x = LEFT_MARGIN;

        Color trackColor = withAlpha(ThemePalette.OBSIDIAN, 140);
        g2.setColor(trackColor);
        g2.fillRoundRect(x, barY, chartWidth, BAR_HEIGHT, 18, 18);

        Color start = withAlpha(ThemePalette.EMERALD, 230);
        Color end = withAlpha(ThemePalette.JADE, 230);
        if (index == 0) {
            start = withAlpha(ThemePalette.GOLD, 240);
            end = withAlpha(ThemePalette.DARK_GOLD, 240);
        }
        GradientPaint gradient = new GradientPaint(x, barY, start, x + Math.max(40, barWidth), barY + BAR_HEIGHT, end);
        g2.setPaint(gradient);
        g2.fillRoundRect(x, barY, Math.max(18, barWidth), BAR_HEIGHT, 18, 18);

        g2.setColor(new Color(0, 0, 0, 90));
        g2.drawRoundRect(x, barY, chartWidth, BAR_HEIGHT, 18, 18);

        g2.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 14f));
        g2.setColor(ThemePalette.TEXT_PRIMARY);
        String zoneText = ellipsize(g2, stat.zoneName(), labelWidth);
        g2.drawString(zoneText, 24, barY + BAR_HEIGHT - 8);

        g2.setFont(ThemePalette.bodyFont());
        g2.setColor(ThemePalette.TEXT_SECONDARY);
        String sessionsText = ellipsize(g2, String.valueOf(stat.sessionCount()), labelWidth);
        g2.drawString(sessionsText, 24, barY + 14);

        String value = formatKph(stat.averageKamasPerHour());
        int valueWidth = g2.getFontMetrics().stringWidth(value);
        int badgePadding = 8;
        int badgeWidth = valueWidth + badgePadding * 2;
        int valueX = Math.min(x + barWidth + 16, x + chartWidth - badgeWidth);
        int badgeY = barY - 6;
        if (badgeY < 12) {
            badgeY = barY + BAR_HEIGHT - 18;
        }
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(valueX - badgePadding, badgeY - 12, badgeWidth, 24, 14, 14);
        g2.setColor(ThemePalette.TEXT_PRIMARY);
        g2.drawString(value, valueX, badgeY + 2);

        String timeLabel = formatDuration(stat.totalMinutes());
        g2.setFont(ThemePalette.bodyFont().deriveFont(Font.ITALIC, 12f));
        g2.setColor(withAlpha(ThemePalette.TEXT_SECONDARY, 210));
        int timeWidth = g2.getFontMetrics().stringWidth(timeLabel);
        g2.drawString(timeLabel, x + chartWidth - timeWidth, barY + BAR_HEIGHT + 16);

        if (index == 0) {
            g2.setColor(withAlpha(ThemePalette.GOLD, 140));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x - 6, barY - 4, chartWidth + 12, BAR_HEIGHT + 10, 22, 22);
        }
    }

    private void drawGrid(Graphics2D g2, double max, int chartWidth) {
        g2.setFont(ThemePalette.bodyFont().deriveFont(12f));
        int[] steps = {0, 33, 66, 100};
        for (int step : steps) {
            double ratio = step / 100d;
            int x = LEFT_MARGIN + (int) (ratio * chartWidth);
            g2.setColor(new Color(255, 255, 255, step == 100 ? 110 : 55));
            g2.drawLine(x, TOP_MARGIN - 8, x, getHeight() - BOTTOM_MARGIN);
            String label = formatKph(max * ratio);
            int width = g2.getFontMetrics().stringWidth(label);
            g2.setColor(ThemePalette.TEXT_SECONDARY);
            g2.drawString(label, x - width / 2, TOP_MARGIN - 14);
        }
    }

    private void drawEmptyState(Graphics2D g2) {
        g2.setFont(ThemePalette.subtitleFont());
        g2.setColor(ThemePalette.TEXT_SECONDARY);
        String text = "Ajoutez des sessions pour tracer les ratios K/h par zone.";
        int width = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (getWidth() - width) / 2, getHeight() / 2);
    }

    private Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private String formatKph(double kph) {
        return String.format(Locale.US, "%.0f K/h", kph);
    }

    private String formatDuration(long minutes) {
        if (minutes <= 0) {
            return "0 min";
        }
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours == 0) {
            return minutes + " min";
        }
        if (mins == 0) {
            return hours + " h";
        }
        return hours + " h " + mins + " min";
    }

    private String ellipsize(Graphics2D g2, String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (g2.getFontMetrics().stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = g2.getFontMetrics().stringWidth(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            int width = g2.getFontMetrics().stringWidth(sb.toString() + c) + ellipsisWidth;
            if (width > maxWidth) {
                break;
            }
            sb.append(c);
        }
        return sb.append(ellipsis).toString();
    }
}
