package me.hasenzahn1.homemanager.db;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.tables.GroupInfosTable;
import me.hasenzahn1.homemanager.db.tables.HomesTable;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.migration.PluginMigrator;
import org.bukkit.World;

import java.sql.Connection;
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

    public PlayerHomes getHomesFromPlayer(UUID uuid, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomesFromPlayer(connection, uuid, group);
    }

    public int getHomeCountFromPlayer(UUID uuid, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomeCountFromPlayer(connection, uuid, group);
    }

    public void saveHomeToDatabase(UUID player, Home home) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).saveHomeToDatabase(connection, player, home);
    }

    public int getFreeHomes(UUID player, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(GroupInfosTable.class).getFreeHomes(connection, player, group);
    }

    public void saveFreeHomes(UUID player, String group, int maxSetHomes) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(GroupInfosTable.class).saveFreeHomes(connection, player, group, maxSetHomes);
    }

    public void incrementFreeHomes(UUID player, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(GroupInfosTable.class).incrementFreeHomes(connection, player, group);
    }

    public void decrementFreeHomes(UUID player, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(GroupInfosTable.class).decrementFreeHomes(connection, player, group);
    }

    public void deleteHomesFromTheDatabase(UUID player, String homeName, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).removeHomeFromDatabase(connection, player, homeName, group);
    }

    public void bulkAddHomeFromMigration(List<PluginMigrator.HomeData> data) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).bulkAddHomeFromMigration(connection, data);
    }

    public int cleanupHomes(List<World> worlds) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).cleanupHomes(connection, worlds);
    }

    public int purgeHomeInWorld(World world) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).purgeHomesInWorld(connection, world);
    }

    public void destroy() {
        database.close(connection);
        connection = null;
        database = null;
    }

}
