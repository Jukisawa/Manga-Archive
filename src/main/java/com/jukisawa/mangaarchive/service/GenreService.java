package com.jukisawa.mangaarchive.service;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.repository.GenreRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenreService {

    private final GenreRepository genreRepository;
    private final Map<Integer, GenreDTO> genreCache = new HashMap<>();

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
        loadAllGenres();
    }

    public void saveGenre(GenreDTO genreDTO) {
        if (genreDTO.getId() == 0) {
            genreRepository.addGenre(genreDTO);
            genreCache.put(genreDTO.getId(), genreDTO);
        } else {
            genreRepository.updateGenre(genreDTO);
        }
    }

    private void loadAllGenres() {
        genreRepository.getAll().forEach(g -> genreCache.put(g.getId(), g));
    }

    public List<GenreDTO> getAllGenres() {
        return genreRepository.getAll();
    }

    public GenreDTO getGenreById(int id) {
        return genreCache.get(id);
    }

}
