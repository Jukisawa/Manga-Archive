package com.jukisawa.mangaarchive.ui.controller;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.service.GenreService;
import com.jukisawa.mangaarchive.ui.components.CustomDropdown;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Optional;

public class GenreEditController {
    @FXML
    private CustomDropdown<GenreDTO> genreDropdown;
    @FXML
    private TextField nameField;

    private ObservableList<GenreDTO> genreList;
    private GenreService genreService;
    private GenreDTO genreDTO;
    private boolean saved;

    @FXML
    public void initialize() {
        saved = true;
        //setup custom Dropdown
        genreDropdown.setDisplayFunction(GenreDTO::getName);
        genreDropdown.selectedItemProperty().addListener((_, oldVal, newVal) -> handleGenreChange(oldVal, newVal));

        //setup nameField
        nameField.textProperty().addListener((_, _, newText) -> {
            // Only mark as unsaved if we already have a selected genre and text has changed
            if (genreDTO != null) {
                saved = newText.equals(genreDTO.getName());
            }
        });
    }

    public void postInit() {
        // Load all genres from service
        List<GenreDTO> genres = genreService.getAllGenres();
        // Wrap in observable list
        genreList = FXCollections.observableArrayList();
        // Create a pseudo-genre representing "New"
        GenreDTO newItem = new GenreDTO(0, "New");

        // Add "New" first
        genreList.add(newItem);
        genreList.addAll(genres);
        genreDropdown.setItems(genreList);
    }

    private void handleGenreChange(GenreDTO oldGenreDto, GenreDTO newGenreDto) {
        if (newGenreDto == null || oldGenreDto == newGenreDto)
            return;

        if (!saved) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Ungespeicherte Änderung");
            alert.setHeaderText("Es gibt ungespeicherte Änderungen!");
            alert.setContentText("Möchtest du die Änderungen löschen und ein anderes Genre auswählen?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                // Cancel the selection change — revert dropdown to previous
                Platform.runLater(() -> genreDropdown.setSelectedItem(oldGenreDto));
                return;
            }
        }

        if (newGenreDto.getId() == 0) {
            genreDTO = new GenreDTO();
        } else {
            genreDTO = newGenreDto;
        }
        nameField.setText(genreDTO.getName());
        saved = true;
    }

    public void setService(GenreService genreService) {
        this.genreService = genreService;
    }

    public void onCancel() {
        nameField.setText(genreDTO.getName());
    }

    public void onSave() {
        if (genreDTO == null)
            return;
        genreDTO.setName(nameField.getText());
        genreService.saveGenre(genreDTO);
        saved = true;
        if (!genreList.contains(genreDTO)) {
            genreList.add(genreDTO);
            // Beim neu Hinzufügen danach Namefield Leeren und neues Genre setzten
            genreDTO = new GenreDTO();
            nameField.setText("");
        }
        genreDropdown.refreshList();
        genreDropdown.refreshDisplay();
    }
}
