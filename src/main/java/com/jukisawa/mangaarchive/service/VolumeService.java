package com.jukisawa.mangaarchive.service;

import com.jukisawa.mangaarchive.dto.VolumeDTO;
import com.jukisawa.mangaarchive.repository.VolumeRepository;

public class VolumeService {

    private final VolumeRepository volumeRepository;

    public VolumeService(VolumeRepository volumeRepository) {
        this.volumeRepository = volumeRepository;
    }

    public void saveVolume(VolumeDTO volumeDTO) {
        if (volumeDTO.getId() == 0) {
            volumeRepository.addVolume(volumeDTO);
        } else {
            volumeRepository.updateVolume(volumeDTO);
        }
    }

}
