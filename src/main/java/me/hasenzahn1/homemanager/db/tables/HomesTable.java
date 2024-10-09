package me.hasenzahn1.homemanager.db.tables;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.system.Table;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class HomesTable extends Table {

    public HomesTable(Database database) {
        super("homes", database);
    }


    @Override
    public String getCreationString() {
        System.out.println("Create homes");
        return "CREATE TABLE IF NOT EXISTS homes(" +
                "uuid VARCHAR(36) NOT NULL, " +
                "name VARCHAR(30) NOT NULL," +
                "world VARCHAR(30) NOT NULL," +
                "x REAL NOT NULL," +
                "y REAL NOT NULL," +
                "z REAL NOT NULL," +
                "yaw REAL NOT NULL," +
                "pitch REAL NOT NULL," +
                "worldgroup VARCHAR(30) NOT NULL" +
                ");";
    }

    public HashMap<String, Location> getHomesFromPlayer(Connection con, UUID uuid, String group) {
        HashMap<String, Location> homes = new HashMap<>();
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

                homes.put(name, location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homes;
    }

    public int getHomeCountFromPlayer(Connection con, UUID player, String group) {
        int count = 0;
        try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM homes WHERE uuid='" + player + "' AND worldgroup LIKE '" + group + "'")) {

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                count = set.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public void saveHomeToDatabase(Connection con, UUID player, String name, Location location) {
        try (PreparedStatement statement = con.prepareStatement("INSERT INTO " + getTableName() + " (uuid, name, world, x, y, z, yaw, pitch, worldgroup) VALUES(?,?,?,?,?,?,?,?,?)")) {
            statement.setString(1, player.toString());
            statement.setString(2, name);
            statement.setString(3, location.getWorld().getName());
            statement.setDouble(4, location.getX());
            statement.setDouble(5, location.getY());
            statement.setDouble(6, location.getZ());
            statement.setFloat(7, location.getYaw());
            statement.setFloat(8, location.getPitch());
            statement.setString(9, HomeManager.getInstance().getWorldGroupManager().getWorldGroup(location.getWorld()).getName());

            statement.executeUpdate();
            Logger.DEBUG.log("Added home of player " + player + " to the database with name " + name);

        } catch (SQLException e) {
            Logger.ERROR.log("Error saving home to database for player " + player + " with name " + name + " at " + location);
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
}
