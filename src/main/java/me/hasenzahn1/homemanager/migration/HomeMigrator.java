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

public class HomeMigrator {

    private final HashMap<String, BaseHomeMigrator> migrators;

    public HomeMigrator() {
        migrators = new HashMap<>();
    }

    public void registerMigrator(BaseHomeMigrator migrator) {
        migrators.put(migrator.getName(), migrator);
    }

    public List<String> getNames() {
        return migrators.keySet().stream().toList();
    }

    public BaseHomeMigrator getMigrator(String name) {
        return migrators.get(name);
    }

    public Duration startMigrationAll(BaseHomeMigrator migrator) {
        Instant start = Instant.now();

        List<HomeData> homes = migrator.migrateAll();
        DatabaseAccessor session = DatabaseAccessor.openSession();
        session.bulkAddHomeFromMigration(homes);
        session.destroy();

        Instant end = Instant.now();
        HomeManager.getInstance().getHomesCache().invalidateAll();
        return Duration.between(start, end);
    }

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

    public record HomeData(UUID uuid, String name, String world, double x, double y, double z, float yaw, float pitch) {
        public WorldGroup getWorldGroup() {
            return HomeManager.getInstance().getWorldGroupManager().getWorldGroup(Bukkit.getWorld(world));
        }
    }
}
