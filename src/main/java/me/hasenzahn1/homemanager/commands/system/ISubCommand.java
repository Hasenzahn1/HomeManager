package me.hasenzahn1.homemanager.commands.system;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface ISubCommand {

    void onCommand(Player executor, String[] args);

    List<String> onTabComplete(CommandSender executor, String[] args);

    String getName();

}
