package com.jukisawa.mangaarchive.ui.controller;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.dto.MangaDTO;
import com.jukisawa.mangaarchive.dto.MangaState;
import com.jukisawa.mangaarchive.service.MangaService;
import com.jukisawa.mangaarchive.util.ImageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MangaEditController {
    private static final Logger LOGGER = Logger.getLogger(MangaEditController.class.getName());

    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private ComboBox<MangaState> stateDropdown;
    @FXML
    private TilePane genreContainer;
    @FXML
    private TextField ratingField;
    @FXML
    private StackPane coverContainer;
    @FXML
    private ImageView coverImageView;
    @FXML
    private TextField relatedField;
    @FXML
    private TextField alternateNameField;

    private MangaDTO manga;
    private boolean saved;
    private List<GenreDTO> allGenres = new ArrayList<>();
    private final Map<GenreDTO, CheckBox> genreCheckboxes = new HashMap<>();
    private byte[] coverBytes;

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

        setupCoverArea();

        stateDropdown.getItems().setAll(MangaState.values());

        stateDropdown.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(MangaState item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        stateDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MangaState item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
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
            stateDropdown.setValue(manga.getState());
            ratingField.setText(String.valueOf(manga.getRating()));
            relatedField.setText(manga.getRelated());
            alternateNameField.setText(manga.getAlternateName());
            if (manga.getGenres() != null) {
                for (GenreDTO genre : manga.getGenres()) {
                    CheckBox cb = genreCheckboxes.get(genre);
                    if (cb != null)
                        cb.setSelected(true);
                }
            }

            if (manga.getCoverImage() != null) {
                coverImageView.setImage(ImageUtils.bytesToImage(manga.getCoverImage()));
                coverBytes = manga.getCoverImage();
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
        manga.setState(stateDropdown.getValue());
        manga.setRating(Integer.parseInt(ratingField.getText()));
        manga.setRelated(relatedField.getText());
        manga.setAlternateName(alternateNameField.getText());

        if (coverBytes != null) {
            manga.setCoverImage(coverBytes);
        }

        List<GenreDTO> selectedGenres = genreCheckboxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();

        manga.setGenres(selectedGenres);
        manga.setVolumes(new ArrayList<>());
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

    private void setupCoverArea() {
        coverContainer.setOnDragOver(event -> {
            if (event.getDragboard().hasImage() || event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        coverContainer.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            Image image = null;

            try {
                if (db.hasImage()) {
                    image = db.getImage();
                } else if (db.hasFiles()) {
                    File file = db.getFiles().getFirst();
                    image = new Image(file.toURI().toString());
                }

                if (image != null) {
                    Image resized = ImageUtils.resizeImage(image);
                    coverImageView.setImage(resized);
                    coverBytes = ImageUtils.imageToBytes(resized);
                    success = true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Fehler beim speichern des Covers.", e);
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void onDeleteCover() {
        coverImageView.setImage(null);
        if (manga != null) manga.setCoverImage(null);
    }

}
