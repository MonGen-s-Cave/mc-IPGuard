package hu.kitsoo.gipguard.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kitsoo.gipguard.util.ConfigUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabaseManager implements DatabaseInterface {
    private HikariDataSource dataSource;

    @Override
    public void initialize(ConfigUtil configUtil) throws SQLException {
        HikariConfig config = new HikariConfig();
        String host = configUtil.getConfig().getString("database.host");
        int port = configUtil.getConfig().getInt("database.port");
        String databaseName = configUtil.getConfig().getString("database.name");
        String username = configUtil.getConfig().getString("database.username");
        String password = configUtil.getConfig().getString("database.password");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?useSSL=false";
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
        createPlayerTable();
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("No default initialization for MySQL");
    }

    @Override
    public void createPlayerTable() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS gig_players (" +
                         "player_name VARCHAR(255) PRIMARY KEY, " +
                         "ip_address VARCHAR(255))";
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
        String sql = "INSERT INTO gig_players (player_name, ip_address) VALUES (?, ?) ON DUPLICATE KEY UPDATE ip_address = VALUES(ip_address)";
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
}
