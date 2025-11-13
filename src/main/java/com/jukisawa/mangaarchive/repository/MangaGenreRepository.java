package com.jukisawa.mangaarchive.repository;

import com.jukisawa.mangaarchive.dto.MangaGenreDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MangaGenreRepository {
    private static final Logger LOGGER = Logger.getLogger(MangaGenreRepository.class.getName());
    private final Connection conn;

    public MangaGenreRepository(Connection conn) {
        this.conn = conn;
    }

    public void saveMangaGenreRelation(int mangaID, List<Integer> genreIds) {
        String deleteRealtaion = "DELETE FROM manga_genre_nm WHERE manga_id = " + mangaID;
        String insertMangaGenreNM = "INSERT INTO manga_genre_nm(manga_id, genre_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(deleteRealtaion)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim delete von alten manga genre nm einträgen", e);
        }

        try (PreparedStatement pstmt = conn.prepareStatement(insertMangaGenreNM)) {
            for (int genreId : genreIds) {
                pstmt.setInt(1, mangaID);
                pstmt.setInt(2, genreId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim insert von manga genre nm Einträgen", e);
        }
    }

    public List<MangaGenreDTO> getByMangaIds(List<Integer> mangaIds) {
        List<MangaGenreDTO> result = new ArrayList<>();
        String selectMangaGenre = """
                Select manga_id, genre_id
                from manga_genre_nm
                where manga_id in (%s)""";
        String inClause = mangaIds.stream().map(_ -> "?").collect(Collectors.joining(","));
        selectMangaGenre = String.format(selectMangaGenre, inClause);

        try (PreparedStatement pstmt = conn.prepareStatement(selectMangaGenre)) {
            int index = 1;
            for (Integer id : mangaIds) {
                pstmt.setInt(index++, id);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MangaGenreDTO mangaGenreDTO = new MangaGenreDTO(rs.getInt("manga_id"), rs.getInt("genre_id"));
                    result.add(mangaGenreDTO);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim select von manga genre nm by manga ids", e);
        }

        return result;
    }
}
