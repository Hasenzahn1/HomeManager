package me.hasenzahn1.homemanager.db.tables;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.system.Table;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.migration.PluginMigrator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public int purgeHomesInWorld(Connection con, World world) {
        try (PreparedStatement statement = con.prepareStatement("DELETE FROM " + getTableName() + " WHERE world LIKE '" + world.getName() + "'")) {
            int count = statement.executeUpdate();
            Logger.DEBUG.log("Successfully purged " + count + " homes from database from world " + world.getName());

            return count;
        } catch (SQLException e) {
            Logger.ERROR.log("Error purging homes from database in world " + world.getName());
            Logger.ERROR.log(e.getMessage());
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
            int rowCount = statement.executeUpdate();
            Logger.DEBUG.log("Successfully cleaned " + rowCount + " homes from database not in worlds " + String.join(", ", worlds.stream().map(World::getName).toList()));

            return rowCount;
        } catch (SQLException e) {
            Logger.ERROR.log("Error cleaning homes from database with worlds " + String.join(", ", worlds.stream().map(World::getName).toList()));
            Logger.ERROR.log(e.getMessage());
        }

        return 0;
    }

    public PlayerHomes getHomesFromPlayer(Connection con, UUID uuid, String group) {
        HashMap<String, Home> homes = new HashMap<>();
        try (PreparedStatement statement = con.prepareStatement("SELECT * FROM " + getTableName() + " WHERE uuid = '" + uuid + "' AND worldgroup LIKE '" + group + "'")) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String name = result.getString("name");
                String world = result.getString("world");
                double x = result.getDouble("x");
                double y = result.getDouble("y");
                double z = result.getDouble("z");
                float yaw = result.getFloat("yaw");
                float pitch = result.getFloat("pitch");

                Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

                homes.put(name.toLowerCase(), new Home(name, location));
            }
        } catch (SQLException e) {
            Logger.ERROR.log("Error retrieving homes from database for player " + uuid + " in group " + group);
            Logger.ERROR.log(e.getMessage());
        }
        return new PlayerHomes(homes);
    }

    public int getHomeCountFromPlayer(Connection con, UUID player, String group) {
        int count = 0;
        try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM homes WHERE uuid='" + player + "' AND worldgroup LIKE '" + group + "'")) {

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                count = set.getInt(1);
            }

        } catch (SQLException e) {
            Logger.ERROR.log("Error retrieving home count for player " + player + " in group " + group);
            Logger.ERROR.log(e.getMessage());
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
            Logger.DEBUG.log("Added home of player " + player + " to the database with name " + home.name());

        } catch (SQLException e) {
            Logger.ERROR.log("Error saving home to database for player " + player + " with name " + home.name() + " at " + home.location());
            Logger.ERROR.log(e.getMessage());
        }
    }

    public void removeHomeFromDatabase(Connection con, UUID player, String name, String group) {
        try (PreparedStatement statement = con.prepareStatement("DELETE FROM " + getTableName() + " WHERE uuid='" + player + "' AND name LIKE '" + name + "' AND worldgroup LIKE '" + group + "'")) {
            statement.executeUpdate();
            Logger.DEBUG.log("Successfully deleted home of player " + player + " to the database with name " + name + " in group " + group);
        } catch (SQLException e) {
            Logger.ERROR.log("Error deleting home from database for player " + player + " with name " + name + " in group " + group);
            Logger.ERROR.log(e.getMessage());
        }
    }

    public void bulkAddHomeFromMigration(Connection con, List<PluginMigrator.HomeData> data) {
        try (PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + getTableName() + " (uuid, name, world, x, y, z, yaw, pitch, worldgroup) VALUES(?,?,?,?,?,?,?,?,?)")) {
            con.setAutoCommit(false);
            for (PluginMigrator.HomeData homeData : data) {
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
            throw new RuntimeException(e);
        }
    }
}
