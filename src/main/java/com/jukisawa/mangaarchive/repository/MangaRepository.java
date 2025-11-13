package com.jukisawa.mangaarchive.repository;

import com.jukisawa.mangaarchive.dto.MangaDTO;
import com.jukisawa.mangaarchive.dto.MangaState;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MangaRepository {
    private static final Logger LOGGER = Logger.getLogger(MangaRepository.class.getName());

    private final Connection conn;

    public MangaRepository(Connection conn) {
        this.conn = conn;
    }

    public void addManga(MangaDTO mangaDTO) {
        String insertManga = "INSERT INTO manga(name, location, state, rating, cover_image, related, alternate_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertManga, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, mangaDTO.getName());
            pstmt.setString(2, mangaDTO.getLocation());
            pstmt.setString(3, mangaDTO.getState().name());
            pstmt.setInt(4, mangaDTO.getRating());
            if (mangaDTO.getCoverImage() != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(mangaDTO.getCoverImage());
                pstmt.setBlob(5, bais);
            } else {
                pstmt.setNull(5, java.sql.Types.BLOB);
            }
            pstmt.setString(6, mangaDTO.getRelated());
            pstmt.setString(7, mangaDTO.getAlternateName());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                keys.next();
                mangaDTO.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim insert von manga", e);
        }
    }

    public void updateManga(MangaDTO mangaDTO) {
        String updateManga = "UPDATE manga set name = ?, location = ?, state = ?, rating = ?, cover_image = ?, related = ?, alternate_name = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateManga)) {
            pstmt.setString(1, mangaDTO.getName());
            pstmt.setString(2, mangaDTO.getLocation());
            pstmt.setString(3, mangaDTO.getState().name());
            pstmt.setInt(4, mangaDTO.getRating());
            if (mangaDTO.getCoverImage() != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(mangaDTO.getCoverImage());
                pstmt.setBlob(5, bais);
            } else {
                pstmt.setNull(5, java.sql.Types.BLOB);
            }
            pstmt.setString(6, mangaDTO.getRelated());
            pstmt.setString(7, mangaDTO.getAlternateName());
            pstmt.setInt(8, mangaDTO.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim update von manga", e);
        }
    }

    public List<MangaDTO> getAll() {
        String selectManga = "SELECT id, name, location, state, rating, cover_image, related, alternate_name FROM manga";
        List<MangaDTO> Result = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectManga)) {
            while (rs.next()) {
                MangaDTO mangaDTO = new MangaDTO(rs.getInt("id"), rs.getString("name"), rs.getString("location"), MangaState.valueOf(rs.getString("state")),
                        null, rs.getInt("rating"), null, rs.getBytes("cover_image"), rs.getString("related"), rs.getString("alternate_name"));
                Result.add(mangaDTO);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim select von manga", e);
        }

        return Result;
    }
}