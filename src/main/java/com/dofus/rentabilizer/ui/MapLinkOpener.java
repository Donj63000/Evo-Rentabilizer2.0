package com.dofus.rentabilizer.ui;

import javax.swing.*;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public final class MapLinkOpener {
    private static final String BASE_URL = "https://www.dofus-retro.co/";

    private MapLinkOpener() {
    }

    public static void openPosition(String position) {
        String url;
        try {
            url = buildDofusRetroUrl(position);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "Position invalide",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            System.err.println("Desktop non supporte, impossible d'ouvrir le navigateur.");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            System.err.println("Action BROWSE non supportee.");
            return;
        }

        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    static String buildDofusRetroUrl(String position) {
        String normalized = normalizePosition(position);
        if (normalized == null) {
            return BASE_URL;
        }
        // TODO: ajuster le pattern si le site propose un deep-link officiel pour les coordonnees.
        return BASE_URL + "#" + normalized;
    }

    private static String normalizePosition(String position) {
        if (position == null) {
            return null;
        }
        String compact = position.trim().replaceAll("\\s+", "");
        if (compact.isEmpty()) {
            return null;
        }
        if (!compact.matches("-?\\d+,-?\\d+")) {
            throw new IllegalArgumentException("Format de position attendu: x,y (ex: 5,-20)");
        }
        return compact;
    }
}
