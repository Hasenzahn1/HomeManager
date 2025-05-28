package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import me.hasenzahn1.homemanager.migration.BaseHomeMigrator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

public class MigrateSubCommand implements ISubCommand {
    @Override
    public void onCommand(Player executor, String[] args) {
        if (args.length <= 1) { //
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate <migrator> <all/world> (world)");
            return;
        }

        String migratorName = args[0];
        BaseHomeMigrator migrator = HomeManager.getInstance().getHomeMigrator().getMigrator(migratorName);
        if (migrator == null) {
            MessageManager.sendMessage(executor, Language.HOME_ADMIN_MIGRATE_SUCCESS, "migrator", migratorName);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "all":
                Duration durationAll = HomeManager.getInstance().getHomeMigrator().startMigrationAll(migrator);
                HomeManager.getInstance().getHomesCache().invalidateAll();
                MessageManager.sendMessage(executor, Language.HOME_ADMIN_MIGRATE_SUCCESS, "millis", String.valueOf(durationAll.toMillis()));
                break;
            case "world":
                if (args.length == 2) {
                    MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>");
                    return;
                }

                String worldName = args[1];
                Duration durationWorld = HomeManager.getInstance().getHomeMigrator().startMigrationWorld(migrator, worldName);
                HomeManager.getInstance().getHomesCache().invalidateAll();
                MessageManager.sendMessage(executor, Language.HOME_ADMIN_MIGRATE_SUCCESS, "millis", String.valueOf(durationWorld.toMillis()));
                break;
            default:
                MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender executor, String[] args) {
        if (args.length == 0) return List.of();
        List<String> arg1Possibilities = HomeManager.getInstance().getHomeMigrator().getNames().stream().filter(f -> f.toLowerCase().startsWith(args[0].toLowerCase())).toList();

        if (args.length == 1) {
            return arg1Possibilities;
        }

        if (arg1Possibilities.isEmpty()) return List.of();

        List<String> arg2Possibilities = Stream.of("all", "world").filter(f -> f.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        if (args.length == 2) {
            return arg2Possibilities;
        }

        if (arg2Possibilities.isEmpty()) return List.of();
        if (!arg2Possibilities.get(0).equalsIgnoreCase("world")) return List.of();

        if (args.length == 3) {
            return Bukkit.getWorlds().stream().map(World::getName).filter(f -> f.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        }

        return List.of();
    }

    @Override
    public String getName() {
        return "migrate";
    }
}
