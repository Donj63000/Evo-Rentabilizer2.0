package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.domain.SessionRecord;
import com.dofus.rentabilizer.service.SessionService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class SessionEditDialog extends JDialog {
    private final SessionService sessionService;
    private final SessionRecord session;
    private final Runnable onSuccess;

    private final JTextField zoneField = new JTextField();
    private final JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 10_000, 1));
    private final JSpinner kamasSpinner = new JSpinner(new SpinnerNumberModel(10000, 0, 50_000_000, 100));
    private final JTextArea noteArea = new JTextArea(3, 20);

    public SessionEditDialog(Frame owner, SessionService sessionService, SessionRecord session, Runnable onSuccess) {
        super(owner, "Modifier la session #" + session.id(), true);
        this.sessionService = sessionService;
        this.session = session;
        this.onSuccess = onSuccess;
        buildUi();
        preloadValues();
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

        JLabel heading = new JLabel("Mettre a jour la session");
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

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        addLabel(content, gbc, "Note sur la session ?");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane noteScroll = new JScrollPane(noteArea);
        styleNoteArea(noteArea, noteScroll);
        content.add(noteScroll, gbc);

        JLabel info = new JLabel("L'heure de fin reste inchangee. La duree recalculera le debut automatiquement.");
        info.setForeground(ThemePalette.TEXT_SECONDARY);
        info.setFont(ThemePalette.bodyFont().deriveFont(Font.ITALIC, 12f));
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(info, gbc);

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

    private void styleNoteArea(JTextArea area, JScrollPane scrollPane) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(ThemePalette.NIGHT);
        area.setForeground(ThemePalette.TEXT_PRIMARY);
        area.setCaretColor(ThemePalette.TEXT_PRIMARY);
        area.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemePalette.EMERALD.darker()));
    }

    private void preloadValues() {
        zoneField.setText(session.zoneName());
        minutesSpinner.setValue(session.durationMinutes());
        kamasSpinner.setValue(session.kamasTotal());
        if (session.note() != null) {
            noteArea.setText(session.note());
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
            LocalDateTime end = sessionService.parseIsoDateTime(session.endedAtIso());
            String note = noteArea.getText();
            sessionService.updateSession(session.id(), zone, minutes, kamas, null, end, note);
            if (onSuccess != null) {
                onSuccess.run();
            }
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de mettre a jour la session: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
