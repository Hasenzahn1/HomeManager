package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.HomeAndDelHomeArguments;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
        if (!commandSender.hasPermission("homemanager.commands.delhome." + arguments.getSendersCurrentWorldGroup().getName())) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check if the command is valid
        if (!arguments.isValidArguments()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        //Check if groupFlagArg is incorrect
        if (!arguments.isGroupFlagValid()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        //Unknown player
        if (!arguments.isArgPlayerValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", arguments.getActionPlayerName())));
            return true;
        }

        //Check if the player has the required .other.group permission if requested
        if (!arguments.senderHasValidOtherPermission("homemanager.commands.delhome")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Group does not exist
        if (!arguments.isGroupValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_GROUP, "name", arguments.getGroupFlagArg())));
            return true;
        }

        if (!arguments.senderHasValidGroupPermission("homemanager.commands.delhome")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_GROUP)));
            return true;
        }

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, Location> playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());

        //Check if home exists
        if (!playerHomes.containsKey(arguments.getHomeName())) {
            sendUnknownHomeMessage(arguments);
            dbSession.destroy();
            return true;
        }

        //Grant free home
        int freeHomes = dbSession.getFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        dbSession.saveFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName(), freeHomes + 1);

        //Delete home
        dbSession.deleteHomesFromTheDatabase(arguments.getActionPlayerUUID(), arguments.getHomeName(), arguments.getWorldGroup().getName());
        dbSession.destroy();
        sendSuccessMessage(arguments);
        return true;
    }

    private void sendUnknownHomeMessage(HomeAndDelHomeArguments arguments) {
        if (arguments.isSelf()) {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME, "name", arguments.getHomeName())));
        } else {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME_OTHER, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName(), "name", arguments.getHomeName())));

        }
    }

    public void sendSuccessMessage(HomeAndDelHomeArguments arguments) {
        if (arguments.isSelf()) {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.DEL_HOME_SUCCESS, "name", arguments.getHomeName())));
        } else {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.DEL_HOME_SUCCESS_OTHER, "name", arguments.getHomeName(), "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName())));
        }
    }
}
