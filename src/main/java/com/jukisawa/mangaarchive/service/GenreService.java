package com.jukisawa.mangaarchive.service;

import com.jukisawa.mangaarchive.database.TransactionManager;
import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.repository.GenreRepository;
import com.jukisawa.mangaarchive.repository.MangaGenreRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenreService {

    private final TransactionManager transactionManager;
    private final GenreRepository genreRepository;
    private final MangaGenreRepository mangaGenreRepository;
    private final Map<Integer, GenreDTO> genreCache = new HashMap<>();

    public GenreService(TransactionManager transactionManager, GenreRepository genreRepository, MangaGenreRepository mangaGenreRepository) {
        this.transactionManager = transactionManager;
        this.genreRepository = genreRepository;
        this.mangaGenreRepository = mangaGenreRepository;
        loadAllGenres();
    }

    public void saveGenre(GenreDTO genreDTO)  throws Exception  {
        transactionManager.beginTransaction();
        try {
            if (genreDTO.getId() == 0) {
                genreRepository.addGenre(genreDTO);
                genreCache.put(genreDTO.getId(), genreDTO);
            } else {
                genreRepository.updateGenre(genreDTO);
            }
            transactionManager.commit();
        }
        catch (Exception e) {
            transactionManager.rollback();
            throw new Exception("Fehler beim speichern von Genre.", e);
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

    public void deleteGenre(GenreDTO genreDTO) throws Exception {
        transactionManager.beginTransaction();
        try {
            genreRepository.deleteGenre(genreDTO);
            mangaGenreRepository.deleteByGenreId(genreDTO.getId());
            genreCache.remove(genreDTO.getId());
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new Exception("Fehler beim löschen von Genre.", e);
        }



    }

}
