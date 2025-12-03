package com.jukisawa.mangaarchive.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    // ThreadLocal speichert die Connection, spezifisch für den aktuellen Thread.
    // Das stellt sicher, dass alle Aufrufe innerhalb eines Service-Methoden-Aufrufs
    // (und seiner Repositories) dieselbe Connection nutzen.
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    /**
     * Startet eine neue Transaktion.
     * Holt eine neue Connection aus der Factory und speichert sie im ThreadLocal.
     */
    public void beginTransaction() throws SQLException {
        if (threadConnection.get() != null) {
            throw new IllegalStateException("Transaktion ist bereits aktiv im aktuellen Thread.");
        }
        
        Connection connection = Database.getConnection();
        connection.setAutoCommit(false); // JDBC-Transaktion starten
        threadConnection.set(connection);
    }

    /**
     * Committet die Transaktion und gibt die Connection frei.
     */
    public void commit() throws SQLException {
        Connection connection = threadConnection.get();
        if (connection == null) {
            throw new IllegalStateException("Keine aktive Transaktion zum Commit.");
        }
        try {
            connection.commit();
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Rollt die Transaktion zurück und gibt die Connection frei.
     */
    public void rollback() {
        Connection connection = threadConnection.get();
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                System.err.println("Fehler beim Rollback: " + e.getMessage());
            } finally {
                closeConnection(connection);
            }
        }
    }

    /**
     * Gibt die aktive Connection des aktuellen Threads zurück. 
     * Diese Methode wird von den Repositories genutzt.
     */
    public Connection getConnection() {
        Connection connection = Database.getConnection();
        if (connection == null) {
            // Hier könnte man alternativ eine neue Connection im Auto-Commit-Modus zurückgeben,
            // wenn ein Repository *außerhalb* einer Transaktion operieren soll.
            throw new IllegalStateException("Keine aktive Transaktion gefunden.");
        }
        return connection;
    }

    private void closeConnection(Connection connection) {
        try {
            connection.close(); // Gibt die Connection an den Pool zurück
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen der Connection: " + e.getMessage());
        } finally {
            threadConnection.remove(); // Wichtig: Connection aus dem Thread entfernen
        }
    }
}