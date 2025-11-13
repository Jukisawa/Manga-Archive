package com.jukisawa.mangaarchive.database;

import com.jukisawa.mangaarchive.util.SQLiteSchemaUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    public static void initDatabase() {

        String createMangaTbl = """
                CREATE TABLE IF NOT EXISTS manga (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        location TEXT NOT NULL,
                        state TEXT NOT NULL,
                        rating INTEGER NOT NULL
                    );
                """;
        String createGenreTbl = """
                CREATE TABLE IF NOT EXISTS genre (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL
                    );
                """;
        String createMangaGenreTbl = """
                CREATE TABLE IF NOT EXISTS manga_genre_nm (
                        manga_id INTEGER NOT NULL,
                        genre_id INTEGER NOT NULL
                    );
                """;
        String createVolumeTbl = """
                CREATE TABLE IF NOT EXISTS manga_volume (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        manga_id INTEGER NOT NULL,
                        volume INTEGER NOT NULL,
                        arc TEXT NOT NULL,
                        note TEXT NOT NULL
                    );
                """;
        String insertGenres = """
                 INSERT INTO genre(name) VALUES
                 ('Action'),
                 ('Adventure'),
                 ('Comedy'),
                 ('Crime'),
                 ('Drama'),
                 ('Fantasy'),
                 ('Gender Bender'),
                 ('Gore'),
                 ('Historical'),
                 ('Horror'),
                 ('Isekai'),
                 ('Magical Girls'),
                 ('Mature'),
                 ('Mecha'),
                 ('Medical'),
                 ('Mystery'),
                 ('Philosophical'),
                 ('Psychological'),
                 ('Romance'),
                 ('Sci-Fi'),
                 ('Shoujo Ai'),
                 ('Shounen Ai'),
                 ('Slice of Life'),
                 ('Sports'),
                 ('Superhero'),
                 ('Thriller'),
                 ('Tragedy'),
                 ('Wuxia'),
                 ('Aliens'),
                 ('Animals'),
                 ('Cooking'),
                 ('Crossdressing'),
                 ('Delinquents'),
                 ('Demons'),
                 ('Genderswap'),
                 ('Ghosts'),
                 ('Gyaru'),
                 ('Harem'),
                 ('Loli'),
                 ('Mafia'),
                 ('Magic'),
                 ('Martial Arts'),
                 ('Military'),
                 ('Monster Girls'),
                 ('Monsters'),
                 ('Music'),
                 ('Ninja'),
                 ('Office Workers'),
                 ('Police'),
                 ('Post-Apocalyptic'),
                 ('Reincarnation'),
                 ('Reverse Harem'),
                 ('Samurai'),
                 ('School Life'),
                 ('Supernatural'),
                 ('Survival'),
                 ('Time Travel'),
                 ('Traditional Games'),
                 ('Vampires'),
                 ('Video Games'),
                 ('Villainess'),
                 ('Virtual Reality'),
                 ('Zombies'),
                 ('Format4-Koma'),
                 ('Adaptation'),
                 ('Anthology'),
                 ('Award Winning'),
                 ('Doujinshi'),
                 ('Fan Colored'),
                 ('Full Color'),
                 ('Long Strip'),
                 ('Official Colored'),
                 ('Oneshot'),
                 ('User Created'),
                 ('Web Comic')
                 """;

        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createMangaTbl);
            stmt.execute(createGenreTbl);
            stmt.execute(createMangaGenreTbl);
            stmt.execute(createVolumeTbl);
            // Add default genres if needed
            SQLiteSchemaUtil.insertIfEmpty(conn, "genre", insertGenres);

            // Add missing columns
            SQLiteSchemaUtil.addColumnIfMissing(conn, "manga", "cover_image", "BLOB");
            SQLiteSchemaUtil.addColumnIfMissing(conn, "manga", "related", "TEXT");
            SQLiteSchemaUtil.addColumnIfMissing(conn, "manga", "alternate_name", "TEXT");


        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Db Initialisieren", e);
        }
    }
}
