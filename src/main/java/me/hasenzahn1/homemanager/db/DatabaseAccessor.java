package me.hasenzahn1.homemanager.db;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.tables.HomesTable;
import org.bukkit.Location;

import java.sql.Connection;
import java.util.HashMap;
import java.util.UUID;

public class DatabaseAccessor {

    private Database database;
    private Connection connection;

    private DatabaseAccessor(Database database) {
        this.database = database;
        this.connection = database.getSQLConnection();

    }

    public static DatabaseAccessor openSession() {
        return new DatabaseAccessor(HomeManager.getInstance().getDatabase());
    }

    public HashMap<String, Location> getHomesFromPlayer(UUID uuid) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomesFromPlayer(connection, uuid);
    }

    public int getHomeCountFromPlayer(UUID uuid) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomeCountFromPlayer(connection, uuid);
    }

    public void saveHomeToDatabase(UUID player, String name, Location location) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).saveHomeToDatabase(connection, player, name, location);
    }
    
    public void destroy() {
        database.close(connection);
        connection = null;
        database = null;
    }

}
