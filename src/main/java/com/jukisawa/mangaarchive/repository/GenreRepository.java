package com.jukisawa.mangaarchive.repository;

import com.jukisawa.mangaarchive.dto.GenreDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenreRepository {
    private static final Logger LOGGER = Logger.getLogger(GenreRepository.class.getName());

    private final Connection conn;

    public GenreRepository(Connection conn) {
        this.conn = conn;
    }

    public List<GenreDTO> getAll() {
        List<GenreDTO> result = new ArrayList<>();
        String selectGenres = """
                Select id, name
                from genre""";

        try (PreparedStatement pstmt = conn.prepareStatement(selectGenres)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GenreDTO genreDTO = new GenreDTO(rs.getInt(1), rs.getString(2));
                    result.add(genreDTO);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim select von Genre", e);
        }

        return result;
    }

    public void addGenre(GenreDTO genreDTO) {
        String insertVolume = "INSERT INTO genres (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertVolume, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, genreDTO.getName());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                keys.next();
                genreDTO.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim INSERT von Genre", e);
        }
    }

    public void updateGenre(GenreDTO genreDTO) {
        String updateManga = "UPDATE genres set name = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateManga)) {
            pstmt.setString(1, genreDTO.getName());
            pstmt.setInt(2, genreDTO.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim UPDATE von Genre", e);
        }
    }

}
