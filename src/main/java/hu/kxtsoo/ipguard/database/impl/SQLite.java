package hu.kxtsoo.ipguard.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kxtsoo.ipguard.database.DatabaseInterface;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLite implements DatabaseInterface {
    private HikariDataSource dataSource;

    public void initialize() {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = "jdbc:sqlite:plugins/gIPGuard/database.db";
        config.setJdbcUrl(jdbcUrl);

        dataSource = new HikariDataSource(config);

        try {
            createPlayerTable();
            Bukkit.getLogger().info("SQLite player table created successfully");
        } catch (SQLException e) {
            Bukkit.getLogger().info("Failed to create SQLite player table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(ConfigUtil configUtil) {
        throw new UnsupportedOperationException("Config not used for SQLite initialization");
    }

    @Override
    public void createPlayerTable() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS gig_players (" +
                    "player_name TEXT PRIMARY KEY, " +
                    "ip_address TEXT)";
            stmt.execute(sql);
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public void addPlayer(String playerName, String ipAddress) throws SQLException {
        String sql = "INSERT OR REPLACE INTO gig_players (player_name, ip_address) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerName);
            statement.setString(2, ipAddress);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean doesPlayerExist(String playerName) throws SQLException {
        String sql = "SELECT 1 FROM gig_players WHERE player_name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public String getPlayerIP(String playerName) throws SQLException {
        String sql = "SELECT ip_address FROM gig_players WHERE player_name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("ip_address") : "N/A";
            }
        }
    }

    @Override
    public boolean removePlayer(String playerName) throws SQLException {
        String sql = "DELETE FROM gig_players WHERE player_name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerName);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public List<String> getDatabasePlayerNames() throws SQLException {
        List<String> playerNames = new ArrayList<>();
        String sql = "SELECT player_name FROM gig_players";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                playerNames.add(resultSet.getString("player_name"));
            }
        }
        return playerNames;
    }
}