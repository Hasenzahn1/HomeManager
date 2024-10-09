package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class HomeListCommand implements CommandExecutor {

    // /homes (player) (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        WorldGroup playerCurrentGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(((Player) commandSender).getWorld());

        //Check for base delhome permission
        if (!commandSender.hasPermission("homemanager.homelist." + playerCurrentGroup.getName())) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Gather data from arguments
        Player playerExecutingCommand = (Player) commandSender;
        String playerGetHomesFrom = "";
        String groupName = "";
        String groupFlagArg = "";
        boolean groupFlagSet = false;

        if (args.length > 4) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/homes (player) (-group group)")));
            return true;
        }

        if (args.length == 0 || args.length == 2) { //Fall /homes || oder /homes (--group group)
            playerGetHomesFrom = playerExecutingCommand.getName();
        } else { //Fall /home (player) || oder /home (player) (--group group)
            playerGetHomesFrom = args[0];
        }

        //Get Group from command
        if (args.length == 2 || args.length == 3) {
            groupFlagArg = args[args.length - 2];
            groupName = args[args.length - 1].toLowerCase();
            groupFlagSet = true;
        } else {
            groupName = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(playerExecutingCommand.getWorld()).getName().toLowerCase();
        }

        //Check if groupFlagArg is incorrect
        if (groupFlagSet && !groupFlagArg.equals("-g") && !groupFlagArg.equals("-group")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/home (player) <name> (-group group)")));
            return true;
        }

        //Unknown player
        if (Bukkit.getOfflinePlayerIfCached(playerGetHomesFrom) == null) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", playerGetHomesFrom)));
            return true;
        }

        //Check if group exists
        boolean groupExists = HomeManager.getInstance().getWorldGroupManager().groupExists(groupName);

        UUID playerExecutingUUID = playerExecutingCommand.getUniqueId();
        UUID playerGetHomesFromUUID = Bukkit.getOfflinePlayerIfCached(playerGetHomesFrom).getUniqueId();

        boolean isSelf = playerExecutingUUID.equals(playerGetHomesFromUUID);

        //Check if the player has the required .other.group permission if requested
        if (groupExists && (!isSelf || groupFlagSet) && !playerExecutingCommand.hasPermission("homemanager.home.other." + groupName)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Group does not exist
        if (!groupExists) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_GROUP, "name", groupName)));
            return true;
        }

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, Location> playerHomes = dbSession.getHomesFromPlayer(playerGetHomesFromUUID, groupName);
        dbSession.destroy();

        Component display = Component.text(Language.getLang(Language.HOME_LIST_HEADER));

        int currentHomeIndex = 0;
        int maxhomes = playerHomes.size();

        for (String homename : playerHomes.keySet()) {
            Component currentHome = Component.text(homename);
            currentHome = currentHome.clickEvent(ClickEvent.runCommand("/home " + playerGetHomesFrom + " " + homename + " -g " + groupName));
            display = display.append(currentHome);

            if (currentHomeIndex < maxhomes - 1) display = display.append(Component.text(", "));
            currentHomeIndex++;
        }

        commandSender.sendMessage(display);
        return true;
    }

}
