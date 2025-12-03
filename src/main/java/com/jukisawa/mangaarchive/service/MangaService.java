package com.jukisawa.mangaarchive.service;

import com.jukisawa.mangaarchive.database.TransactionManager;
import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.dto.MangaDTO;
import com.jukisawa.mangaarchive.dto.MangaGenreDTO;
import com.jukisawa.mangaarchive.dto.VolumeDTO;
import com.jukisawa.mangaarchive.repository.MangaGenreRepository;
import com.jukisawa.mangaarchive.repository.MangaRepository;
import com.jukisawa.mangaarchive.repository.VolumeRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MangaService {
    private final TransactionManager transactionManager;
    private final MangaRepository mangaRepository;
    private final MangaGenreRepository mangaGenreRepository;
    private final GenreService genreService;
    private final VolumeRepository volumeRepository;

    public MangaService(TransactionManager transactionManager, MangaRepository mangaRepository, MangaGenreRepository mangaGenreRepository,
                        GenreService genreService,
                        VolumeRepository volumeRepository) {
        this.transactionManager = transactionManager;
        this.mangaRepository = mangaRepository;
        this.mangaGenreRepository = mangaGenreRepository;
        this.genreService = genreService;
        this.volumeRepository = volumeRepository;
    }

    public void saveManga(MangaDTO mangaDTO) throws Exception {

        transactionManager.beginTransaction();
        try {
            // Manga Speichern
            if (mangaDTO.getId() == 0) {
                mangaRepository.addManga(mangaDTO);
            } else {
                mangaRepository.updateManga(mangaDTO);
            }

            // Manga Genre Relation Speichern
            List<Integer> genreIds = mangaDTO.getGenres().stream().map(GenreDTO::getId).toList();
            mangaGenreRepository.saveMangaGenreRelation(mangaDTO.getId(), genreIds);
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new Exception("Fehler beim speichern von Manga.", e);
        }

    }

    public List<MangaDTO> getAllMangas() {
        List<MangaDTO> result = mangaRepository.getAll();
        List<Integer> mangaIds = result.stream().map(MangaDTO::getId).toList();

        List<MangaGenreDTO> mangaGenreDTOs = mangaGenreRepository.getByMangaIds(mangaIds);
        List<VolumeDTO> volumeDTOs = volumeRepository.getByMangaIds(mangaIds);

        Map<Integer, List<VolumeDTO>> volumeMap = volumeDTOs.stream()
                .collect(Collectors.groupingBy(VolumeDTO::getMangaId));

        Map<Integer, List<GenreDTO>> mangaGenreMap = new HashMap<>();

        for (MangaGenreDTO rel : mangaGenreDTOs) {
            int mangaId = rel.getMangaId();
            int genreId = rel.getGenreId();

            GenreDTO typ = genreService.getGenreById(genreId);
            if (typ != null) {
                mangaGenreMap.computeIfAbsent(mangaId, _ -> new ArrayList<>()).add(typ);
            }
        }

        for (MangaDTO mangaDTO : result) {
            mangaDTO.setGenres(mangaGenreMap.getOrDefault(mangaDTO.getId(), new ArrayList<>()));

            mangaDTO.setVolumes(volumeMap.getOrDefault(mangaDTO.getId(), new ArrayList<>()));
        }

        return result;
    }
}
