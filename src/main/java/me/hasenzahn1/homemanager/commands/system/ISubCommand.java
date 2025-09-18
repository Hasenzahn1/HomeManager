package me.hasenzahn1.homemanager.commands.system;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ISubCommand {

    void onCommand(CommandSender executor, String[] args);

    List<String> onTabComplete(CommandSender executor, String[] args);

    String getName();

}
