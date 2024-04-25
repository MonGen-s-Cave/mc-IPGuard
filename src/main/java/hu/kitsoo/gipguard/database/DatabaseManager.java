package hu.kitsoo.gipguard.database;

import hu.kitsoo.gipguard.util.ConfigUtil;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {
    private static DatabaseInterface database;

    public static void initialize(ConfigUtil configUtil) throws SQLException {
        String driver = configUtil.getConfig().getString("database.driver");
        switch (driver.toLowerCase()) {
            case "sqlite":
                database = new SQLiteDatabaseManager();
                database.initialize();
                break;
            case "mysql":
                database = new MySQLDatabaseManager();
                database.initialize(configUtil);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database driver: " + driver);
        }
       database.createPlayerTable();
    }


    public static void addPlayer(String playerName, String ipAddress) throws SQLException {
        database.addPlayer(playerName, ipAddress);
    }

    public static boolean doesPlayerExist(String playerName) throws SQLException {
        return database.doesPlayerExist(playerName);
    }

    public static boolean removePlayer(String playerName) throws SQLException {
        return database.removePlayer(playerName);
    }

    public static List<String> getDatabasePlayerNames() throws SQLException {
        return database.getDatabasePlayerNames();
    }
    public static String getPlayerIP(String playerName) throws SQLException {
        return database.getPlayerIP(playerName);
    }


    public static void close() throws SQLException {
        if (database != null) {
            database.close();
        }
    }

    public static String getDatabaseType() {
        return database instanceof MySQLDatabaseManager ? "mysql" : "sqlite";
    }
}
