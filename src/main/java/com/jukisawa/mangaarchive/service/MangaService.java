package com.jukisawa.mangaarchive.service;

import com.jukisawa.mangaarchive.dto.GenreDTO;
import com.jukisawa.mangaarchive.dto.MangaDTO;
import com.jukisawa.mangaarchive.dto.MangaGenreDTO;
import com.jukisawa.mangaarchive.dto.VolumeDTO;
import com.jukisawa.mangaarchive.repository.GenreRepository;
import com.jukisawa.mangaarchive.repository.MangaGenreRepository;
import com.jukisawa.mangaarchive.repository.MangaRepository;
import com.jukisawa.mangaarchive.repository.VolumeRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class MangaService {
    private final MangaRepository mangaRepository;
    private final MangaGenreRepository mangaGenreRepository;
    private final GenreRepository genreRepository;
    private final VolumeRepository volumeRepository;

    public MangaService(MangaRepository mangaRepository, MangaGenreRepository mangaGenreRepository,
            GenreRepository genreRepository,
            VolumeRepository volumeRepository) {
        this.mangaRepository = mangaRepository;
        this.mangaGenreRepository = mangaGenreRepository;
        this.genreRepository = genreRepository;
        this.volumeRepository = volumeRepository;
    }

    public void saveManga(MangaDTO mangaDTO) {

        // Manga Speichern
        if (mangaDTO.getId() == 0) {
            mangaRepository.addManga(mangaDTO);
        } else {
            mangaRepository.updateManga(mangaDTO);
        }

        // Manga Genre Relation Speichern
        List<Integer> genreIds = mangaDTO.getGenres().stream().map(GenreDTO::getId).toList();
        mangaGenreRepository.saveMangaGenreRelation(mangaDTO.getId(), genreIds);
    }

    public List<MangaDTO> getAllMangas() {
        List<MangaDTO> result = mangaRepository.getAll();
        List<Integer> mangaIds = result.stream().map(MangaDTO::getId).toList();

        List<MangaGenreDTO> mangaGenreDTOs = mangaGenreRepository.getByMangaIds(mangaIds);
        List<VolumeDTO> volumeDTOs = volumeRepository.getByMangaIds(mangaIds);
        List<GenreDTO> genreDTOs = genreRepository.getAll();


        Map<Integer, List<VolumeDTO>> volumeMap = volumeDTOs.stream()
                .collect(Collectors.groupingBy(VolumeDTO::getMangaId));

        Map<Integer, GenreDTO> genreById = genreDTOs.stream()
                .collect(Collectors.toMap(GenreDTO::getId, Function.identity()));
        Map<Integer, List<GenreDTO>> mangaGenreMap = new HashMap<>();

        for (MangaGenreDTO rel : mangaGenreDTOs) {
            int kundeId = rel.getMangaId();
            int typId = rel.getGenreId();

            GenreDTO typ = genreById.get(typId);
            if (typ != null) {
                mangaGenreMap.computeIfAbsent(kundeId, _ -> new ArrayList<>()).add(typ);
            }
        }

        for (MangaDTO mangaDTO : result) {
            mangaDTO.setGenres(mangaGenreMap.getOrDefault(mangaDTO.getId(), new ArrayList<>()));

            mangaDTO.setVolumes(volumeMap.getOrDefault(mangaDTO.getId(), new ArrayList<>()));
        }

        return result;
    }
}
