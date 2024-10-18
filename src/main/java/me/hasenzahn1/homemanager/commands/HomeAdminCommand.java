package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.migration.BasicHomesMigrator;
import me.hasenzahn1.homemanager.migration.PluginMigrator;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class HomeAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        if (!commandSender.hasPermission("homemanager.commands.homeadmin")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        Player executor = (Player) commandSender;
        if (args.length == 0) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/homeadmin <migrate>")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "migrate":
                handleMigrateCommand(executor, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/homeadmin <migrate>")));
        }

        return true;
    }

    public void handleMigrateCommand(Player executor, String[] args) {
        if (args.length == 0) { //
            executor.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/homeadmin migrate <all/world>")));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "all":
                PluginMigrator.startMigrationAll(executor, new BasicHomesMigrator());
                break;
            case "world":
                if (args.length == 1) {
                    executor.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>")));
                    return;
                }

                String worldName = args[1];
                PluginMigrator.startMigrationWorld(executor, new BasicHomesMigrator(), worldName);
                break;
            default:
                executor.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/homeadmin migrate world <world>")));
        }
    }

}
