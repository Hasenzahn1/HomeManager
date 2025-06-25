package me.hasenzahn1.homemanager.updates;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class responsible for upgrading the plugin's database schema based on the version.
 * It applies patches for different versions and handles auto-upgrades when the plugin is outdated.
 */
public class VersionUpgrader {

    private DatabaseAccessor session;
    private int currentVersion;

    /**
     * Constructor initializes the session and retrieves the current version from the database.
     */
    public VersionUpgrader() {
        session = DatabaseAccessor.openSession();
        currentVersion = session.getVersion();
    }

    /**
     * Starts the upgrade process by comparing the current version with the plugin's version.
     * If the current version is outdated, it proceeds with applying upgrades.
     */
    public void startUpgrade() {
        // If the current version is up-to-date, notify and stop
        if (currentVersion >= HomeManager.PLUGIN_VERSION) {
            Logger.SUCCESS.log("Plugin Metadata is on the newest version (" + currentVersion + ").");
            return;
        }

        // If outdated, notify and proceed with upgrade
        Logger.INFO.log("Outdated Plugin Metadata (" + currentVersion + " / " + HomeManager.PLUGIN_VERSION + "). Starting auto upgrade");
        upgrade();
    }

    /**
     * Recursively applies version-based patches to the plugin's database schema.
     * It increments the current version after each successful patch application.
     */
    private void upgrade() {
        boolean valid = false;

        // Apply patches for version 0
        if (currentVersion == 0) {
            valid = applyVersion0Patches();
        }

        // Check if the patch was successful
        if (!valid) {
            Logger.ERROR.log("Error Applying patches to plugin metadata. The plugin is in an unsafe state and will disable itself.");
            Bukkit.getPluginManager().disablePlugin(HomeManager.getInstance());
            return;
        }
        currentVersion++;  // Increment version after successful patch
        session.setVersion(currentVersion);  // Update version in the database
        session = DatabaseAccessor.openSession();  // Reopen session to reflect new version

        // If the current version is still behind, continue upgrading
        if (currentVersion < HomeManager.PLUGIN_VERSION) upgrade();
    }

    /**
     * Applies the patch for version 0 by removing outdated columns and restructuring the homes table.
     *
     * @return true if the patch was successfully applied, false if an error occurred.
     */
    private boolean applyVersion0Patches() {
        Logger.INFO.log("Applying patches for version 0");
        Logger.DEBUG.log("Removing WorldGroup from homes table");

        try {
            session.getConnection().setAutoCommit(false);
            try (Statement stmt = session.getConnection().createStatement()) {
                // Rename the old homes table to keep backup of the previous schema
                stmt.execute("ALTER TABLE homes RENAME TO homes_old");

                // Create a new homes table with the updated primary key and structure
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

                // Copy data from the old table to the new table
                stmt.execute("""
                        INSERT INTO homes (uuid, name, world, x, y, z, yaw, pitch)
                        SELECT uuid, name, world, x, y, z, yaw, pitch FROM homes_old;
                        """);

                // Drop the old table after data migration
                stmt.execute("DROP TABLE homes_old");
                session.getConnection().commit();  // Commit the changes
                Logger.SUCCESS.log("Version 0 patches applied.");
            } catch (SQLException e) {
                session.getConnection().rollback();  // Rollback if an error occurs
                Logger.ERROR.logException(e);
                Logger.ERROR.log("Error removing worldgroup column from homes table. To fix this error open the database file, ");
                Logger.ERROR.log("Navigate to the homes table and check if the column exists. If not increase the version in the version table.");
                return false;
            } finally {
                session.getConnection().setAutoCommit(true);  // Restore auto-commit
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
