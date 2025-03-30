package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import me.hasenzahn1.homemanager.migration.BasicHomesMigrator;
import me.hasenzahn1.homemanager.migration.PluginMigrator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;

public class MigrateSubCommand implements ISubCommand {
    @Override
    public void onCommand(Player executor, String[] args) {
        if (args.length == 0) { //
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate <all/world> ");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "all":
                Duration durationAll = PluginMigrator.startMigrationAll(new BasicHomesMigrator());
                MessageManager.sendMessage(executor, Language.HOME_ADMIN_MIGRATE_SUCCESS, "millis", String.valueOf(durationAll.toMillis()));
                break;
            case "world":
                if (args.length == 1) {
                    MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>");
                    return;
                }

                String worldName = args[1];
                Duration durationWorld = PluginMigrator.startMigrationWorld(new BasicHomesMigrator(), worldName);
                MessageManager.sendMessage(executor, Language.HOME_ADMIN_MIGRATE_SUCCESS, "millis", String.valueOf(durationWorld.toMillis()));
                break;
            default:
                MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender executor, String[] args) {
        return List.of();
    }

    @Override
    public String getName() {
        return "migrate";
    }
}
