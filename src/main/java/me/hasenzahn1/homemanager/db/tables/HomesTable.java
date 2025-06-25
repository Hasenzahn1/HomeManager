package me.hasenzahn1.homemanager.db.tables;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.system.Table;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.migration.HomeMigrator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HomesTable extends Table {

    public HomesTable(Database database) {
        super("homes", database);
    }


    @Override
    public String getCreationString() {
        return "CREATE TABLE IF NOT EXISTS homes(" +
                "uuid VARCHAR(36) NOT NULL, " +
                "name VARCHAR(30) NOT NULL," +
                "world VARCHAR(30) NOT NULL," +
                "x REAL NOT NULL," +
                "y REAL NOT NULL," +
                "z REAL NOT NULL," +
                "yaw REAL NOT NULL," +
                "pitch REAL NOT NULL," +
                "PRIMARY KEY (uuid, name, world)" +
                ");";
    }

    /**
     * Retrieves all homes located within a specified horizontal radius around a given center point.
     * The Y-coordinate is ignored; the search is performed in a 2D circle on the XZ-plane.
     *
     * @param con    The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param center The center point of the search area (Y-coordinate is ignored).
     * @param radius The radius of the circular search area.
     * @return A list of all homes found within the specified radius on the XZ-plane.
     */
    public List<Home> getHomesInRadius(Connection con, Location center, int radius) {
        List<Home> homes = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE world LIKE '%world%' AND (x%x%)*(x%x%)+(z%z%)*(z%z%)<%radius%*%radius%"
                .replace("%world%", center.getWorld().getName())
                .replace("%x%", String.format("%+d", -center.getBlockX()))
                .replace("%z%", String.format("%+d", -center.getBlockZ()))
                .replace("%radius%", String.valueOf(radius));

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("uuid"));
                String name = result.getString("name");
                String world = result.getString("world");
                double x = result.getDouble("x");
                double y = result.getDouble("y");
                double z = result.getDouble("z");
                float yaw = result.getFloat("yaw");
                float pitch = result.getFloat("pitch");

                Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

                homes.add(new Home(uuid, name, location));
            }
        } catch (SQLException e) {
            Logger.ERROR.log("Error retrieving homes from database at location " + center + " with radius " + radius);
            Logger.ERROR.logException(e);
        }
        return homes;
    }

    /**
     * Deletes all homes stored in the database that are located in the specified world.
     *
     * @param con   The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param world The world whose homes should be removed from the database.
     * @return The number of homes deleted. Returns 0 if an error occurs during the operation.
     */
    public int purgeHomesInWorld(Connection con, World world) {
        try (PreparedStatement statement = con.prepareStatement("DELETE FROM " + getTableName() + " WHERE world LIKE ?")) {
            statement.setString(1, world.getName());
            return statement.executeUpdate();
        } catch (SQLException e) {
            Logger.ERROR.log("Error purging homes from database in world " + world.getName());
            Logger.ERROR.logException(e);
        }
        return 0;
    }

    /**
     * Deletes all homes from the database that are not located in the specified list of worlds.
     *
     * @param con    The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param worlds A list of worlds to keep; homes in other worlds will be removed.
     * @return The number of homes deleted. Returns 0 if an error occurs during the operation.
     */
    public int cleanupHomes(Connection con, List<World> worlds) {
        StringBuilder sql = new StringBuilder("DELETE FROM " + getTableName() + " WHERE");
        for (World world : worlds) {
            sql.append(" world NOT LIKE '").append(world.getName()).append("'").append(" AND");
        }
        sql.delete(sql.length() - 4, sql.length());
        try (PreparedStatement statement = con.prepareStatement(sql.toString())) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            Logger.ERROR.log("Error cleaning homes from database with worlds " + String.join(", ", worlds.stream().map(World::getName).toList()));
            Logger.ERROR.logException(e);
        }

        return 0;
    }

    /**
     * Retrieves all home names of a player, grouped by their corresponding world group.
     *
     * @param con  The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param uuid The UUID of the player whose homes should be retrieved.
     * @return A map where each key is a {@link WorldGroup} and the value is a list of home names
     * the player owns in that group. Returns an empty map if no homes are found or an error occurs.
     */
    public HashMap<WorldGroup, List<String>> getAllHomeNamesFromPlayer(Connection con, UUID uuid) {
        HashMap<WorldGroup, List<String>> map = new HashMap<>();
        try (PreparedStatement statement = con.prepareStatement("SELECT world, name FROM " + getTableName() + " WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                World world = Bukkit.getWorld(result.getString("world"));
                String name = result.getString("name");

                WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(world);
                if (worldGroup == null) continue;

                if (!map.containsKey(worldGroup)) map.put(worldGroup, new ArrayList<>());
                map.get(worldGroup).add(name);
            }

        } catch (SQLException e) {
            Logger.ERROR.log("Error getting all homes from database for player " + uuid);
            Logger.ERROR.logException(e);
        }
        return map;
    }


    /**
     * Retrieves all homes from a player in a specific worldgroup
     *
     * @param con   The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param uuid  The UUID of the player whose homes should be retrieved.
     * @param group The worldgroup from which the homes should be retrieved
     * @return A {@link PlayerHomes} object with all the homes from the worldgroup
     */
    public PlayerHomes getHomesFromPlayer(Connection con, UUID uuid, WorldGroup group) {
        String placeHolders = group.getWorlds().stream().map(World::getName).map(n -> "?").collect(Collectors.joining(","));
        HashMap<String, Home> homes = new HashMap<>();
        try (PreparedStatement statement = con.prepareStatement("SELECT * FROM " + getTableName() + " WHERE uuid = ? AND world IN (" + placeHolders + ")")) {
            statement.setString(1, uuid.toString());
            int index = 2;
            for (World w : group.getWorlds()) {
                statement.setString(index, w.getName());
                index++;
            }

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID homeUUID = UUID.fromString(result.getString("uuid"));
                String name = result.getString("name");
                String world = result.getString("world");
                double x = result.getDouble("x");
                double y = result.getDouble("y");
                double z = result.getDouble("z");
                float yaw = result.getFloat("yaw");
                float pitch = result.getFloat("pitch");

                Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

                homes.put(name.toLowerCase(), new Home(homeUUID, name, location));
            }
        } catch (SQLException e) {
            Logger.ERROR.log("Error retrieving homes from database for player " + uuid + " in group " + group);
            Logger.ERROR.logException(e);
        }
        return new PlayerHomes(homes);
    }

    /**
     * Retrieves the home count from a player in a specified worldgroup
     *
     * @param con    The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param player The UUID of the player whose home count should be retrieved.
     * @param group  The worldgroup from which the home count should be retrieved
     * @return The number of homes the player has in the respective worldgroup
     */
    public int getHomeCountFromPlayer(Connection con, UUID player, WorldGroup group) {
        String placeHolders = group.getWorlds().stream().map(World::getName).map(n -> "?").collect(Collectors.joining(","));
        int count = 0;
        try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM homes WHERE uuid=? AND world IN (" + placeHolders + ")")) {
            statement.setString(1, player.toString());
            int index = 2;
            for (World w : group.getWorlds()) {
                statement.setString(index, w.getName());
                index++;
            }

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                count = set.getInt(1);
            }

        } catch (SQLException e) {
            Logger.ERROR.log("Error retrieving home count for player " + player + " in group " + group);
            Logger.ERROR.logException(e);
        }
        return count;
    }

    /**
     * Saves a players home to the database
     *
     * @param con    The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param player The UUID of the player whose home should be saved.
     * @param home   The home to save to the database
     */
    public void saveHomeToDatabase(Connection con, UUID player, Home home) {
        try (PreparedStatement statement = con.prepareStatement("INSERT INTO " + getTableName() + " (uuid, name, world, x, y, z, yaw, pitch) VALUES(?,?,?,?,?,?,?,?)")) {
            statement.setString(1, player.toString());
            statement.setString(2, home.name());
            statement.setString(3, home.location().getWorld().getName());
            statement.setDouble(4, home.location().getX());
            statement.setDouble(5, home.location().getY());
            statement.setDouble(6, home.location().getZ());
            statement.setFloat(7, home.location().getYaw());
            statement.setFloat(8, home.location().getPitch());

            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.ERROR.log("Error saving home to database for player " + player + " with name " + home.name() + " at " + home.location());
            Logger.ERROR.logException(e);
        }
    }

    /**
     * Removes a home of a player in a specific world group by its name.
     *
     * @param con   The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param uuid  The UUID of the player whose home should be removed.
     * @param name  The name of the home to be removed.
     * @param group The world group in which the home is located.
     */
    public void removeHomeFromDatabase(Connection con, UUID uuid, String name, WorldGroup group) {
        String placeHolders = group.getWorlds().stream().map(World::getName).map(n -> "?").collect(Collectors.joining(","));
        try (PreparedStatement statement = con.prepareStatement("DELETE FROM " + getTableName() + " WHERE uuid=? AND name LIKE ? AND world IN (" + placeHolders + ")")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            int index = 3;
            for (World w : group.getWorlds()) {
                statement.setString(index, w.getName());
                index++;
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.ERROR.log("Error deleting home from database for player " + uuid + " with name " + name + " in group " + group);
            Logger.ERROR.logException(e);
        }
    }

    /**
     * Adds a large number of homes, gathered from the migration system, to the database in bulk.
     *
     * @param con  The database connection, provided by {@link me.hasenzahn1.homemanager.db.DatabaseAccessor}.
     * @param data A list of {@link me.hasenzahn1.homemanager.migration.HomeMigrator.HomeData} objects representing the homes to be added.
     */
    public void bulkAddHomeFromMigration(Connection con, List<HomeMigrator.HomeData> data) {
        try (PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + getTableName() + " (uuid, name, world, x, y, z, yaw, pitch) VALUES(?,?,?,?,?,?,?,?)")) {
            con.setAutoCommit(false);
            for (HomeMigrator.HomeData homeData : data) {
                statement.setString(1, homeData.uuid().toString());
                statement.setString(2, homeData.name());
                statement.setString(3, homeData.world());
                statement.setDouble(4, homeData.x());
                statement.setDouble(5, homeData.y());
                statement.setDouble(6, homeData.z());
                statement.setFloat(7, homeData.yaw());
                statement.setFloat(8, homeData.pitch());

                statement.executeUpdate();
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            Logger.ERROR.log("Error bulk adding " + data.size() + " homes.");
            Logger.ERROR.logException(e);
        }
    }
}
