package com.jukisawa.mangaarchive.repository;

import com.jukisawa.mangaarchive.dto.VolumeDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VolumeRepository {
    private static final Logger LOGGER = Logger.getLogger(VolumeRepository.class.getName());

    private final Connection conn;

    public VolumeRepository(Connection conn) {
        this.conn = conn;
    }

    public List<VolumeDTO> getByMangaIds(List<Integer> mangaIds) {
        List<VolumeDTO> result = new ArrayList<>();
        String selectVolume = """
                Select id, manga_id, volume, arc, note
                from manga_volume
                where manga_id in (%s)""";
        String inClause = mangaIds.stream().map(_ -> "?").collect(Collectors.joining(","));
        selectVolume = String.format(selectVolume, inClause);

        try (PreparedStatement pstmt = conn.prepareStatement(selectVolume)) {
            int index = 1;
            for (Integer id : mangaIds) {
                pstmt.setInt(index++, id);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    VolumeDTO volumeDTO = new VolumeDTO(rs.getInt("id"), rs.getInt("manga_id"), rs.getInt("volume"), rs.getString("arc"),
                            rs.getString("note"));
                    result.add(volumeDTO);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim select von Volume by Manga Ids", e);
        }

        return result;
    }

    public void addVolume(VolumeDTO volumeDTO) {
        String insertVolume = "INSERT INTO manga_volume (manga_id, volume, arc, note) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertVolume, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, volumeDTO.getMangaId());
            pstmt.setInt(2, volumeDTO.getVolume());
            pstmt.setString(3, volumeDTO.getArc());
            pstmt.setString(4, volumeDTO.getNote());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                keys.next();
                volumeDTO.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim insert von Volume", e);
        }
    }

    public void updateVolume(VolumeDTO volumeDTO) {
        String updateManga = "UPDATE manga_volume set manga_id = ?, volume = ?, arc = ?, note = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateManga)) {
            pstmt.setInt(1, volumeDTO.getMangaId());
            pstmt.setInt(2, volumeDTO.getVolume());
            pstmt.setString(3, volumeDTO.getArc());
            pstmt.setString(4, volumeDTO.getNote());
            pstmt.setInt(6, volumeDTO.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim update von Volume", e);
        }
    }
}
