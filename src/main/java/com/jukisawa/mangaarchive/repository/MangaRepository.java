package com.jukisawa.mangaarchive.repository;

import com.jukisawa.mangaarchive.dto.MangaDTO;

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
        String insertManga = "INSERT INTO manga(name, location, completed, aborted, rating) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertManga, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, mangaDTO.getName());
            pstmt.setString(2, mangaDTO.getLocation());
            pstmt.setBoolean(3, mangaDTO.isCompleted());
            pstmt.setBoolean(4, mangaDTO.isAborted());
            pstmt.setInt(5, mangaDTO.getRating());
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
        String updateManga = "UPDATE manga set name = ?, location = ?, completed = ?, aborted = ?, rating = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateManga)) {
            pstmt.setString(1, mangaDTO.getName());
            pstmt.setString(2, mangaDTO.getLocation());
            pstmt.setBoolean(3, mangaDTO.isCompleted());
            pstmt.setBoolean(4, mangaDTO.isAborted());
            pstmt.setInt(5, mangaDTO.getRating());
            pstmt.setInt(6, mangaDTO.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim update von manga", e);
        }
    }

    public List<MangaDTO> getAll() {
        String selectManga = "SELECT id, name, location, completed, aborted, rating FROM manga";
        List<MangaDTO> Result = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectManga)) {
            while (rs.next()) {
                MangaDTO mangaDTO = new MangaDTO(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBoolean(4),
                        rs.getBoolean(5), null, rs.getInt(6), null);
                Result.add(mangaDTO);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim select von manga", e);
        }

        return Result;
    }
}