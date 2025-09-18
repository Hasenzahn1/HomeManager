package me.hasenzahn1.homemanager.db;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.tables.GroupInfosTable;
import me.hasenzahn1.homemanager.db.tables.HomesTable;
import me.hasenzahn1.homemanager.db.tables.VersionTable;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.migration.HomeMigrator;
import org.bukkit.Location;
import org.bukkit.World;

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

    public HashMap<WorldGroup, List<String>> getAllHomeNamesFromPlayer(UUID uuid) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getAllHomeNamesFromPlayer(connection, uuid);
    }

    public PlayerHomes getHomesFromPlayer(UUID uuid, WorldGroup group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomesFromPlayer(connection, uuid, group);
    }

    public int getHomeCountFromPlayer(UUID uuid, WorldGroup group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomeCountFromPlayer(connection, uuid, group);
    }

    public void saveHomeToDatabase(UUID player, Home home) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).saveHomeToDatabase(connection, player, home);
    }

    public HashMap<WorldGroup, Integer> getAllFreeHomes(UUID player) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(GroupInfosTable.class).getAllFreeHomes(connection, player);
    }

    public int getFreeHomes(UUID player, String group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(GroupInfosTable.class).getAllFreeHomes(connection, player, group);
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

    public void deleteHomesFromTheDatabase(UUID player, String homeName, WorldGroup group) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).removeHomeFromDatabase(connection, player, homeName, group);
    }

    public void bulkAddHomeFromMigration(List<HomeMigrator.HomeData> data) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(HomesTable.class).bulkAddHomeFromMigration(connection, data);
    }

    public int cleanupHomes(List<World> worlds) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).cleanupHomes(connection, worlds);
    }

    public int cleanupHomesGetAmount(List<World> worlds) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).cleanupHomesGetAmount(connection, worlds);
    }

    public int purgeHomesInWorld(World world) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).purgeHomesInWorld(connection, world);
    }

    public int purgeHomesInWorldGetAmount(World world) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).purgeHomesInWorldGetAmount(connection, world);
    }

    public List<Home> getHomesInRadius(Location center, int radius) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomesInRadius(connection, center, radius);
    }

    public List<Home> getHomesThatDontMatchRegex(String regex) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(HomesTable.class).getHomesThatDontMatchRegex(connection, regex);
    }

    public int getVersion() {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        return database.getTable(VersionTable.class).getVersion(connection);
    }

    public void setVersion(int version) {
        if (connection == null || database == null) throw new RuntimeException("Database Connection closed");
        database.getTable(VersionTable.class).setVersion(connection, version);
    }

    public void destroy() {
        database.close(connection);
        connection = null;
        database = null;
    }

    public Connection getConnection() {
        return connection;
    }
}
