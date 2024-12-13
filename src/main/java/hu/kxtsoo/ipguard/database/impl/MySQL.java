package hu.kxtsoo.ipguard.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kxtsoo.ipguard.database.DatabaseInterface;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQL implements DatabaseInterface {
    private final ConfigUtil configUtil;
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public MySQL(ConfigUtil configUtil, JavaPlugin plugin) {
        this.configUtil = configUtil;
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();

        String host = configUtil.getConfig().getString("storage.host", "localhost");
        String port = configUtil.getConfig().getString("storage.port", "3306");
        String database = configUtil.getConfig().getString("storage.name", "database_name");
        String username = configUtil.getConfig().getString("storage.username", "root");
        String password = configUtil.getConfig().getString("storage.password", "");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(configUtil.getConfig().getInt("storage.pool.maximumPoolSize", 10));
        hikariConfig.setMinimumIdle(configUtil.getConfig().getInt("storage.pool.minimumIdle", 5));
        hikariConfig.setConnectionTimeout(configUtil.getConfig().getInt("storage.pool.connectionTimeout", 30000));
        hikariConfig.setMaxLifetime(configUtil.getConfig().getInt("storage.pool.maxLifetime", 1800000));
        hikariConfig.setIdleTimeout(configUtil.getConfig().getInt("storage.pool.idleTimeout", 600000));

        dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    @Override
    public void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS ipguard_players (" +
                    "uuid CHAR(36) PRIMARY KEY, " +
                    "ip_address VARCHAR(255))";
            stmt.execute(sql);
        }
    }

    @Override
    public void addPlayer(String uuid, String ipAddress) throws SQLException {
        String sql = "INSERT INTO ipguard_players (uuid, ip_address) VALUES (?, ?) ON DUPLICATE KEY UPDATE ip_address = VALUES(ip_address)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setString(2, ipAddress);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean doesPlayerExist(String uuid) throws SQLException {
        String sql = "SELECT 1 FROM ipguard_players WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean removePlayer(String uuid) throws SQLException {
        String sql = "DELETE FROM ipguard_players WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public List<String> getDatabasePlayerNames() throws SQLException {
        List<String> uuids = new ArrayList<>();
        String sql = "SELECT uuid FROM ipguard_players";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("ip_address") : "N/A";
            }
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}