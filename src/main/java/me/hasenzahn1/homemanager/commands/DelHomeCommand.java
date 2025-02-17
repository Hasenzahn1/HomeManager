package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DelHomeCommand extends BaseHomeCommand {

    public DelHomeCommand(CompletionsHelper completionsHelper) {
        super(completionsHelper);
    }

    // /delhome (player) homename (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            Language.sendMessage(commandSender, Language.NO_PLAYER);
            return true;
        }

        //Parse arguments
        PlayerNameGroupArguments arguments = PlayerNameGroupArguments.parseArguments(((Player) commandSender), args);

        //Validate Permissions
        if (PermissionValidator.checkInvalidPermissionsWithGroup(commandSender, arguments, "homemanager.commands.delhome"))
            return true;

        //Validate Arguments
        if (ArgumentValidator.checkInvalidPlayerGroupArgs(commandSender, arguments, command))
            return true;

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        PlayerHomes playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());

        //Check if home exists
        if (!playerHomes.homeExists(arguments.getHomeName())) {
            Language.sendUnknownHomeMessage(arguments);
            dbSession.destroy();
            return true;
        }

        //Get Valid Home
        Home home = playerHomes.getHome(arguments.getHomeName());

        //Delete home
        dbSession.deleteHomesFromTheDatabase(arguments.getActionPlayerUUID(), home.name(), arguments.getWorldGroup().getName());

        //Grant free home
        dbSession.incrementFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        dbSession.destroy();

        sendSuccessMessage(arguments, home.name());
        completionsHelper.invalidatePlayerHomes(arguments.getActionPlayerUUID());
        return true;
    }


    public void sendSuccessMessage(PlayerNameGroupArguments arguments, String homeName) {
        if (arguments.isSelf()) {
            Language.sendMessage(arguments.getCmdSender(), Language.DEL_HOME_SUCCESS, "name", homeName);
        } else {
            Language.sendMessage(arguments.getCmdSender(), Language.DEL_HOME_SUCCESS_OTHER, "name", homeName, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return List.of();

        WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(player.getWorld());

        boolean hasOtherPermission = commandSender.hasPermission("homemanager.commands.delhome.other." + worldGroup.getName());
        boolean hasGroupPermission = commandSender.hasPermission("homemanager.commands.delhome.group." + worldGroup.getName());

        List<String> offlinePlayers = hasOtherPermission ? completionsHelper.matchAndSort(completionsHelper.getOfflinePlayers(), strings[0]) : List.of();
        List<String> playersHomes = completionsHelper.getHomeSuggestions(player, player.getName());

        List<String> groupPrefix = List.of("-g", "-group");
        List<String> groups = hasGroupPermission ? completionsHelper.getWorldGroups(commandSender, "homemanager.commands.delhome.groups") : List.of();

        boolean arg0IsPlayer = !offlinePlayers.isEmpty();

        if (strings.length == 1) {
            if (!completionsHelper.matchAndSort(playersHomes, strings[0]).isEmpty()) // player types homename
                return completionsHelper.matchAndSort(playersHomes, strings[0]);

            return offlinePlayers;
        }

        if (strings.length == 2) {
            if (arg0IsPlayer) {
                List<String> otherHomes = completionsHelper.getHomeSuggestions(player, strings[0]);
                return completionsHelper.matchAndSort(otherHomes, strings[1]);
            }

            if (hasGroupPermission) {
                return completionsHelper.matchAndSort(groupPrefix, strings[1]);
            }
            return List.of();
        }

        if (strings.length == 3) {
            if (arg0IsPlayer) {
                return completionsHelper.matchAndSort(groupPrefix, strings[2]);
            }

            if (!completionsHelper.matchAndSort(groupPrefix, strings[1]).isEmpty() && hasGroupPermission) {
                return completionsHelper.matchAndSort(groups, strings[2]);
            }
            return List.of();
        }

        if (strings.length == 4) {
            if (!hasOtherPermission && !hasGroupPermission) return List.of();
            if (!arg0IsPlayer) return List.of();
            return completionsHelper.matchAndSort(groups, strings[3]);
        }

        return List.of();
    }
}
