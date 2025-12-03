package com.jukisawa.mangaarchive.repository;

import com.jukisawa.mangaarchive.database.TransactionManager;
import com.jukisawa.mangaarchive.dto.GenreDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenreRepository {
    private static final Logger LOGGER = Logger.getLogger(GenreRepository.class.getName());

    private final TransactionManager transactionManager;

    public GenreRepository(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public List<GenreDTO> getAll() {
        Connection conn = transactionManager.getConnection();
        List<GenreDTO> result = new ArrayList<>();
        String selectGenres = """
                Select id, name
                from genre order by name""";

        try (PreparedStatement pstmt = conn.prepareStatement(selectGenres)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GenreDTO genreDTO = new GenreDTO(rs.getInt("id"), rs.getString("name"));
                    result.add(genreDTO);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim select von Genre", e);
        }

        return result;
    }

    public void addGenre(GenreDTO genreDTO) {
        Connection conn = transactionManager.getConnection();
        String insertGenre = "INSERT INTO genre (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertGenre, Statement.RETURN_GENERATED_KEYS)) {
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
        Connection conn = transactionManager.getConnection();
        String updateGenre = "UPDATE genre set name = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateGenre)) {
            pstmt.setString(1, genreDTO.getName());
            pstmt.setInt(2, genreDTO.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim UPDATE von Genre", e);
        }
    }

    public void deleteGenre(GenreDTO genreDTO) {
        Connection conn = transactionManager.getConnection();
        String deleteGenre = "DELETE FROM genre WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteGenre)) {
            pstmt.setInt(1, genreDTO.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim DELETEN von Genre", e);
        }
    }

}
