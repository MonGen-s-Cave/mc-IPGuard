package hu.kxtsoo.ipguard.database;

import java.sql.SQLException;
import java.util.List;

public interface DatabaseInterface {
    void initialize() throws SQLException;

    void createTables() throws SQLException;
    void addPlayer(String uuid, String ipAddress) throws SQLException;
    boolean doesPlayerExist(String uuid) throws SQLException;
    String getPlayerIP(String uuid) throws SQLException;
    boolean removePlayer(String uuid) throws SQLException;
    List<String> getDatabasePlayerNames() throws SQLException;
    void close() throws SQLException;
}