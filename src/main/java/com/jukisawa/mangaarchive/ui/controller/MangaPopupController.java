package com.jukisawa.mangaarchive.ui.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.dto.MangaDTO;
import com.jukisawa.mangaarchive.service.MangaService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class MangaPopupController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private CheckBox completedCb;
    @FXML
    private CheckBox abortedCb;
    @FXML
    private TilePane genreContainer;
    @FXML
    private TextField ratingField;

    private MangaDTO manga;
    private boolean saved;
    private List<GenreDTO> allGenres = new ArrayList<>();
    private final Map<GenreDTO, CheckBox> genreCheckboxes = new HashMap<>();

    private MangaService mangaService;

    @FXML
    public void initialize() {
        // Filter auf ratingField setzten damit nur Zahlen von 1 bis 10 eingegeben werden können
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty())
                return change;
            try {
                int value = Integer.parseInt(newText);
                if (value >= 1 && value <= 10) {
                    return change;
                }
            } catch (NumberFormatException _) {
            }
            return null;
        };

        TextFormatter<Integer> formatter = new TextFormatter<>(new IntegerStringConverter(), null, filter);
        ratingField.setTextFormatter(formatter);
    }

    public void setService(MangaService mangaService) {
        this.mangaService = mangaService;
    }

    public void setGenres(List<GenreDTO> genres) {
        this.allGenres = genres;
        initGenreCheckboxes();
    }

    public boolean isSaved() {
        return saved;
    }

    public MangaDTO getMangaDTO() {
        return manga;
    }

    public void setManga(MangaDTO manga) {
        this.manga = manga;
        this.saved = false;

        if (manga != null) {
            nameField.setText(manga.getName());
            locationField.setText(manga.getLocation());
            completedCb.setSelected(manga.isCompleted());
            abortedCb.setSelected(manga.isAborted());
            ratingField.setText(String.valueOf(manga.getRating()));
        }

        if (manga != null && manga.getGenres() != null) {
            for (GenreDTO genre : manga.getGenres()) {
                CheckBox cb = genreCheckboxes.get(genre);
                if (cb != null)
                    cb.setSelected(true);
            }
        }
    }

    private void initGenreCheckboxes() {
        genreContainer.getChildren().clear();
        for (GenreDTO genre : allGenres) {
            CheckBox cb = new CheckBox(genre.getName());
            cb.setStyle("-fx-text-fill: white;");
            genreCheckboxes.put(genre, cb);
            genreContainer.getChildren().add(cb);
        }
    }

    @FXML
    private void onSave() {
        if (manga == null) {
            manga = new MangaDTO();
        }

        manga.setName(nameField.getText());
        manga.setLocation(locationField.getText());
        manga.setCompleted(completedCb.isSelected());
        manga.setAborted(abortedCb.isSelected());
        manga.setRating(Integer.parseInt(ratingField.getText()));

        List<GenreDTO> selectedGenres = genreCheckboxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();

        manga.setGenres(selectedGenres);
        mangaService.saveManga(manga);
        saved = true;
        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

}
