package com.jukisawa.mangaarchive.service;

import com.jukisawa.mangaarchive.database.TransactionManager;
import com.jukisawa.mangaarchive.dto.VolumeDTO;
import com.jukisawa.mangaarchive.repository.VolumeRepository;

import java.sql.SQLException;

public class VolumeService {

    private final TransactionManager transactionManager;
    private final VolumeRepository volumeRepository;

    public VolumeService(TransactionManager transactionManager, VolumeRepository volumeRepository) {
        this.transactionManager = transactionManager;
        this.volumeRepository = volumeRepository;
    }

    public void saveVolume(VolumeDTO volumeDTO) throws Exception {
        transactionManager.beginTransaction();
        try {
            if (volumeDTO.getId() == 0) {
                volumeRepository.addVolume(volumeDTO);
            } else {
                volumeRepository.updateVolume(volumeDTO);
            }
            transactionManager.commit();
        }
        catch (Exception e) {
            transactionManager.rollback();
            throw new Exception("Fehler beim speichern vom Volume.", e);
        }
    }

    public void deleteVolume(VolumeDTO volumeDTO) throws Exception {
        transactionManager.beginTransaction();
        try {
            volumeRepository.deleteByVolumeId(volumeDTO.getId());

            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new Exception("Fehler beim Löschen vom Volume.", e);
        }
    }

}
