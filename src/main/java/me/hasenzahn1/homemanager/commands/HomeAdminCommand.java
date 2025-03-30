package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.homeadmin.*;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeAdminCommand implements CommandExecutor, TabCompleter {

    private final List<ISubCommand> subCommands;

    public HomeAdminCommand() {
        subCommands = new ArrayList<>();

        subCommands.add(new CleanupSubCommand());
        subCommands.add(new MigrateSubCommand());
        subCommands.add(new PurgeSubCommand());
        subCommands.add(new ReloadSubCommand());
        subCommands.add(new FreeHomesSubCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player executor)) {
            MessageManager.sendMessage(commandSender, Language.NO_PLAYER);
            return true;
        }

        if (!commandSender.hasPermission("homeadmin.commands.homeadmin")) {
            MessageManager.sendMessage(commandSender, Language.NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            MessageManager.sendMessage(commandSender, Language.INVALID_COMMAND, "command", "/homeadmin " + getSubCommandList());
            return true;
        }

        for (ISubCommand subCommand : subCommands) {
            if (!subCommand.getName().equalsIgnoreCase(args[0])) continue;

            if (!commandSender.hasPermission("homeadmin.commands.homeadmin." + subCommand.getName())) {
                MessageManager.sendMessage(commandSender, Language.NO_PERMISSION);
                return true;
            }

            subCommand.onCommand(executor, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream().map(ISubCommand::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).filter(name -> commandSender.hasPermission("homeadmin.commands.homeadmin." + name)).sorted().toList();
        }

        if (args.length >= 2) {
            for (ISubCommand subCommand : subCommands) {
                if (!subCommand.getName().equalsIgnoreCase(args[0])) continue;

                if (!commandSender.hasPermission("homeadmin.commands.homeadmin." + subCommand.getName())) {
                    return List.of();
                }

                return subCommand.onTabComplete(commandSender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return List.of();
    }

    private String getSubCommandList() {
        StringBuilder builder = new StringBuilder();
        builder.append("<").append(subCommands.get(0).getName());

        for (int i = 1; i < subCommands.size(); i++) {
            builder.append("/").append(subCommands.get(i).getName());
        }
        return builder.append(">").toString();
    }
}
