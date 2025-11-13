package com.jukisawa.mangaarchive.ui.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.dto.MangaDTO;
import com.jukisawa.mangaarchive.dto.MangaState;
import com.jukisawa.mangaarchive.dto.VolumeDTO;
import com.jukisawa.mangaarchive.service.GenreService;
import com.jukisawa.mangaarchive.service.MangaService;
import com.jukisawa.mangaarchive.service.VolumeService;
import com.jukisawa.mangaarchive.ui.components.HoverPopup;
import com.jukisawa.mangaarchive.ui.components.Table;
import com.jukisawa.mangaarchive.util.StageIconHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class MangaViewController {
    private static final Logger LOGGER = Logger.getLogger(MangaViewController.class.getName());

    //action
    @FXML
    public Button openGenreEditorButton;

    //filter
    @FXML
    private TextField filterField;
    @FXML
    private CheckBox completedCb;
    @FXML
    private CheckBox abortedCb;
    @FXML
    public CheckBox ongoingCb;
    @FXML
    private Button genreFilterButton;
    private final Map<GenreDTO, CheckBox> genreCheckBoxes = new HashMap<>();
    private Popup genrePopup;

    //table
    public HBox stats;
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
        mangaTable.addStringColumn("Alternativ Name", 200, MangaDTO::getAlternateName, false, false);
        mangaTable.addStringColumn("Location", 200, MangaDTO::getLocation, false, false);
        mangaTable.addStringColumn("Status", 200, m -> m.getState().getDisplayName(), true, false);
        mangaTable.addStringColumn("Genres", 200,
                m -> String.join(", ", m.getGenres().stream().map(GenreDTO::getName).toList()), false, false);
        mangaTable.addStringColumn("Rating", 200, m -> String.valueOf(m.getRating()), true, false);
        mangaTable.addActionColumn(
                this::onAddMangaClicked,
                this::onEditMangaClicked, null);

        //hover part
        HoverPopup<MangaDTO> hoverPopup = new HoverPopup<>(manga -> {
            VBox hoverBox = new VBox(5);
            if (manga.getCoverImage() != null) {
                ImageView img = new ImageView(new Image(new ByteArrayInputStream(manga.getCoverImage())));
                img.setFitWidth(150);
                img.setPreserveRatio(true);
                hoverBox.getChildren().add(img);
            }

            String relatedText = manga.getRelated() == null ? "" : manga.getRelated();
            Label related = new Label("Related: " + relatedText);
            hoverBox.getChildren().add(related);

            return hoverBox;
        });
        mangaTable.setHoverPopup(hoverPopup);

        //nested table
        mangaTable.setNestedTableProvider(manga -> {
            Table<VolumeDTO> volumeTable = new Table<>();
            volumeTable.addStringColumn("Band", 100, v -> String.valueOf(v.getVolume()), true, false);
            volumeTable.addStringColumn("Arc", 200, VolumeDTO::getArc, false, false);
            volumeTable.addStringColumn("Notiz", 200, VolumeDTO::getNote, false, false);
            volumeTable.addActionColumn(
                    () -> onAddVolumeClicked(manga.getId()),
                    this::onEditVolumeClicked, () -> onAddShiftVolumeClicked(manga.getId()));

            ObservableList<VolumeDTO> observableVolumes =
                    nestedVolumeMap.computeIfAbsent(manga.getId(),
                            _ -> FXCollections.observableArrayList(manga.getVolumes()));
            SortedList<VolumeDTO> sortedVolumes = new SortedList<>(observableVolumes);
            volumeTable.setItems(sortedVolumes);

            // store for later updates
            nestedVolumeMap.put(manga.getId(), observableVolumes);
            return volumeTable;
        });

        openGenreEditorButton.setOnAction(_ -> openGenreEditor());

        setupFilter();
    }

    public void init() {
        loadManga();
        initGenrePopup();
        setupFilter();
        updateStats();
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

    private void updateStats() {
        String completed = "Abgeschlossen: " + (long) mangaList.stream().filter(m -> m.getState() == MangaState.COMPLETED).toList().size();
        String aborted = "Abgebrochen: " + (long) mangaList.stream().filter(m -> m.getState() == MangaState.ABORTED).toList().size();
        String ongoing = "Laufend: " + (long) mangaList.stream().filter(m -> m.getState() == MangaState.ONGOING).toList().size();
        String mangaTotal = "Serien: " + (long) mangaList.size();
        String volumeTotal = "Total Manga: " + mangaList.stream()
                .mapToLong(manga -> manga.getVolumes().size())
                .sum();
        Label completedLbl = new Label(completed);
        Label abortedLbl = new Label(aborted);
        Label ongoingLbl = new Label(ongoing);
        Label mangaTotalLbl = new Label(mangaTotal);
        Label volumeTotalLbl = new Label(volumeTotal);
        completedLbl.setStyle("-fx-font-weight: bold;");
        abortedLbl.setStyle("-fx-font-weight: bold;");
        mangaTotalLbl.setStyle("-fx-font-weight: bold;");
        volumeTotalLbl.setStyle("-fx-font-weight: bold;");
        stats.getChildren().clear();
        stats.getChildren().addAll(completedLbl, abortedLbl, ongoingLbl, mangaTotalLbl, volumeTotalLbl);
    }

    public void loadManga() {
        List<MangaDTO> mangas = mangaService.getAllMangas();
        mangaList = FXCollections.observableArrayList(mangas);
        filteredMangas = new FilteredList<>(mangaList, _ -> true);
        SortedList<MangaDTO> sortedMangas = new SortedList<>(filteredMangas);
        mangaTable.setItems(sortedMangas);
    }

    private void setupFilter() {
        filterField.textProperty().addListener((_, _, _) -> applyFilter());
        completedCb.selectedProperty().addListener((_, _, _) -> applyFilter());
        abortedCb.selectedProperty().addListener((_, _, _) -> applyFilter());
        ongoingCb.selectedProperty().addListener((_, _, _) -> applyFilter());
    }

    private void applyFilter() {
        String filterText = filterField.getText().toLowerCase().trim();

        filteredMangas.setPredicate(manga -> {

            // Checkbox Filter
            if (!completedCb.isSelected() && manga.getState() == MangaState.COMPLETED) {
                return false;
            }
            if (!abortedCb.isSelected() && manga.getState() == MangaState.ABORTED) {
                return false;
            }
            if (!ongoingCb.isSelected() && manga.getState() == MangaState.ONGOING) {
                return false;
            }

            // Dropdown Genre Filter
            List<GenreDTO> selectedGenreDTOs = genreCheckBoxes.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .toList();

            if (!selectedGenreDTOs.isEmpty()) {
                boolean genreMatch = new HashSet<>(manga.getGenres()).containsAll(selectedGenreDTOs);
                if (!genreMatch) {
                    return false;
                }
            }

            // Textbox filter
            if (filterText.isEmpty())
                return true;
            if (manga.getName().toLowerCase().contains(filterText))
                return true;
            if (manga.getAlternateName() != null && manga.getAlternateName().toLowerCase().contains(filterText))
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
        openVolumeEditor(volume);
    }

    private void onAddShiftVolumeClicked(int mangaId) {
        VolumeDTO volume = new VolumeDTO();
        MangaDTO manga = mangaList.stream()
                .filter(m -> m.getId() == mangaId)
                .findFirst()
                .orElse(null);
        if (manga != null) {
            VolumeDTO lastVolume = manga.getVolumes().stream().max(Comparator.comparingInt(VolumeDTO::getVolume))
                    .orElse(null);
            if (lastVolume != null) {
                volume.setArc(lastVolume.getArc());
                volume.setVolume(lastVolume.getVolume() + 1);
                volume.setNote(lastVolume.getNote());
                volume.setMangaId(mangaId);
                volumeService.saveVolume(volume);
                nestedVolumeMap.get(manga.getId()).add(volume);
                manga.getVolumes().add(volume);
                updateStats();
            }
        }

    }

    private void onEditVolumeClicked(VolumeDTO volume) {
        openVolumeEditor(volume);
    }

    private void openVolumeEditor(VolumeDTO volume) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jukisawa/mangaarchive/fxml/VolumeEdit.fxml"));
            Parent root = loader.load();

            VolumeEditController controller = loader.getController();
            controller.setService(volumeService);
            controller.setVolume(volume);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/jukisawa/mangaarchive/css/styles.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(volume.getId() == 0 ? "Neuer Band erfassen" : "Band bearbeiten");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // blockiert Hauptfenster
            stage.setResizable(false);
            stage.getIcons().addAll(StageIconHelper.getIcons());

            // Optional Höhe dynamisch anpassen
            stage.sizeToScene();
            stage.showAndWait();

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
                updateStats();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim laden von resourcen", e);
        }
    }

    private void onAddMangaClicked() {
        openMangaEditor(null);
    }

    private void onEditMangaClicked(MangaDTO manga) {
        openMangaEditor(manga);
    }

    private void openMangaEditor(MangaDTO manga) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jukisawa/mangaarchive/fxml/MangaEdit.fxml"));
            Parent root = loader.load();

            MangaEditController controller = loader.getController();
            controller.setService(mangaService);
            controller.setGenres(genreService.getAllGenres());
            controller.setManga(manga);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/jukisawa/mangaarchive/css/styles.css")).toExternalForm());

            Stage stage = new Stage();
            boolean newManga = manga == null;
            stage.setTitle(newManga ? "Neuer Manga erfassen" : "Manga bearbeiten");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // blockiert Hauptfenster
            stage.setResizable(false);
            stage.getIcons().addAll(StageIconHelper.getIcons());

            // Optional Höhe dynamisch anpassen
            stage.sizeToScene();
            stage.showAndWait();

            if (controller.isSaved()) {
                if (newManga) {
                    mangaList.add(controller.getMangaDTO());
                }
                updateStats();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim laden von resourcen", e);
        }
    }

    private void openGenreEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jukisawa/mangaarchive/fxml/GenreEdit.fxml"));
            Parent root = loader.load();

            GenreEditController controller = loader.getController();
            controller.setService(genreService);
            controller.postInit();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/jukisawa/mangaarchive/css/styles.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Genre bearbeiten");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // blockiert Hauptfenster
            stage.setResizable(false);
            stage.getIcons().addAll(StageIconHelper.getIcons());

            // Optional Höhe dynamisch anpassen
            stage.sizeToScene();
            stage.showAndWait();

            initGenrePopup();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim laden von resourcen", e);
        }
    }
}