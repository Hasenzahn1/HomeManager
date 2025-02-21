package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.migration.BasicHomesMigrator;
import me.hasenzahn1.homemanager.migration.PluginMigrator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class HomeAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player executor)) {
            MessageManager.sendMessage(commandSender, Language.NO_PLAYER);
            return true;
        }

        if (!commandSender.hasPermission("homemanager.commands.homeadmin")) {
            MessageManager.sendMessage(commandSender, Language.NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            MessageManager.sendMessage(commandSender, Language.INVALID_COMMAND, "command", "/homeadmin <migrate>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "migrate":
                handleMigrateCommand(executor, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                MessageManager.sendMessage(commandSender, Language.INVALID_COMMAND, "command", "/homeadmin <migrate>");
        }

        return true;
    }

    public void handleMigrateCommand(Player executor, String[] args) {
        if (args.length == 0) { //
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate <all/world>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "all":
                PluginMigrator.startMigrationAll(executor, new BasicHomesMigrator());
                break;
            case "world":
                if (args.length == 1) {
                    MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>");
                    return;
                }

                String worldName = args[1];
                PluginMigrator.startMigrationWorld(executor, new BasicHomesMigrator(), worldName);
                break;
            default:
                MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>");
        }
    }

}
