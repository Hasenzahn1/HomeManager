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
                "worldgroup VARCHAR(30) NOT NULL," +
                "PRIMARY KEY (uuid, name, worldgroup)" +
                ");";
    }

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

    public HashMap<WorldGroup, List<String>> getAllHomeNamesFromPlayer(Connection con, UUID uuid) {
        HashMap<WorldGroup, List<String>> map = new HashMap<>();
        try (PreparedStatement statement = con.prepareStatement("SELECT worldgroup, name FROM " + getTableName() + " WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String worldGroupName = result.getString("worldgroup");
                String name = result.getString("name");

                WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(worldGroupName);
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


    public PlayerHomes getHomesFromPlayer(Connection con, UUID uuid, String group) {
        HashMap<String, Home> homes = new HashMap<>();
        try (PreparedStatement statement = con.prepareStatement("SELECT * FROM " + getTableName() + " WHERE uuid = ? AND worldgroup LIKE ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, group);

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

    public int getHomeCountFromPlayer(Connection con, UUID player, String group) {
        int count = 0;
        try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM homes WHERE uuid=? AND worldgroup LIKE ?")) {
            statement.setString(1, player.toString());
            statement.setString(2, group);

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

    public void saveHomeToDatabase(Connection con, UUID player, Home home) {
        try (PreparedStatement statement = con.prepareStatement("INSERT INTO " + getTableName() + " (uuid, name, world, x, y, z, yaw, pitch, worldgroup) VALUES(?,?,?,?,?,?,?,?,?)")) {
            statement.setString(1, player.toString());
            statement.setString(2, home.name());
            statement.setString(3, home.location().getWorld().getName());
            statement.setDouble(4, home.location().getX());
            statement.setDouble(5, home.location().getY());
            statement.setDouble(6, home.location().getZ());
            statement.setFloat(7, home.location().getYaw());
            statement.setFloat(8, home.location().getPitch());
            statement.setString(9, HomeManager.getInstance().getWorldGroupManager().getWorldGroup(home.location().getWorld()).getName());

            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.ERROR.log("Error saving home to database for player " + player + " with name " + home.name() + " at " + home.location());
            Logger.ERROR.logException(e);
        }
    }

    public void removeHomeFromDatabase(Connection con, UUID uuid, String name, String group) {
        try (PreparedStatement statement = con.prepareStatement("DELETE FROM " + getTableName() + " WHERE uuid=? AND name LIKE ? AND worldgroup LIKE ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setString(3, group);

            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.ERROR.log("Error deleting home from database for player " + uuid + " with name " + name + " in group " + group);
            Logger.ERROR.logException(e);
        }
    }

    public void bulkAddHomeFromMigration(Connection con, List<HomeMigrator.HomeData> data) {
        try (PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + getTableName() + " (uuid, name, world, x, y, z, yaw, pitch, worldgroup) VALUES(?,?,?,?,?,?,?,?,?)")) {
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
                statement.setString(9, homeData.getWorldGroup().getName());

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
