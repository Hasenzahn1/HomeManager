package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.HomeAndDelHomeArguments;
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

public class HomeCommand implements CommandExecutor {


    // /home (player) homename (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        HomeAndDelHomeArguments arguments = HomeAndDelHomeArguments.parse(((Player) commandSender), args);

        //Check for base delhome permission
        if (!commandSender.hasPermission("homemanager.commands.home." + arguments.getSendersCurrentWorldGroup().getName())) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

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
        if (!arguments.senderHasValidOtherPermission("homemanager.commands.home")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Group does not exist
        if (!arguments.isGroupValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_GROUP, "name", arguments.getGroupFlagArg())));
            return true;
        }

        if (!arguments.senderHasValidGroupPermission("homemanager.commands.home")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_GROUP)));
            return true;
        }

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, PlayerHome> dbHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        dbSession.destroy();

        PlayerHome requestedHome = dbHomes.get(arguments.getHomeName().toLowerCase());

        //Check if home exists
        if (requestedHome == null) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME, "name", arguments.getHomeName(), "group", arguments.getWorldGroup().getName())));
            return true;
        }

        arguments.getCmdSender().teleport(requestedHome.getLocation());

        sendSuccessMessage(arguments, requestedHome.getName());
        return true;
    }

    public void sendSuccessMessage(HomeAndDelHomeArguments arguments, String homeName) {
        if (arguments.isSelf()) {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.HOME_SUCCESS, "name", homeName)));
        } else {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.HOME_SUCCESS_OTHER, "name", homeName, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName())));
        }
    }

}
