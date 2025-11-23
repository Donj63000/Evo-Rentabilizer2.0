package com.dofus.rentabilizer.ui;

import com.dofus.rentabilizer.service.SessionService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ModeFarmPanel extends JPanel {
    private static final int KNOWN_ZONES_LIMIT = 12;
    private final SessionService sessionService;
    private final Runnable onBack;
    private final Runnable onSessionSaved;

    private final JTextField zoneField = new JTextField();
    private final JTextField kamasField = new JTextField();
    private final JTextArea noteArea = new JTextArea(3, 32);
    private final JSpinner hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 48, 1));
    private final JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 59, 1));
    private final JSpinner secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private final JLabel timerLabel = new JLabel("00:00:00");
    private final JLabel statusLabel = new JLabel("Appuyez sur « Demarrer farm » pour commencer.");
    private final JLabel ratioLabel = new JLabel("--- K/h");
    private final JButton startButton = UiComponents.primaryButton("Demarrer farm");
    private final JButton stopButton = UiComponents.ghostButton("Arret du farm");
    private final JButton saveButton = UiComponents.primaryButton("Enregistrer la session");
    private final JButton zonePickerButton = buildZonePickerButton();
    private final JPopupMenu zoneHistoryMenu = new JPopupMenu();

    private final Timer timer;
    private Timer countdownTimer;
    private boolean running;
    private LocalDateTime startDateTime;
    private long elapsedSeconds;
    private boolean durationEditedBeforeStart;
    private boolean updatingDurationInputs;
    private List<String> knownZones = new ArrayList<>();

    public ModeFarmPanel(SessionService sessionService, Runnable onBack, Runnable onSessionSaved) {
        this.sessionService = sessionService;
        this.onBack = onBack;
        this.onSessionSaved = onSessionSaved;
        setOpaque(true);
        setLayout(new BorderLayout(0, 16));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollableForm(), BorderLayout.CENTER);

        timer = new Timer(1000, e -> {
            if (running && startDateTime != null) {
                elapsedSeconds = Duration.between(startDateTime, LocalDateTime.now()).getSeconds();
                updateTimerLabel();
            }
        });
        hookListeners();
        prepareForNewSession();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Image bg = ThemePalette.farmBackgroundTexture();
        if (bg != null) {
            int w = getWidth();
            int h = getHeight();
            int imgW = bg.getWidth(null);
            int imgH = bg.getHeight(null);
            if (w > 0 && h > 0 && imgW > 0 && imgH > 0) {
                double scale = Math.max((double) w / imgW, (double) h / imgH);
                int drawW = (int) (imgW * scale);
                int drawH = (int) (imgH * scale);
                int x = (w - drawW) / 2;
                int y = (h - drawH) / 2;
                g2.drawImage(bg, x, y, drawW, drawH, null);
            }
        }
        g2.setPaint(new GradientPaint(0, 0, new Color(8, 14, 20, 90),
                0, getHeight(), new Color(12, 20, 28, 130)));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JComponent buildHeader() {
        RoundedPanel top = new RoundedPanel(26);
        top.setGradient(new Color(32, 52, 60, 150), new Color(24, 36, 42, 170));
        top.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        top.setLayout(new BorderLayout(12, 0));

        JButton back = UiComponents.ghostButton("< Precedent");
        back.addActionListener(e -> {
            stopTimerInternal();
            prepareForNewSession();
            onBack.run();
        });
        top.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("Mode farm");
        title.setForeground(ThemePalette.TEXT_PRIMARY);
        title.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 22f));
        top.add(title, BorderLayout.CENTER);

        return top;
    }

    private JComponent buildScrollableForm() {
        JComponent form = buildForm();
        JScrollPane scrollPane = new JScrollPane(form, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JComponent buildForm() {
        RoundedPanel form = new RoundedPanel(34);
        form.setGradient(new Color(16, 24, 30, 150), new Color(12, 18, 26, 150));
        form.setBorder(BorderFactory.createEmptyBorder(22, 32, 22, 32));
        form.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 14, 12, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 32));
        timerLabel.setForeground(ThemePalette.GOLD);
        ratioLabel.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 28f));
        ratioLabel.setForeground(ThemePalette.TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        form.add(buildHighlightsRow(), gbc);
        gbc.gridwidth = 1;
        row++;

        // Zone field
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(label("Quelle zone est farmee ?"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        zoneField.setColumns(30);
        styleTextField(zoneField);
        RoundedPanel zoneFieldWrapper = createInputWrapper();
        zoneFieldWrapper.add(zoneField, BorderLayout.CENTER);
        zoneFieldWrapper.add(zonePickerButton, BorderLayout.EAST);
        form.add(zoneFieldWrapper, gbc);
        gbc.gridwidth = 1;
        row++;

        // Session controls
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(label("Controle de session"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        form.add(buildSessionControls(), gbc);
        gbc.gridwidth = 1;
        row++;

        // Duration inputs
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(label("Combien de temps on farm ?"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        RoundedPanel durationPanel = new RoundedPanel(24);
        durationPanel.setGradient(new Color(26, 38, 46, 140), new Color(20, 30, 38, 160));
        durationPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        durationPanel.setOpaque(false);
        durationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stylizeSpinner(hoursSpinner, "h");
        stylizeSpinner(minutesSpinner, "min");
        stylizeSpinner(secondsSpinner, "s");
        hoursSpinner.setEnabled(false);
        minutesSpinner.setEnabled(false);
        secondsSpinner.setEnabled(false);
        durationPanel.add(wrapper(hoursSpinner, "h"));
        durationPanel.add(wrapper(minutesSpinner, "min"));
        durationPanel.add(wrapper(secondsSpinner, "s"));
        form.add(durationPanel, gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        form.add(createHintLabel("Ajustable apres Arret du farm"), gbc);
        gbc.gridwidth = 1;
        row++;

        // Kamas field
        gbc.gridy = row;
        gbc.gridx = 0;
        form.add(label("Combien de kamas lors de la mise en vente ?"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        styleTextField(kamasField);
        RoundedPanel kamasWrapper = createInputWrapper();
        kamasWrapper.add(kamasField, BorderLayout.CENTER);
        JLabel kamasSuffix = new JLabel("K");
        kamasSuffix.setForeground(ThemePalette.TEXT_SECONDARY);
        kamasSuffix.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        kamasWrapper.add(kamasSuffix, BorderLayout.EAST);
        form.add(kamasWrapper, gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        form.add(createHintLabel("Valeur totale reelle apres mise en vente HDV"), gbc);
        gbc.gridwidth = 1;
        row++;

        // Note field
        gbc.gridy = row;
        gbc.gridx = 0;
        form.add(label("Note sur la session ?"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        form.add(buildNoteField(), gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        form.add(createHintLabel("Optionnel: drops, incidents, objectifs..."), gbc);
        gbc.gridwidth = 1;
        row++;

        // Status panel
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        form.add(buildStatusPanel(), gbc);
        gbc.gridwidth = 1;
        row++;

        // Actions
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 12, 10, 12);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        saveButton.setEnabled(false);
        saveButton.setForeground(new Color(32, 23, 10));
        actions.add(saveButton);
        form.add(actions, gbc);

        return form;
    }

    private JComponent buildHighlightsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.add(metricCard("Chronometre", timerLabel, "Temps reel en cours"));
        row.add(metricCard("Ratio estime", ratioLabel, "Projection en kamas/heure"));
        return row;
    }

    private RoundedPanel metricCard(String title, JLabel valueLabel, String helper) {
        RoundedPanel card = new RoundedPanel(28);
        card.setGradient(new Color(28, 44, 54, 150), new Color(18, 28, 34, 150));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));
        card.setLayout(new BorderLayout(0, 8));

        JLabel cardTitle = new JLabel(title.toUpperCase());
        cardTitle.setForeground(ThemePalette.TEXT_SECONDARY);
        cardTitle.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 14f));
        card.add(cardTitle, BorderLayout.NORTH);

        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel helperLabel = createHintLabel(helper);
        card.add(helperLabel, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildSessionControls() {
        RoundedPanel controlsPanel = new RoundedPanel(24);
        controlsPanel.setGradient(new Color(30, 44, 54, 140), new Color(18, 26, 34, 140));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        controlsPanel.setLayout(new BorderLayout(0, 4));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttons.setOpaque(false);
        buttons.add(startButton);
        buttons.add(stopButton);
        controlsPanel.add(buttons, BorderLayout.CENTER);

        controlsPanel.add(createHintLabel("Un compte a rebours de 10 s vous laisse vous placer avant le depart."), BorderLayout.SOUTH);
        return controlsPanel;
    }

    private JComponent buildStatusPanel() {
        RoundedPanel panel = new RoundedPanel(28);
        panel.setGradient(new Color(26, 46, 44, 130), new Color(20, 38, 34, 150));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panel.setLayout(new BorderLayout());
        statusLabel.setForeground(ThemePalette.TEXT_PRIMARY);
        statusLabel.setFont(ThemePalette.bodyFont().deriveFont(Font.PLAIN, 15f));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemePalette.TEXT_PRIMARY);
        return label;
    }

    private RoundedPanel createInputWrapper() {
        RoundedPanel wrapper = new RoundedPanel(24);
        wrapper.setGradient(new Color(26, 38, 46, 130), new Color(16, 24, 30, 150));
        wrapper.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        wrapper.setLayout(new BorderLayout(8, 0));
        wrapper.setOpaque(false);
        return wrapper;
    }

    private JLabel createHintLabel(String text) {
        JLabel hint = new JLabel(text);
        hint.setForeground(ThemePalette.TEXT_SECONDARY);
        hint.setFont(ThemePalette.bodyFont().deriveFont(Font.ITALIC, 12f));
        return hint;
    }

    private JComponent buildNoteField() {
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBackground(ThemePalette.NIGHT);
        noteArea.setForeground(ThemePalette.TEXT_PRIMARY);
        noteArea.setCaretColor(ThemePalette.TEXT_PRIMARY);
        noteArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        RoundedPanel wrapper = createInputWrapper();
        wrapper.add(scrollPane, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(200, 110));
        return wrapper;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(ThemePalette.NIGHT);
        field.setForeground(ThemePalette.TEXT_PRIMARY);
        field.setCaretColor(ThemePalette.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 100, 110)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void stylizeSpinner(JSpinner spinner, String name) {
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            JTextField textField = editor.getTextField();
            textField.setColumns(2);
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBackground(ThemePalette.NIGHT);
            textField.setForeground(ThemePalette.TEXT_PRIMARY);
            textField.setCaretColor(ThemePalette.TEXT_PRIMARY);
            textField.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        }
        spinner.setBorder(BorderFactory.createLineBorder(new Color(70, 100, 110)));
        spinner.setOpaque(false);
        spinner.setFocusable(false);
        spinner.getAccessibleContext().setAccessibleName(name);
        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                JButton button = arrowButton("▲");
                installNextButtonListeners(button);
                return button;
            }

            @Override
            protected Component createPreviousButton() {
                JButton button = arrowButton("▼");
                installPreviousButtonListeners(button);
                return button;
            }

            private JButton arrowButton(String label) {
                JButton button = new JButton(label);
                button.setFont(ThemePalette.bodyFont().deriveFont(Font.BOLD, 10f));
                button.setForeground(ThemePalette.TEXT_PRIMARY);
                button.setBackground(new Color(50, 70, 80));
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createLineBorder(new Color(70, 100, 110)));
                button.setPreferredSize(new Dimension(32, 16));
                return button;
            }
        });
    }

    private JPanel wrapper(JSpinner spinner, String suffix) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);
        panel.add(spinner, BorderLayout.CENTER);
        JLabel label = new JLabel(suffix);
        label.setForeground(ThemePalette.TEXT_SECONDARY);
        panel.add(label, BorderLayout.EAST);
        return panel;
    }

    private void hookListeners() {
        startButton.addActionListener(e -> startFarmWithCountdown());
        stopButton.addActionListener(e -> stopFarm());
        saveButton.addActionListener(e -> saveSession());
        stopButton.setEnabled(false);

        hoursSpinner.addChangeListener(e -> onDurationInputChanged());
        minutesSpinner.addChangeListener(e -> onDurationInputChanged());
        secondsSpinner.addChangeListener(e -> onDurationInputChanged());
        kamasField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateRatioPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateRatioPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateRatioPreview();
            }
        });
    }

    public void prepareForNewSession() {
        stopTimerInternal();
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        running = false;
        startDateTime = null;
        elapsedSeconds = 0;
        zoneField.setText("");
        kamasField.setText("");
        kamasField.setEnabled(true);
        noteArea.setText("");
        noteArea.setEnabled(true);
        timerLabel.setText("00:00:00");
        updatingDurationInputs = true;
        hoursSpinner.setValue(0);
        minutesSpinner.setValue(1);
        secondsSpinner.setValue(0);
        updatingDurationInputs = false;
        setDurationInputsEnabled(true);
        durationEditedBeforeStart = false;
        saveButton.setEnabled(false);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusLabel.setText("Appuyez sur « Demarrer farm » pour commencer.");
        ratioLabel.setText("--- K/h");
        refreshZoneHistory();
    }

    private void startFarmWithCountdown() {
        if (running) {
            return;
        }
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        setDurationInputsEnabled(false);
        kamasField.setEnabled(false);
        noteArea.setEnabled(false);
        saveButton.setEnabled(false);

        final int[] remaining = {10};
        statusLabel.setText(String.format("Commencez a farmer dans %d secondes...", remaining[0]));
        countdownTimer = new Timer(1000, e -> {
            remaining[0]--;
            if (remaining[0] > 0) {
                statusLabel.setText(String.format("Commencez a farmer dans %d secondes...", remaining[0]));
            } else {
                countdownTimer.stop();
                countdownTimer = null;
                statusLabel.setText("Session en cours... Arretez le farm quand vous avez termine.");
                startFarm();
            }
        });
        countdownTimer.start();
    }

    private void startFarm() {
        startDateTime = LocalDateTime.now();
        elapsedSeconds = 0;
        running = true;
        timer.start();
        setDurationInputsEnabled(false);
        kamasField.setEnabled(false);
        noteArea.setEnabled(false);
        saveButton.setEnabled(false);
        stopButton.setEnabled(true);

        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    private void stopFarm() {
        if (countdownTimer != null && startDateTime == null) {
            countdownTimer.stop();
            countdownTimer = null;
            statusLabel.setText("Compte a rebours interrompu.");
            setDurationInputsEnabled(true);
            kamasField.setEnabled(true);
            noteArea.setEnabled(true);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            return;
        }
        if (!running || startDateTime == null) {
            return;
        }
        stopTimerInternal();
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        LocalDateTime now = LocalDateTime.now();
        elapsedSeconds = Duration.between(startDateTime, now).getSeconds();
        running = false;
        if (!durationEditedBeforeStart) {
            setDurationFromSeconds(elapsedSeconds);
        }
        durationEditedBeforeStart = false;
        setDurationInputsEnabled(true);
        kamasField.setEnabled(true);
        noteArea.setEnabled(true);
        saveButton.setEnabled(kamasField.getText().trim().length() > 0);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        updateTimerLabel();
        updateRatioPreview();
        statusLabel.setText("Session terminee. Renseignez les kamas puis enregistrez.");
    }

    private void stopTimerInternal() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    private void saveSession() {
        String zone = zoneField.getText().trim();
        if (zone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La zone est obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (startDateTime == null) {
            JOptionPane.showMessageDialog(this, "Demarrez puis arreter le farm avant d'enregistrer.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long totalSeconds = getTotalSecondsFromInputs();
        if (totalSeconds <= 0) {
            JOptionPane.showMessageDialog(this, "La duree doit etre superieure a 0 seconde.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int minutes = (int) Math.max(1, Math.round(totalSeconds / 60.0));
        long kamas;
        try {
            String raw = kamasField.getText().trim().replaceAll("\\s", "");
            kamas = Long.parseLong(raw);
            if (kamas < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Entrez un montant de kamas valide.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDateTime endTime = startDateTime.plusMinutes(minutes);
        try {
            String note = noteArea.getText();
            sessionService.addSession(zone, minutes, kamas, null, endTime, note);
            statusLabel.setText(String.format("Session enregistree (%s | %d min | %d K).", zone, minutes, kamas));
            JOptionPane.showMessageDialog(this, "Session enregistree avec succes !", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
            onSessionSaved.run();
            prepareForNewSession();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTimerLabel() {
        long seconds = elapsedSeconds;
        long hrs = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        long secs = seconds % 60;
        timerLabel.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
    }

    private void updateRatioPreview() {
        long totalSeconds = getTotalSecondsFromInputs();
        String raw = kamasField.getText().trim().replaceAll("\\s", "");
        boolean canSave = !running && startDateTime != null;
        if (totalSeconds > 0 && !raw.isEmpty()) {
            try {
                long kamas = Long.parseLong(raw);
                double ratio = (kamas * 3600.0) / totalSeconds;
                ratioLabel.setText(String.format("%.0f K/h", ratio));
                saveButton.setEnabled(canSave);
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        ratioLabel.setText("--- K/h");
        if (!running) {
            saveButton.setEnabled(false);
        }
    }

    private void setDurationInputsEnabled(boolean enabled) {
        hoursSpinner.setEnabled(enabled);
        minutesSpinner.setEnabled(enabled);
        secondsSpinner.setEnabled(enabled);
    }

    private void setDurationFromSeconds(long seconds) {
        long hrs = Math.min(48, seconds / 3600);
        long remainder = seconds - hrs * 3600;
        long mins = remainder / 60;
        long secs = remainder % 60;
        updatingDurationInputs = true;
        hoursSpinner.setValue((int) hrs);
        minutesSpinner.setValue((int) mins);
        secondsSpinner.setValue((int) secs);
        updatingDurationInputs = false;
    }

    private long getTotalSecondsFromInputs() {
        int hrs = ((Number) hoursSpinner.getValue()).intValue();
        int mins = ((Number) minutesSpinner.getValue()).intValue();
        int secs = ((Number) secondsSpinner.getValue()).intValue();
        return hrs * 3600L + mins * 60L + secs;
    }

    private void onDurationInputChanged() {
        if (!updatingDurationInputs && !running && startDateTime == null) {
            durationEditedBeforeStart = true;
        }
        updateRatioPreview();
    }

    private JButton buildZonePickerButton() {
        JButton button = new JButton("▾");
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setBackground(new Color(34, 48, 58));
        button.setForeground(ThemePalette.TEXT_PRIMARY);
        button.setFont(ThemePalette.subtitleFont().deriveFont(Font.BOLD, 16f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 100, 110)),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Choisir une zone deja enregistree");
        button.addActionListener(e -> toggleZoneHistoryMenu());
        button.setEnabled(false);
        return button;
    }

    private void toggleZoneHistoryMenu() {
        if (!zonePickerButton.isEnabled()) {
            return;
        }
        if (zoneHistoryMenu.isVisible()) {
            zoneHistoryMenu.setVisible(false);
        } else {
            zoneHistoryMenu.show(zonePickerButton, 0, zonePickerButton.getHeight());
        }
    }

    private void refreshZoneHistory() {
        try {
            knownZones = sessionService.knownZones(KNOWN_ZONES_LIMIT);
        } catch (Exception e) {
            knownZones = new ArrayList<>();
            zonePickerButton.setEnabled(false);
            zonePickerButton.setToolTipText("Zones indisponibles: " + e.getMessage());
            zoneHistoryMenu.removeAll();
            JMenuItem error = new JMenuItem("Chargement impossible");
            error.setOpaque(true);
            error.setEnabled(false);
            error.setForeground(ThemePalette.TEXT_SECONDARY);
            error.setBackground(new Color(24, 32, 40));
            zoneHistoryMenu.add(error);
            return;
        }
        boolean hasZones = !knownZones.isEmpty();
        zonePickerButton.setEnabled(hasZones);
        zonePickerButton.setToolTipText(hasZones
                ? "Choisir une zone deja enregistree"
                : "Ajoutez une session pour remplir ce menu");
        rebuildZoneHistoryMenu();
    }

    private void rebuildZoneHistoryMenu() {
        zoneHistoryMenu.removeAll();
        zoneHistoryMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 90, 100)),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        zoneHistoryMenu.setBackground(new Color(18, 26, 34));
        if (knownZones.isEmpty()) {
            JMenuItem empty = new JMenuItem("Aucune zone enregistree");
            empty.setOpaque(true);
            empty.setEnabled(false);
            empty.setForeground(ThemePalette.TEXT_SECONDARY);
            empty.setBackground(new Color(24, 32, 40));
            zoneHistoryMenu.add(empty);
            return;
        }
        for (String zone : knownZones) {
            JMenuItem item = new JMenuItem(zone);
            item.setOpaque(true);
            item.setFont(ThemePalette.bodyFont());
            item.setBackground(new Color(22, 34, 44));
            item.setForeground(ThemePalette.TEXT_PRIMARY);
            item.addActionListener(e -> {
                zoneField.setText(zone);
                zoneField.requestFocusInWindow();
                zoneField.setCaretPosition(zone.length());
                zoneHistoryMenu.setVisible(false);
            });
            zoneHistoryMenu.add(item);
        }
    }
}
