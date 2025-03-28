package me.hasenzahn1.homemanager.migration;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public abstract class PluginMigrator {

    public abstract void migrateAll();

    public abstract void migrate(String world);


    public record HomeData(UUID uuid, String name, String world, double x, double y, double z, float yaw, float pitch) {

        public WorldGroup getWorldGroup() {
            return HomeManager.getInstance().getWorldGroupManager().getWorldGroup(Bukkit.getWorld(world));
        }
    }

    public static Duration startMigrationAll(PluginMigrator migrator) {
        Instant start = Instant.now();
        migrator.migrateAll();
        Instant end = Instant.now();
        return Duration.between(start, end);
    }

    public static Duration startMigrationWorld(PluginMigrator migrator, String world) {
        Instant start = Instant.now();
        migrator.migrate(world);
        Instant end = Instant.now();
        return Duration.between(start, end);
    }
}
