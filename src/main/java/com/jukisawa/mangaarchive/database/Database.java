package com.jukisawa.mangaarchive.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:app.db"; // Datei im Projektverzeichnis

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Verbinden zur Datenbank", e);
        }
    }
}