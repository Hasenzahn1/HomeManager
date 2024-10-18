package me.hasenzahn1.homemanager.migration;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.group.WorldGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public static void startMigrationAll(Player player, PluginMigrator migrator) {
        Instant start = Instant.now();
        migrator.migrateAll();
        Instant end = Instant.now();
        player.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.HOME_ADMIN_MIGRATE_SUCCESS, "millis", String.valueOf(Duration.between(start, end).toMillis()))));
    }

    public static void startMigrationWorld(Player player, PluginMigrator migrator, String world) {
        Instant start = Instant.now();
        migrator.migrate(world);
        Instant end = Instant.now();
        player.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.HOME_ADMIN_MIGRATE_SUCCESS, "millis", String.valueOf(Duration.between(start, end).toMillis()))));
    }
}
