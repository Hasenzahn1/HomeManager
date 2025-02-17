package me.hasenzahn1.homemanager.db.tables;

import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.system.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GroupInfosTable extends Table {

    public GroupInfosTable(Database database) {
        super("playergroupinfo", database);
    }

    @Override
    public String getCreationString() {
        return "CREATE TABLE IF NOT EXISTS " + getTableName() + " (" +
                "uuid VARCHAR(36) NOT NULL," +
                "worldgroup VARCHAR(30) NOT NULL," +
                "freehomes INTEGER NOT NULL," +
                "PRIMARY KEY (uuid, worldgroup));";
    }

    public void saveFreeHomes(Connection con, UUID uuid, String group, int maxHomes) {
        try (PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + getTableName() + " (uuid, worldgroup, freehomes) VALUES (?, ?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, group);
            statement.setInt(3, maxHomes);

            statement.executeUpdate();
            Logger.DEBUG.log("Successfully saved free home to database for player " + uuid + " in group " + group + " with homes " + maxHomes);
        } catch (SQLException e) {
            Logger.ERROR.log("Error saving free homes to database for player " + uuid + " in group " + group + " with homes " + maxHomes);
            Logger.ERROR.log(e.getMessage());
        }
    }

    public int getFreeHomes(Connection con, UUID uuid, String group) {
        try (PreparedStatement statement = con.prepareStatement("SELECT freehomes FROM " + getTableName() + " WHERE uuid='" + uuid + "' AND worldgroup LIKE '" + group + "'")) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.ERROR.log("Error retrieving free homes from database for player " + uuid + " in group " + group);
            Logger.ERROR.log(e.getMessage());
        }
        return 0;
    }

    public void incrementFreeHomes(Connection con, UUID uuid, String group) {
        try (PreparedStatement statement = con.prepareStatement("UPDATE " + getTableName() + " SET freehomes = freehomes + 1 WHERE uuid='" + uuid + "' AND worldgroup LIKE '" + group + "'")) {
            statement.executeUpdate();
            Logger.DEBUG.log("Successfully incremented free homes for player " + uuid + " in group " + group);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void decrementFreeHomes(Connection con, UUID uuid, String group) {
        try (PreparedStatement statement = con.prepareStatement("UPDATE " + getTableName() + " SET freehomes = freehomes - 1 WHERE uuid='" + uuid + "' AND worldgroup LIKE '" + group + "' AND freehomes > 0")) {
            statement.executeUpdate();
            Logger.DEBUG.log("Successfully decremented free homes for player " + uuid + " in group " + group);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
