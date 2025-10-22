package com.jukisawa.mangaarchive.ui.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.dto.MangaDTO;
import com.jukisawa.mangaarchive.dto.VolumeDTO;
import com.jukisawa.mangaarchive.service.GenreService;
import com.jukisawa.mangaarchive.service.MangaService;
import com.jukisawa.mangaarchive.service.VolumeService;
import com.jukisawa.mangaarchive.ui.components.Table;
import com.jukisawa.mangaarchive.util.StageIconHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class MangaViewController {
    private static final Logger LOGGER = Logger.getLogger(MangaViewController.class.getName());

    @FXML
    private TextField filterField;
    @FXML
    private CheckBox completedCb;
    @FXML
    private CheckBox abortedCb;
    @FXML
    private Button genreFilterButton;
    private final Map<GenreDTO, CheckBox> genreCheckBoxes = new HashMap<>();
    private Popup genrePopup;
    @FXML
    Table<MangaDTO> mangaTable;

    private MangaService mangaService;
    private GenreService genreService;
    private VolumeService volumeService;

    private ObservableList<MangaDTO> mangaList;
    private FilteredList<MangaDTO> filteredMangas;
    private final Map<Integer, ObservableList<VolumeDTO>> nestedVolumeMap = new HashMap<>();

    public void setService(MangaService mangaService, GenreService genreService, VolumeService volumeService) {
        this.mangaService = mangaService;
        this.genreService = genreService;
        this.volumeService = volumeService;
    }

    @FXML
    public void initialize() {
        mangaTable.addStringColumn("Name", 200, MangaDTO::getName, false, false);
        mangaTable.addStringColumn("Location", 200, MangaDTO::getLocation, false, false);
        mangaTable.addStringColumn("Abgeschlossen", 200, m -> m.isCompleted() ? "Ja" : "Nein", true, false);
        mangaTable.addStringColumn("Abgebrochen", 200, m -> m.isAborted() ? "Ja" : "Nein", true, false);
        mangaTable.addStringColumn("Genres", 200,
                m -> String.join(", ", m.getGenres().stream().map(GenreDTO::getName).toList()), false, false);
        mangaTable.addStringColumn("Rating", 200, m -> String.valueOf(m.getRating()), true, false);
        mangaTable.addActionColumn(
                this::onAddMangaClicked,
                this::onEditMangaClicked);

        mangaTable.setNestedTableProvider(manga -> {
            Table<VolumeDTO> volumeTable = new Table<>();
            volumeTable.addStringColumn("Band", 100, v -> String.valueOf(v.getVolume()), true, false);
            volumeTable.addStringColumn("Arc", 200, VolumeDTO::getArc, false, false);
            volumeTable.addStringColumn("Notiz", 200, VolumeDTO::getNote, false, false);
            volumeTable.addActionColumn(
                    () -> onAddVolumeClicked(manga.getId()),
                    this::onEditVolumeClicked);


            ObservableList<VolumeDTO> observableVolumes = FXCollections.observableArrayList(manga.getVolumes());
            volumeTable.setItems(observableVolumes);

            // store for later updates
            nestedVolumeMap.put(manga.getId(), observableVolumes);
            return volumeTable;
        });

        setupFilter();
    }

    public void init() {
        loadManga();
        initGenrePopup();
        setupFilter();
    }

    private void initGenrePopup() {
        genrePopup = new Popup();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        List<GenreDTO> allGenres = genreService.getAllGenres();

        int maxColumns = 5;
        int row = 0;
        int col = 0;

        for (GenreDTO typ : allGenres) {
            CheckBox cb = new CheckBox(typ.getName());
            cb.selectedProperty().addListener((_, _, _) -> applyFilter());
            genreCheckBoxes.put(typ, cb);
            grid.add(cb, col, row);

            col++;
            if (col >= maxColumns) {
                col = 0;
                row++;
            }
        }

        genrePopup.getContent().add(grid);

        genreFilterButton.setOnAction(_ -> {
            if (!genrePopup.isShowing()) {
                // Zeige Popup direkt unter dem Button
                genrePopup.show(genreFilterButton,
                        genreFilterButton.localToScreen(0, genreFilterButton.getHeight()).getX(),
                        genreFilterButton.localToScreen(0, genreFilterButton.getHeight()).getY());
            } else {
                genrePopup.hide();
            }
        });
    }

    public void loadManga() {
        List<MangaDTO> mangas = mangaService.getAllMangas();
        mangaList = FXCollections.observableArrayList(mangas);
        filteredMangas = new FilteredList<>(mangaList, _ -> true);
        mangaTable.setItems(filteredMangas);
    }

    private void setupFilter() {
        filterField.textProperty().addListener((_, _, _) -> applyFilter());
        completedCb.selectedProperty().addListener((_, _, _) -> applyFilter());
        abortedCb.selectedProperty().addListener((_, _, _) -> applyFilter());
    }

    private void applyFilter() {
        String filterText = filterField.getText().toLowerCase().trim();

        filteredMangas.setPredicate(manga -> {

            // Checkbox Filter
            if (manga.isCompleted() && !completedCb.isSelected())
                return false;
            if (!manga.isAborted() && !abortedCb.isSelected())
                return false;

            // Dropdown Genre Filter
            List<GenreDTO> selectedGenreDTOs = genreCheckBoxes.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .toList();

            if (!selectedGenreDTOs.isEmpty()) {
                boolean genreMatch = manga.getGenres().stream().anyMatch(selectedGenreDTOs::contains);
                if (!genreMatch) {
                    return false;
                }
            }

            // Textbox filter
            if (filterText.isEmpty())
                return true;
            if (manga.getName().toLowerCase().contains(filterText))
                return true;
            if (manga.getVolumes() != null) {
                for (VolumeDTO volume : manga.getVolumes()) {
                    if (volume.getArc().toLowerCase().contains(filterText)) {
                        return true;
                    }
                }
            }

            return false;
        });
    }

    private void onAddVolumeClicked(int mangaId) {
        VolumeDTO volume = new VolumeDTO();
        volume.setMangaId(mangaId);
        openVolumePopup(volume);
    }

    private void onEditVolumeClicked(VolumeDTO volume) {
        openVolumePopup(volume);
    }

    private void openVolumePopup(VolumeDTO volume) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jukisawa/mangaarchive/fxml/VolumePopup.fxml"));
            Parent root = loader.load();

            VolumePopupController controller = loader.getController();
            controller.setService(volumeService);
            controller.setVolume(volume);

            Scene popupScene = new Scene(root);
            popupScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/jukisawa/mangaarchive/css/styles.css")).toExternalForm());

            Stage popupStage = new Stage();
            popupStage.setTitle(volume.getId() == 0 ? "Neuer Band erfassen" : "Band bearbeiten");
            popupStage.setScene(popupScene);
            popupStage.initModality(Modality.APPLICATION_MODAL); // blockiert Hauptfenster
            popupStage.setResizable(false);
            popupStage.getIcons().addAll(StageIconHelper.getIcons());

            // Optional Höhe dynamisch anpassen
            popupStage.sizeToScene();
            popupStage.showAndWait();

            if (controller.isSaved()) {
                VolumeDTO savedVolume = controller.getVolumeDTO();
                MangaDTO manga = mangaList.stream()
                        .filter(m -> m.getId() == savedVolume.getMangaId())
                        .findFirst()
                        .orElse(null);
                if (manga != null) {
                    manga.getVolumes().removeIf(v -> v.getId() == savedVolume.getId());
                    manga.getVolumes().add(savedVolume);

                    ObservableList<VolumeDTO> observableVolumes = nestedVolumeMap.get(manga.getId());
                    if (observableVolumes != null) {
                        observableVolumes.setAll(manga.getVolumes());
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim laden von resourcen", e);
        }
    }

    private void onAddMangaClicked() {
        openMangaPopup(null);
    }

    private void onEditMangaClicked(MangaDTO manga) {
        openMangaPopup(manga);
    }

    private void openMangaPopup(MangaDTO manga) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jukisawa/mangaarchive/fxml/MangaPopup.fxml"));
            Parent root = loader.load();

            MangaPopupController controller = loader.getController();
            controller.setService(mangaService);
            controller.setGenres(genreService.getAllGenres());
            controller.setManga(manga);

            Scene popupScene = new Scene(root);
            popupScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/jukisawa/mangaarchive/css/styles.css")).toExternalForm());

            Stage popupStage = new Stage();
            boolean newManga = manga == null;
            popupStage.setTitle(newManga ? "Neuer Manga erfassen" : "Manga bearbeiten");
            popupStage.setScene(popupScene);
            popupStage.initModality(Modality.APPLICATION_MODAL); // blockiert Hauptfenster
            popupStage.setResizable(false);
            popupStage.getIcons().addAll(StageIconHelper.getIcons());

            // Optional Höhe dynamisch anpassen
            popupStage.sizeToScene();
            popupStage.showAndWait();

            if (controller.isSaved()) {
                if (newManga) {
                    mangaList.add(controller.getMangaDTO());
                }
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim laden von resourcen", e);
        }
    }
}