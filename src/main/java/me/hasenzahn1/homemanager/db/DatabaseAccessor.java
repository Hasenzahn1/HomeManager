package me.hasenzahn1.homemanager.db;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.tables.GroupInfosTable;
import me.hasenzahn1.homemanager.db.tables.HomesTable;
import me.hasenzahn1.homemanager.migration.PluginMigrator;
import org.bukkit.Location;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
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

    public HashMap<String, Location> getHomesFromPlayer(UUID uuid, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomesFromPlayer(connection, uuid, group);
    }

    public int getHomeCountFromPlayer(UUID uuid, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomeCountFromPlayer(connection, uuid, group);
    }

    public void saveHomeToDatabase(UUID player, String name, Location location) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).saveHomeToDatabase(connection, player, name, location);
    }

    public int getFreeHomes(UUID player, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(GroupInfosTable.class).getFreeHomes(connection, player, group);
    }

    public void saveFreeHomes(UUID player, String group, int maxSetHomes) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(GroupInfosTable.class).saveFreeHomes(connection, player, group, maxSetHomes);
    }

    public void deleteHomesFromTheDatabase(UUID player, String homeName, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).removeHomeFromDatabase(connection, player, homeName, group);
    }

    public void bulkAddHomeFromMigration(List<PluginMigrator.HomeData> data) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).bulkAddHomeFromMigration(connection, data);
    }

    public void destroy() {
        database.close(connection);
        connection = null;
        database = null;
    }

}
