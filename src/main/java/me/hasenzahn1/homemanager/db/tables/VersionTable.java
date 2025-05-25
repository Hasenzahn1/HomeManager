package me.hasenzahn1.homemanager.db.tables;

import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.system.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionTable extends Table {

    public VersionTable(Database database) {
        super("version", database);
    }

    @Override
    public String getCreationString() {
        return "CREATE TABLE IF NOT EXISTS " + getTableName() + "(" +
                "version INTEGER PRIMARY KEY);";
    }

    public int getVersion(Connection con) {
        try (PreparedStatement statement = con.prepareStatement("SELECT * FROM " + getTableName() + " ORDER BY version DESC LIMIT 1")) {
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt("version");
            }
        } catch (SQLException e) {
            Logger.ERROR.log("Error retrieving version from database");
            Logger.ERROR.logException(e);
        }
        return 0;
    }

    public void setVersion(Connection con, int version) {
        try (PreparedStatement statement = con.prepareStatement("INSERT OR IGNORE INTO " + getTableName() + "(version) VALUES(?)")) {
            statement.setInt(1, version);
            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.ERROR.log("Error setting version " + version + " to database");
            Logger.ERROR.logException(e);
        }
    }
}
