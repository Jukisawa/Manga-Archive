package com.jukisawa.mangaarchive;

import com.jukisawa.mangaarchive.database.Database;
import com.jukisawa.mangaarchive.database.DatabaseInitializer;
import com.jukisawa.mangaarchive.repository.GenreRepository;
import com.jukisawa.mangaarchive.repository.MangaGenreRepository;
import com.jukisawa.mangaarchive.repository.MangaRepository;
import com.jukisawa.mangaarchive.repository.VolumeRepository;
import com.jukisawa.mangaarchive.service.GenreService;
import com.jukisawa.mangaarchive.service.MangaService;
import com.jukisawa.mangaarchive.service.VolumeService;
import com.jukisawa.mangaarchive.ui.controller.MangaViewController;
import com.jukisawa.mangaarchive.util.StageIconHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainApplication.class.getName());
    @Override
    public void start(Stage stage) {
        // Datenbankverbindung
        Connection connection = Database.getConnection();
        DatabaseInitializer.initDatabase();

        // Repositories erstellen
        MangaRepository kundeRepository = new MangaRepository(connection);
        GenreRepository genreRepository = new GenreRepository(connection);
        MangaGenreRepository mangaGenreRepository = new MangaGenreRepository(connection);
        VolumeRepository volumeRepository = new VolumeRepository(connection);

        // Services erstellen
        MangaService mangaService = new MangaService(kundeRepository, mangaGenreRepository, genreRepository,
                volumeRepository);
        GenreService genreService = new GenreService(genreRepository);
        VolumeService volumeService = new VolumeService(volumeRepository);

        try {
            // FXML & Controller laden
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jukisawa/mangaarchive/fxml/MangaView.fxml"));
            Scene scene = new Scene(loader.load());

            // Controller holen & Service übergeben
            MangaViewController controller = loader.getController();
            controller.setService(mangaService, genreService, volumeService);
            controller.init();

            // Scene + CSS
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/jukisawa/mangaarchive/css/styles.css")).toExternalForm());


            stage.setTitle("Manga Archiv");
            // Fenstericon setzen
            stage.getIcons().addAll(StageIconHelper.getIcons());
            stage.sizeToScene();
            stage.setScene(scene);
            stage.show();

            // Verbindung beim Schließen trennen
            stage.setOnCloseRequest(_ -> {
                try {
                    connection.close();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Fehler beim schließen der Db verbindung", ex);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fehler beim starten der App", e);
        }
    }
}
