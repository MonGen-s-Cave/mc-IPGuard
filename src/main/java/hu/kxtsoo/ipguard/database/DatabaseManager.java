package hu.kxtsoo.ipguard.database;

import hu.kxtsoo.ipguard.database.impl.H2;
import hu.kxtsoo.ipguard.database.impl.MySQL;
import hu.kxtsoo.ipguard.database.impl.SQLite;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {
    private static DatabaseInterface database;

    public static void initialize(ConfigUtil configUtil, JavaPlugin plugin) throws SQLException {
        String driver = configUtil.getConfig().getString("storage.driver", "h2");
        switch (driver.toLowerCase()) {
            case "sqlite":
                database = new SQLite(plugin);
                database.initialize();
                break;
            case "mysql":
                database = new MySQL(configUtil, plugin);
                database.initialize();
                break;
            case "h2":
                database = new H2(plugin, configUtil);
                database.initialize();
                break;
            default:
                throw new IllegalArgumentException("Unsupported database driver: " + driver);
        }
       database.createTables();
    }

    public static void addPlayer(String uuid, String ipAddress) throws SQLException {
        database.addPlayer(uuid, ipAddress);
    }

    public static boolean doesPlayerExist(String uuid) throws SQLException {
        return database.doesPlayerExist(uuid);
    }

    public static boolean removePlayer(String uuid) throws SQLException {
        return database.removePlayer(uuid);
    }

    public static List<String> getDatabasePlayerNames() throws SQLException {
        return database.getDatabasePlayerNames();
    }
    public static String getPlayerIP(String uuid) throws SQLException {
        return database.getPlayerIP(uuid);
    }

    public static void close() throws SQLException {
        if (database != null) {
            database.close();
        }
    }
}
