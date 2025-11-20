package com.dofus.rentabilizer.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public final class ThemePalette {
    public static final Color JADE = new Color(27, 80, 66);
    public static final Color EMERALD = new Color(62, 132, 104);
    public static final Color GOLD = new Color(248, 202, 87);
    public static final Color DARK_GOLD = new Color(191, 138, 49);
    public static final Color OBSIDIAN = new Color(18, 24, 33);
    public static final Color NIGHT = new Color(12, 18, 26);
    public static final Color TEXT_PRIMARY = new Color(248, 244, 232);
    public static final Color TEXT_SECONDARY = new Color(206, 200, 182);
    public static final Color PANEL = new Color(25, 34, 44, 230);
    private static Image backgroundTexture;
    private static Font titleFont;
    private static Font subtitleFont;
    private static Font bodyFont;

    private ThemePalette() {
    }

    public static Font titleFont() {
        if (titleFont == null) {
            titleFont = deriveFont("Palatino Linotype", Font.BOLD, 34f);
        }
        return titleFont;
    }

    public static Font subtitleFont() {
        if (subtitleFont == null) {
            subtitleFont = deriveFont("Georgia", Font.PLAIN, 18f);
        }
        return subtitleFont;
    }

    public static Font bodyFont() {
        if (bodyFont == null) {
            bodyFont = deriveFont("Trebuchet MS", Font.PLAIN, 14f);
        }
        return bodyFont;
    }

    public static Image backgroundTexture() {
        if (backgroundTexture == null) {
            URL url = ThemePalette.class.getResource("/img.png");
            if (url != null) {
                backgroundTexture = new ImageIcon(url).getImage();
            }
        }
        return backgroundTexture;
    }

    public static Icon logoIcon(int width, int height) {
        Image texture = backgroundTexture();
        if (texture == null) {
            return null;
        }
        Image scaled = texture.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static void applyGlobalFont() {
        Font primary = bodyFont();
        UIManager.put("Label.font", primary);
        UIManager.put("Button.font", primary);
        UIManager.put("Table.font", primary);
        UIManager.put("TableHeader.font", subtitleFont().deriveFont(Font.BOLD, 14f));
        UIManager.put("TextField.font", primary);
        UIManager.put("ComboBox.font", primary);
    }

    private static Font deriveFont(String name, int style, float size) {
        Font font = new Font(name, style, (int) size);
        if (font == null) {
            return new Font("SansSerif", style, (int) size);
        }
        return font.deriveFont(style, size);
    }
}
