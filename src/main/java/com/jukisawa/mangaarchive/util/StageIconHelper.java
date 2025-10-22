package com.jukisawa.mangaarchive.util;

import javafx.scene.image.Image;

import java.util.List;
import java.util.Objects;

public class StageIconHelper {

    private StageIconHelper() {
        // Utility class -> keine Instanz erlaubt
    }

    public static List<Image> getIcons() {
        return List.of(
                new Image(Objects.requireNonNull(StageIconHelper.class.getResource(
                        "/com/jukisawa/mangaarchive/icons/icon64.png")).toExternalForm()),
                new Image(Objects.requireNonNull(StageIconHelper.class.getResource(
                        "/com/jukisawa/mangaarchive/icons/icon32.png")).toExternalForm()),
                new Image(Objects.requireNonNull(StageIconHelper.class.getResource(
                        "/com/jukisawa/mangaarchive/icons/icon16.png")).toExternalForm())
        );
    }
}
