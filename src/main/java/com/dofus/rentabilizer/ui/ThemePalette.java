package com.dofus.rentabilizer.ui;

import java.awt.Color;
import java.awt.Font;

public final class ThemePalette {
    public static final Color JADE = new Color(31, 81, 68);
    public static final Color EMERALD = new Color(52, 116, 87);
    public static final Color GOLD = new Color(247, 192, 92);
    public static final Color DARK_GOLD = new Color(173, 114, 25);
    public static final Color OBSIDIAN = new Color(22, 26, 33);
    public static final Color NIGHT = new Color(15, 22, 30);
    public static final Color TEXT_PRIMARY = new Color(245, 242, 230);
    public static final Color TEXT_SECONDARY = new Color(208, 204, 191);
    public static final Color PANEL = new Color(28, 38, 48);

    private ThemePalette() {
    }

    public static Font titleFont() {
        return new Font("Serif", Font.BOLD, 30);
    }

    public static Font subtitleFont() {
        return new Font("Serif", Font.PLAIN, 16);
    }

    public static Font bodyFont() {
        return new Font("SansSerif", Font.PLAIN, 14);
    }
}
