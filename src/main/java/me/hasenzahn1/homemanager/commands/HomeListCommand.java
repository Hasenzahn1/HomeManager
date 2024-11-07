package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.ListHomesArguments;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.homes.PlayerHome;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class HomeListCommand implements CommandExecutor {

    // /homes (player) (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        ListHomesArguments arguments = ListHomesArguments.parse(((Player) commandSender), args);

        //Check for base delhome permission
        if (!commandSender.hasPermission("homemanager.commands.homelist." + arguments.getSendersCurrentWorldGroup().getName())) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        if (!arguments.isValidArguments()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, false, arguments.getWorldGroup());
            return true;
        }

        //Check if groupFlagArg is incorrect
        if (!arguments.isGroupFlagValid()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, false, arguments.getWorldGroup());
            return true;
        }

        //Unknown player
        if (!arguments.argPlayerValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", arguments.getPlayerArgumentName())));
            return true;
        }

        //Check if the player has the required .other.<group> permission if requested
        if (!arguments.senderHasValidOtherPermission()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Group does not exist
        if (!arguments.isGroupValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_GROUP, "name", arguments.getGroupFlagArg())));
            return true;
        }

        //Check if the player has the required .group.<group> permission if requested
        if (!arguments.senderHasValidGroupPermission()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_GROUP)));
            return true;
        }

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, PlayerHome> playerHomes = dbSession.getHomesFromPlayer(arguments.getPlayerListHomesUUID(), arguments.getWorldGroup().getName());
        dbSession.destroy();

        //No Homes Message
        if (playerHomes.isEmpty()) {
            sendNoHomesMessage(arguments);
            return true;
        }

        //Create homes list
        Component display = Component.text(Language.getLang(Language.HOME_LIST_HEADER, "prefix", HomeManager.PREFIX));
        OfflinePlayer player = Bukkit.getOfflinePlayer(arguments.getPlayerListHomesUUID());

        int currentHomeIndex = 0;
        int maxhomes = playerHomes.size();

        for (PlayerHome home : playerHomes.values()) {
            Component currentHome = Component.text(Language.getLang(Language.HOME_LIST_HOME, "name", home.getName()));
            currentHome = currentHome.clickEvent(ClickEvent.runCommand("/home " + player.getName() + " " + home.getName() + " -g " + arguments.getWorldGroup().getName()));
            display = display.append(currentHome);

            if (currentHomeIndex < maxhomes - 1)
                display = display.append(Component.text(Language.getLang(Language.HOME_LIST_SEPARATOR)));
            currentHomeIndex++;
        }

        commandSender.sendMessage(display);
        return true;
    }

    private void sendNoHomesMessage(ListHomesArguments arguments) {
        if (arguments.isSelf()) {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.HOME_LIST_NO_HOMES)));
        } else {
            arguments.getCmdSender().sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.HOME_LIST_NO_HOMES_OTHER, "player", Bukkit.getOfflinePlayer(arguments.getPlayerListHomesUUID()).getName())));
        }
    }

}
