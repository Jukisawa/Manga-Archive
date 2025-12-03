package com.jukisawa.mangaarchive.ui.controller;

import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jukisawa.mangaarchive.dto.VolumeDTO;
import com.jukisawa.mangaarchive.service.VolumeService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class VolumeEditController {
    private static final Logger LOGGER = Logger.getLogger(VolumeEditController.class.getName());

    @FXML
    private TextField volumeField;
    @FXML
    private TextField arcField;
    @FXML
    private TextField noteField;

    private VolumeDTO volume;
    private boolean saved;

    private VolumeService volumeService;

    @FXML
    public void initialize() {
        // Filter auf volumeField setzten damit nur Zahlen eingegeben werden können
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty())
                return change;
            try {
                Integer.parseInt(newText);
                return change;
            } catch (NumberFormatException _) {
            }
            return null;
        };

        TextFormatter<Integer> formatter = new TextFormatter<>(new IntegerStringConverter(), null, filter);
        volumeField.setTextFormatter(formatter);
    }

    public void setService(VolumeService volumeService) {
        this.volumeService = volumeService;
    }

    public boolean isSaved() {
        return saved;
    }

    public VolumeDTO getVolumeDTO() {
        return volume;
    }

    public void setVolume(VolumeDTO volume) {
        this.volume = volume;
        this.saved = false;

        if (volume.getId() != 0) {
            volumeField.setText(String.valueOf(volume.getVolume()));
            arcField.setText(volume.getArc());
            noteField.setText(volume.getNote());
        }
    }

    @FXML
    private void onSave() {
        try {
            if (volume == null) {
                volume = new VolumeDTO();
            }

            volume.setArc(arcField.getText());
            volume.setVolume(Integer.parseInt(volumeField.getText()));
            volume.setNote(noteField.getText());

            volumeService.saveVolume(volume);
            saved = true;
            closeWindow();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fehler beim speichern von Volume", e);
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) volumeField.getScene().getWindow();
        stage.close();
    }

}
