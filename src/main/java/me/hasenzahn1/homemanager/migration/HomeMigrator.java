package me.hasenzahn1.homemanager.migration;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Handles the registration and execution of home data migrations from various external formats.
 */
public class HomeMigrator {

    private final HashMap<String, BaseHomeMigrator> migrators;

    
    public HomeMigrator() {
        migrators = new HashMap<>();
    }

    /**
     * Registers a {@link BaseHomeMigrator} implementation for later use.
     *
     * @param migrator The migrator to register.
     */
    public void registerMigrator(BaseHomeMigrator migrator) {
        migrators.put(migrator.getName(), migrator);
    }

    /**
     * Gets a list of all registered migrator names.
     *
     * @return A list of migrator names.
     */
    public List<String> getNames() {
        return migrators.keySet().stream().toList();
    }

    /**
     * Retrieves a registered migrator by its name.
     *
     * @param name The name of the migrator.
     * @return The matching {@link BaseHomeMigrator}, or null if none was registered under this name.
     */
    public BaseHomeMigrator getMigrator(String name) {
        return migrators.get(name);
    }

    /**
     * Performs a migration of all homes using the given migrator.
     *
     * @param migrator The migrator to execute.
     * @return The time duration the migration took.
     */
    public Duration startMigrationAll(BaseHomeMigrator migrator) {
        Instant start = Instant.now();

        List<HomeData> homes = migrator.migrateAll();
        DatabaseAccessor session = DatabaseAccessor.openSession();
        session.bulkAddHomeFromMigration(homes);
        session.destroy();

        Instant end = Instant.now();
        HomeManager.getInstance().getHomesCache().invalidateAll(); // clear any stale cache
        return Duration.between(start, end);
    }

    /**
     * Performs a migration of homes in a specific world using the given migrator.
     *
     * @param migrator The migrator to execute.
     * @param world    The name of the world to filter by.
     * @return The time duration the migration took.
     */
    public Duration startMigrationWorld(BaseHomeMigrator migrator, String world) {
        Instant start = Instant.now();

        List<HomeData> homes = migrator.migrate(world);
        DatabaseAccessor session = DatabaseAccessor.openSession();
        session.bulkAddHomeFromMigration(homes);
        session.destroy();

        Instant end = Instant.now();
        HomeManager.getInstance().getHomesCache().invalidateAll();
        return Duration.between(start, end);
    }

    /**
     * Represents a single home entry used for migration.
     *
     * @param uuid  The UUID of the player.
     * @param name  The name of the home.
     * @param world The world the home is located in.
     * @param x     X-coordinate.
     * @param y     Y-coordinate.
     * @param z     Z-coordinate.
     * @param yaw   Player's yaw at the home location.
     * @param pitch Player's pitch at the home location.
     */
    public record HomeData(UUID uuid, String name, String world, double x, double y, double z, float yaw, float pitch) {

        /**
         * Gets the {@link WorldGroup} this home belongs to, based on the world.
         *
         * @return The associated {@link WorldGroup}.
         */
        public WorldGroup getWorldGroup() {
            return HomeManager.getInstance().getWorldGroupManager().getWorldGroup(Bukkit.getWorld(world));
        }
    }
}
