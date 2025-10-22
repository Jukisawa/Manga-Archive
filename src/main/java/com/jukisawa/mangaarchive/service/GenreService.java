package com.jukisawa.mangaarchive.service;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.repository.GenreRepository;

import java.util.List;

public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public void saveGenre(GenreDTO genreDTO) {
        if (genreDTO.getId() == 0) {
            genreRepository.addGenre(genreDTO);
        } else {
            genreRepository.updateGenre(genreDTO);
        }
    }

    public List<GenreDTO> getAllGenres() {
        return genreRepository.getAll();
    }

}
