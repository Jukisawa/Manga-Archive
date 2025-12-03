package com.jukisawa.mangaarchive;

import com.jukisawa.mangaarchive.database.Database;
import com.jukisawa.mangaarchive.database.DatabaseInitializer;
import com.jukisawa.mangaarchive.database.TransactionManager;
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
        TransactionManager transactionManager = new TransactionManager();
        DatabaseInitializer.initDatabase();

        // Repositories erstellen
        MangaRepository kundeRepository = new MangaRepository(transactionManager);
        GenreRepository genreRepository = new GenreRepository(transactionManager);
        MangaGenreRepository mangaGenreRepository = new MangaGenreRepository(transactionManager);
        VolumeRepository volumeRepository = new VolumeRepository(transactionManager);

        // Services erstellen
        GenreService genreService = new GenreService(transactionManager, genreRepository, mangaGenreRepository);
        MangaService mangaService = new MangaService(transactionManager, kundeRepository, mangaGenreRepository, genreService,
                volumeRepository);
        VolumeService volumeService = new VolumeService(transactionManager, volumeRepository);

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
                    transactionManager.getConnection().close();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Fehler beim schließen der Db verbindung", ex);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fehler beim starten der App", e);
        }
    }
}
