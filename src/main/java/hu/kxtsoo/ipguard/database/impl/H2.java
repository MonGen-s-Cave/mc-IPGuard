package hu.kxtsoo.ipguard.database.impl;

import hu.kxtsoo.ipguard.IPGuard;
import hu.kxtsoo.ipguard.database.DatabaseInterface;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.h2.jdbc.JdbcConnection;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class H2 implements DatabaseInterface {

    private final JavaPlugin plugin;
    private Connection connection;
    private final ConfigUtil configUtil;

    public H2(JavaPlugin plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            connection = new JdbcConnection("jdbc:h2:./" + IPGuard.getInstance().getDataFolder() + "/data;mode=MySQL", new Properties(), null, null, false);
            connection.setAutoCommit(true);

            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the H2 database", e);
        }
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS ipguard_players (" +
                    "uuid CHAR(36) PRIMARY KEY, " +
                    "ip_address VARCHAR(255))";
            stmt.execute(sql);
        }
    }

    @Override
    public void addPlayer(String uuid, String ipAddress) throws SQLException {
        String sql = "MERGE INTO ipguard_players (uuid, ip_address) KEY(uuid) VALUES (?, ?)";
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
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not close the H2 database connection", e);
        }
    }
}