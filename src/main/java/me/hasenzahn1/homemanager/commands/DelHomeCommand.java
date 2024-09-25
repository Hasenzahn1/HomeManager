package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class DelHomeCommand implements CommandExecutor {

    // /delhome (player) homename (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        WorldGroup playerCurrentGroup = HomeManager.getInstance().getWorldGroupManager().groupsByWorld().get(((Player) commandSender).getWorld());

        //Check for base delhome permission
        if (!commandSender.hasPermission("homemanager.delhome." + playerCurrentGroup.getName())) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Gather data from arguments
        Player playerExecutingCommand = (Player) commandSender;
        String playerToDelHomeFrom = "";
        String homeName = "";
        String groupName = "";
        String groupFlagArg = "";
        boolean groupFlagSet = false;

        if (args.length > 4 || args.length == 0) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/delhome (player) <name> (-group group)")));
            return true;
        }

        //Get Homename from home
        if (args.length == 1 || args.length == 3) { //Fall /delhome homename || oder /delhome homename (--group group)
            homeName = args[0];
            playerToDelHomeFrom = playerExecutingCommand.getName();
        } else { //Fall /delhome (player) homename || oder /delhome (player) homename (--group group)
            homeName = args[1];
            playerToDelHomeFrom = args[0];
        }

        //Get Group from command
        if (args.length == 3 || args.length == 4) {
            groupFlagArg = args[args.length - 2];
            groupName = args[args.length - 1].toLowerCase();
            groupFlagSet = true;
        } else {
            groupName = HomeManager.getInstance().getWorldGroupManager().groupsByWorld().get(playerExecutingCommand.getWorld()).getName().toLowerCase();
        }

        //Check if groupFlagArg is incorrect
        if (groupFlagSet && !groupFlagArg.equals("-g") && !groupFlagArg.equals("-group")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/delhome (player) <name> (-group group)")));
            return true;
        }

        //Unknown player
        if (Bukkit.getOfflinePlayerIfCached(playerToDelHomeFrom) == null) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", playerToDelHomeFrom)));
            return true;
        }

        //Check if group exists
        boolean groupExists = HomeManager.getInstance().getWorldGroupManager().groupsByName().containsKey(groupName);

        UUID playerExecutingUUID = playerExecutingCommand.getUniqueId();
        UUID playerDelHomeFromUUID = Bukkit.getOfflinePlayerIfCached(playerToDelHomeFrom).getUniqueId();

        boolean isSelf = playerExecutingUUID.equals(playerDelHomeFromUUID);

        //Check if the player has the required .other.group permission if requested
        if (groupExists && (!isSelf || groupFlagSet) && !playerExecutingCommand.hasPermission("homemanager.delhome.other." + groupName)) {
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
        HashMap<String, Location> playerHomes = dbSession.getHomesFromPlayer(playerDelHomeFromUUID, groupName);

        //Check if home exists
        if (!playerHomes.containsKey(homeName)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME, "name", homeName, "group", groupName)));
            dbSession.destroy();
            return true;
        }

        //Grant free home
        int freeHomes = dbSession.getFreeHomes(playerDelHomeFromUUID, groupName);
        dbSession.saveFreeHomes(playerDelHomeFromUUID, groupName, freeHomes + 1);

        //Delete home
        dbSession.deleteHomesFromTheDatabase(playerDelHomeFromUUID, homeName, groupName);
        dbSession.destroy();
        commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.DEL_HOME_SUCCESS, "name", homeName)));
        return true;
    }
}
