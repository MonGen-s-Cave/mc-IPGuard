package hu.kxtsoo.ipguard.database;

import hu.kxtsoo.ipguard.util.ConfigUtil;

import java.sql.SQLException;
import java.util.List;

public interface DatabaseInterface {
    void initialize(ConfigUtil configUtil) throws SQLException;

    void initialize();

    void createPlayerTable() throws SQLException;
    void addPlayer(String playerName, String ipAddress) throws SQLException;
    boolean doesPlayerExist(String playerName) throws SQLException;
    String getPlayerIP(String playerName) throws SQLException;
    boolean removePlayer(String playerName) throws SQLException;
    List<String> getDatabasePlayerNames() throws SQLException;
    void close() throws SQLException;
}