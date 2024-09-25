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

public class HomeCommand implements CommandExecutor {


    // /home (player) homename (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        WorldGroup playerCurrentGroup = HomeManager.getInstance().getWorldGroupManager().groupsByWorld().get(((Player) commandSender).getWorld());

        //Check for base delhome permission
        if (!commandSender.hasPermission("homemanager.home." + playerCurrentGroup.getName())) {
            commandSender.sendMessage(Component.text(Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Gather data from arguments
        Player playerExecutingCommand = (Player) commandSender;
        String playerGetHomeToTeleportToFrom = "";
        String homeName = "";
        String groupName = "";
        String groupFlagArg = "";
        boolean groupFlagSet = false;

        if (args.length > 4 || args.length == 0) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/home (player) <name> (-group group)")));
            return true;
        }

        //Get Homename from home
        if (args.length == 1 || args.length == 3) { //Fall /home homename || oder /home homename (--group group)
            homeName = args[0];
            playerGetHomeToTeleportToFrom = playerExecutingCommand.getName();
        } else { //Fall /home (player) homename || oder /home (player) homename (--group group)
            homeName = args[1];
            playerGetHomeToTeleportToFrom = args[0];
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
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/home (player) <name> (-group group)")));
            return true;
        }

        //Unknown player
        if (Bukkit.getOfflinePlayerIfCached(playerGetHomeToTeleportToFrom) == null) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", playerGetHomeToTeleportToFrom)));
            return true;
        }

        //Check if group exists
        boolean groupExists = HomeManager.getInstance().getWorldGroupManager().groupsByName().containsKey(groupName);

        UUID playerExecutingUUID = playerExecutingCommand.getUniqueId();
        UUID playerGetHomeToTeleportToFromUUID = Bukkit.getOfflinePlayerIfCached(playerGetHomeToTeleportToFrom).getUniqueId();

        boolean isSelf = playerExecutingUUID.equals(playerGetHomeToTeleportToFromUUID);

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
        HashMap<String, Location> playerHomes = dbSession.getHomesFromPlayer(playerGetHomeToTeleportToFromUUID, groupName);
        dbSession.destroy();

        //Check if home exists
        if (!playerHomes.containsKey(homeName)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_HOME, "name", homeName, "group", groupName)));
            return true;
        }

        Location location = playerHomes.get(homeName);

        playerExecutingCommand.teleport(location);

        commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.HOME_SUCCESS, "name", homeName)));
        return true;
    }

}
