package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.service.SessionService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class SessionFormDialog extends JDialog {
    private final SessionService sessionService;
    private final Runnable onSuccess;

    private final JTextField zoneField = new JTextField();
    private final JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 600, 1));
    private final JSpinner kamasSpinner = new JSpinner(new SpinnerNumberModel(10000, 0, 50_000_000, 100));

    public SessionFormDialog(Frame owner, SessionService sessionService, Runnable onSuccess) {
        super(owner, "Nouvelle session", true);
        this.sessionService = sessionService;
        this.onSuccess = onSuccess;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemePalette.OBSIDIAN);
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        content.setBackground(ThemePalette.PANEL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel("Nouvelle session");
        heading.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 20f));
        heading.setForeground(ThemePalette.TEXT_PRIMARY);
        gbc.gridwidth = 2;
        content.add(heading, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        addLabel(content, gbc, "Zone");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        zoneField.setColumns(20);
        zoneField.setBackground(ThemePalette.NIGHT);
        zoneField.setForeground(ThemePalette.TEXT_PRIMARY);
        zoneField.setCaretColor(ThemePalette.TEXT_PRIMARY);
        zoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemePalette.EMERALD.darker()),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        content.add(zoneField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        addLabel(content, gbc, "Minutes");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        stylizeSpinner(minutesSpinner);
        content.add(minutesSpinner, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        addLabel(content, gbc, "Kamas");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        stylizeSpinner(kamasSpinner);
        content.add(kamasSpinner, gbc);

        add(content, BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(ThemePalette.NIGHT);

        JButton cancel = UiComponents.ghostButton("Annuler");
        cancel.addActionListener(e -> dispose());

        JButton save = UiComponents.primaryButton("Enregistrer");
        save.addActionListener(e -> onSave());

        panel.add(cancel);
        panel.add(save);
        return panel;
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemePalette.TEXT_PRIMARY);
        panel.add(label, gbc);
    }

    private void stylizeSpinner(JSpinner spinner) {
        spinner.setBorder(BorderFactory.createLineBorder(ThemePalette.EMERALD.darker()));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            JTextField textField = editor.getTextField();
            textField.setBackground(ThemePalette.NIGHT);
            textField.setForeground(ThemePalette.TEXT_PRIMARY);
            textField.setCaretColor(ThemePalette.TEXT_PRIMARY);
        }
    }

    private void onSave() {
        try {
            String zone = zoneField.getText().trim();
            int minutes = ((Number) minutesSpinner.getValue()).intValue();
            long kamas = ((Number) kamasSpinner.getValue()).longValue();

            if (zone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La zone est obligatoire", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            sessionService.addSession(zone, minutes, kamas, LocalDateTime.now());
            if (onSuccess != null) {
                onSuccess.run();
            }
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible d'enregistrer la session: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
