package com.jukisawa.mangaarchive.dto;

public enum MangaState {
    ONGOING,
    COMPLETED,
    ABORTED,
    HIATUS,
    MISSING;

    public String getDisplayName() {
        return switch (this) {
            case ONGOING -> "Laufend";
            case COMPLETED -> "Abgeschlossen";
            case ABORTED -> "Abgebrochen";
            case HIATUS -> "Hiatus";
            case MISSING -> "Unvollständig";
        };
    }
}