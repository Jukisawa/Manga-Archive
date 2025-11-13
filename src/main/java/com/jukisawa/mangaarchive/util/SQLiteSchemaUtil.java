package com.jukisawa.mangaarchive.util;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteSchemaUtil {
    private static final Logger LOGGER = Logger.getLogger(SQLiteSchemaUtil.class.getName());

    private SQLiteSchemaUtil() {}

    /**
     * Checks if a given table contains a specific column.
     */
    public static boolean columnExists(Connection conn, String tableName, String columnName) {
        String query = "SELECT COUNT(*) AS count FROM pragma_table_info(?) WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            pstmt.setString(2, columnName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim prüfen von Columns", e);
        }
        return false;
    }

    /**
     * Adds a column to a table if it does not exist yet.
     */
    public static void addColumnIfMissing(Connection conn, String tableName, String columnName, String columnDefinition) {
        if (!columnExists(conn, tableName, columnName)) {
            String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.printf("Added missing column '%s' to table '%s'%n", columnName, tableName);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Fehler beim hinzufügen von Column", e);
            }
        }
    }

    /**
     * Adds default data to a table if it's empty.
     */
    public static void insertIfEmpty(Connection conn, String tableName, String insertSQL) {
        String query = "SELECT COUNT(*) AS count FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next() && rs.getInt("count") == 0) {
                stmt.execute(insertSQL);
                System.out.printf("Inserted default data into table '%s'%n", tableName);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim hinzufügen von default data", e);
        }
    }
}
