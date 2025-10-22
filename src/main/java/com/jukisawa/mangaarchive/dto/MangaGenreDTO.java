package com.jukisawa.mangaarchive.dto;

public class MangaGenreDTO {
    private final int mangaId;
    private final int genreId;

    public MangaGenreDTO(int mangaId, int genreId) {
        this.mangaId = mangaId;
        this.genreId = genreId;
    }

    public int getMangaId() {
        return mangaId;
    }


    public int getGenreId() {
        return genreId;
    }

}
