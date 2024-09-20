package me.hasenzahn1.homemanager.db.tables;

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getFreeHomes(Connection con, UUID uuid, String group) {
        try (PreparedStatement statement = con.prepareStatement("SELECT freehomes FROM " + getTableName() + " WHERE uuid='" + uuid + "' AND worldgroup LIKE '" + group + "'")) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
