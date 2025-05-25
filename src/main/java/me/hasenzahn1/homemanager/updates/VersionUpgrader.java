package me.hasenzahn1.homemanager.updates;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.sql.Statement;

public class VersionUpgrader {

    private DatabaseAccessor session;
    private int currentVersion;

    public VersionUpgrader() {
        session = DatabaseAccessor.openSession();

        currentVersion = session.getVersion();
    }

    public void startUpgrade() {
        if (currentVersion >= HomeManager.PLUGIN_VERSION) {
            Logger.SUCCESS.log("Plugin Metadata is on the newest version (" + currentVersion + ").");
            return;
        }

        Logger.INFO.log("Outdated Plugin Metadata (" + currentVersion + " / " + HomeManager.PLUGIN_VERSION + ")" + ". Starting auto upgrade");
        upgrade();
    }

    private void upgrade() {
        boolean valid = false;

        //Apply Patches
        if (currentVersion == 0) {
            valid = applyVersion0Patches();
        }

        //Check patch Success
        if (!valid) {
            Logger.ERROR.log("Error Applying patches to plugin metadata. The plugin is in an unsafe state and will disable itself.");
            Bukkit.getPluginManager().disablePlugin(HomeManager.getInstance());
            return;
        }
        currentVersion++;
        session.setVersion(currentVersion);
        session = DatabaseAccessor.openSession();
        if (currentVersion < HomeManager.PLUGIN_VERSION) upgrade();
    }

    private boolean applyVersion0Patches() {
        Logger.INFO.log("Applying patches for version 0");
        Logger.DEBUG.log("Removing WorldGroup from homes table");

        try {
            session.getConnection().setAutoCommit(false);
            try (Statement stmt = session.getConnection().createStatement()) {
                // 2. Rename old table
                stmt.execute("ALTER TABLE homes RENAME TO homes_old");

                // 3. Create new table with updated primary key
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS homes(
                            uuid VARCHAR(36) NOT NULL,
                            name VARCHAR(30) NOT NULL,
                            world VARCHAR(30) NOT NULL,
                            x REAL NOT NULL,
                            y REAL NOT NULL,
                            z REAL NOT NULL,
                            yaw REAL NOT NULL,
                            pitch REAL NOT NULL,
                            PRIMARY KEY (uuid, name, world)
                        );
                        """);

                // 4. Copy data
                stmt.execute("""
                        INSERT INTO homes (uuid, name, world, x, y, z, yaw, pitch)
                        SELECT uuid, name, world, x, y, z, yaw, pitch FROM homes_old;
                        """);

                // 5. Drop old table
                stmt.execute("DROP TABLE homes_old");
                session.getConnection().commit();
                Logger.SUCCESS.log("Version 0 patches applied.");
            } catch (SQLException e) {
                session.getConnection().rollback();
                Logger.ERROR.logException(e);
                Logger.ERROR.log("Error removing worldgroup column from homes table. To fix this error open the database file, ");
                Logger.ERROR.log("Navigate to the homes table and check if the column exists. If not increase the version in the version table.");
                return false;
            } finally {
                session.getConnection().setAutoCommit(true);
            }
        } catch (SQLException e) {
            Logger.ERROR.logException(e);
            Logger.ERROR.log("Error removing worldgroup column from homes table. To fix this error open the database file, ");
            Logger.ERROR.log("Navigate to the homes table and check if the column exists. If not increase the version in the version table.");
            return false;
        }
        return true;
    }
}
