package hu.kitsoo.gipguard.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kitsoo.gipguard.util.ConfigUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static HikariDataSource dataSource;

    public static void initialize(ConfigUtil configUtil) {
        String driver = configUtil.getConfig().getString("database.driver");
        String host = configUtil.getConfig().getString("database.host");
        int port = configUtil.getConfig().getInt("database.port");
        String databaseName = configUtil.getConfig().getString("database.name");
        String username = configUtil.getConfig().getString("database.username");
        String password = configUtil.getConfig().getString("database.password");

        HikariConfig config = new HikariConfig();
        if (driver.equalsIgnoreCase("sqlite")) {
            String jdbcUrl = "jdbc:sqlite:" + databaseName;
            config.setJdbcUrl(jdbcUrl);
        } else if (driver.equalsIgnoreCase("mysql")) {
            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
        } else {
            throw new IllegalArgumentException("Unsupported database driver: " + driver);
        }
        config.setMaximumPoolSize(configUtil.getConfig().getInt("database.pool.maximumPoolSize"));
        config.setMinimumIdle(configUtil.getConfig().getInt("database.pool.minimumIdle"));
        config.setConnectionTimeout(configUtil.getConfig().getInt("database.pool.connectionTimeout"));
        config.setMaxLifetime(configUtil.getConfig().getInt("database.pool.maxLifetime"));
        config.setIdleTimeout(configUtil.getConfig().getInt("database.pool.idleTimeout"));

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource has not been initialized.");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static String getPlayerIP(String playerName) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT ip_address FROM gig_players WHERE player_name = ?")) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("ip_address") : "N/A";
            }
        }
    }

    public static void addPlayer(String playerName, String ipAddress) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO gig_players (player_name, ip_address) VALUES (?, ?)")) {
            statement.setString(1, playerName);
            statement.setString(2, ipAddress);
            statement.executeUpdate();
        }
    }

    public static boolean doesPlayerExist(String playerName) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM gig_players WHERE player_name = ?")) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public static void updatePlayerIP(String playerName, String ipAddress) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE gig_players SET ip_address = ? WHERE player_name = ?")) {
            statement.setString(1, ipAddress);
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }

    public static boolean removePlayer(String playerName) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM gig_players WHERE player_name = ?")) {
            statement.setString(1, playerName);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static List<String> getDatabasePlayerNames() throws SQLException {
        List<String> playerNames = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT player_name FROM gig_players");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String playerName = resultSet.getString("player_name");
                playerNames.add(playerName);
            }
        }
        return playerNames;
    }

    public static String getDatabaseType() {
        return dataSource.getJdbcUrl().contains("mysql") ? "mysql" : "sqlite";
    }

}
