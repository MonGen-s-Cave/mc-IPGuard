package hu.kxtsoo.ipguard.database.impl;

import hu.kxtsoo.ipguard.database.DatabaseInterface;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLite implements DatabaseInterface {

    private final JavaPlugin plugin;
    private Connection connection;

    public SQLite(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String url = "jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath();
        connection = DriverManager.getConnection(url);

        createTables();
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS ipguard_players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "ip_address TEXT)";
            statement.execute(sql);
        }
    }

    @Override
    public void addPlayer(String uuid, String ipAddress) throws SQLException {
        String sql = "INSERT OR REPLACE INTO ipguard_players (uuid, ip_address) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setString(2, ipAddress);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean doesPlayerExist(String uuid) throws SQLException {
        String sql = "SELECT 1 FROM ipguard_players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public String getPlayerIP(String uuid) throws SQLException {
        String sql = "SELECT ip_address FROM ipguard_players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("ip_address") : "N/A";
            }
        }
    }

    @Override
    public boolean removePlayer(String uuid) throws SQLException {
        String sql = "DELETE FROM ipguard_players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public List<String> getDatabasePlayerNames() throws SQLException {
        List<String> uuids = new ArrayList<>();
        String sql = "SELECT uuid FROM ipguard_players";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                uuids.add(resultSet.getString("uuid"));
            }
        }
        return uuids;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
