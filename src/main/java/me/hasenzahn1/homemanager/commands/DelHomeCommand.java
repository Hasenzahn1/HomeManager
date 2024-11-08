package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.homes.PlayerHome;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
        PlayerNameGroupArguments arguments = PlayerNameGroupArguments.parseArguments(((Player) commandSender), args);

        //Check for base delhome permission
        if (!arguments.senderHasValidCommandPermission("homemanager.commands.delhome")) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check if the player has the required .other.group permission if requested
        if (!arguments.senderHasValidOtherPermission("homemanager.commands.delhome")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Check for valid group permission
        if (!arguments.senderHasValidGroupPermission("homemanager.commands.delhome")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_GROUP)));
            return true;
        }

        //Check if the command is valid
        if (arguments.invalidArguments()) {
            System.out.println("Invalid Arguments");
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        //Unknown player
        if (arguments.playerArgInvalid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", arguments.getOptionalPlayerArg())));
            return true;
        }

        //Check if groupFlagArg is incorrect
        if (arguments.groupFlagInvalid()) {
            System.out.println("Invalid Group Flag");
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        //Group does not exist
        if (arguments.groupInvalid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_GROUP, "name", arguments.getGroupName())));
            return true;
        }

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, PlayerHome> playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());

        //Check if home exists
        if (!playerHomes.containsKey(arguments.getHomeName().toLowerCase())) {
            sendUnknownHomeMessage(arguments);
            dbSession.destroy();
            return true;
        }

        //Grant free home
        int freeHomes = dbSession.getFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        dbSession.saveFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName(), freeHomes + 1);

        //Delete home
        dbSession.deleteHomesFromTheDatabase(arguments.getActionPlayerUUID(), playerHomes.get(arguments.getHomeName()).getName(), arguments.getWorldGroup().getName());
        dbSession.destroy();
        sendSuccessMessage(arguments, playerHomes.get(arguments.getHomeName().toLowerCase()).getName());
        return true;
    }

    private void sendUnknownHomeMessage(PlayerNameGroupArguments arguments) {
        if (arguments.isSelf()) {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME, "name", arguments.getHomeName())));
        } else {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME_OTHER, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName(), "name", arguments.getHomeName())));

        }
    }

    public void sendSuccessMessage(PlayerNameGroupArguments arguments, String homeName) {
        if (arguments.isSelf()) {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.DEL_HOME_SUCCESS, "name", homeName)));
        } else {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.DEL_HOME_SUCCESS_OTHER, "name", homeName, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName())));
        }
    }
}
