package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.HomeAndDelHomeArguments;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DelHomeCommand implements CommandExecutor {

    // /delhome (player) homename (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        //Parse arguments
        HomeAndDelHomeArguments arguments = HomeAndDelHomeArguments.parse(((Player) commandSender), args);

        //Check for base delhome permission
        if (!commandSender.hasPermission("homemanager.delhome." + arguments.getSendersCurrentWorldGroup().getName())) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check if the command is valid
        if (!arguments.isValidArguments()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/delhome (player) <name> (-group group)")));
            return true;
        }

        //Check if groupFlagArg is incorrect
        if (!arguments.isGroupFlagValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_FLAG, "flag", arguments.getGroupFlag(), "command", "/delhome (player) <name> (-group group)")));
            return true;
        }

        //Unknown player
        if (!arguments.isArgPlayerValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", arguments.getActionPlayerName())));
            return true;
        }

        //Check if the player has the required .other.group permission if requested
        if (!arguments.senderHasValidOtherPermission("homemanager.delhome")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Group does not exist
        if (!arguments.isGroupValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_GROUP, "name", arguments.getGroupFlagArg())));
            return true;
        }

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, Location> playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());

        //Check if home exists
        if (!playerHomes.containsKey(arguments.getHomeName())) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME, "name", arguments.getHomeName(), "group", arguments.getWorldGroup().getName())));
            dbSession.destroy();
            return true;
        }

        //Grant free home
        int freeHomes = dbSession.getFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        dbSession.saveFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName(), freeHomes + 1);

        //Delete home
        dbSession.deleteHomesFromTheDatabase(arguments.getActionPlayerUUID(), arguments.getHomeName(), arguments.getWorldGroup().getName());
        dbSession.destroy();
        commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.DEL_HOME_SUCCESS, "name", arguments.getHomeName())));
        return true;
    }
}
