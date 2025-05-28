package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.commands.system.BaseHomeCommand;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
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
        Logger.DEBUG.log(commandSender.getName() + " executed /" + command.getName() + " " + String.join(" ", args));

        //Check player
        if (!(commandSender instanceof Player)) {
            MessageManager.sendMessage(commandSender, Language.NO_PLAYER);
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
        PlayerHomes playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup());

        //Check if home exists
        if (!playerHomes.homeExists(arguments.getHomeName())) {
            MessageManager.sendUnknownHomeMessage(arguments);
            dbSession.destroy();
            return true;
        }

        //Get Valid Home
        Home home = playerHomes.getHome(arguments.getHomeName());

        //Delete home
        dbSession.deleteHomesFromTheDatabase(arguments.getActionPlayerUUID(), home.name(), arguments.getWorldGroup());
        Logger.DEBUG.log("Deleted home " + home.name() + " of player " + arguments.getActionPlayerUUID() + " in worldgroup " + arguments.getWorldGroup().getName());

        //Grant free home
        //boolean shouldNotGrantFreeHomes = !arguments.getWorldGroup().getSettings().isFreeHomesDisableInCreative() && arguments.getCmdSender().isInvulnerable();
        boolean shouldGrantFreeHomes = !arguments.isSelf() || (arguments.getWorldGroup().getSettings().isFreeHomesDisableInCreative() && arguments.getCmdSender().isInvulnerable());
        if (arguments.getWorldGroup().getSettings().isFreeHomesActive() && shouldGrantFreeHomes) {
            dbSession.incrementFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
            Logger.DEBUG.log("Granted freehome for player " + arguments.getActionPlayerUUID() + " in worldgroup " + arguments.getWorldGroup().getName());
        }
        dbSession.destroy();

        sendSuccessMessage(arguments, home);
        HomeManager.getInstance().getHomesCache().invalidateCache(arguments.getActionPlayerUUID());
        return true;
    }

    public void sendSuccessMessage(PlayerNameGroupArguments arguments, Home home) {
        if (arguments.isSelf()) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.DEL_HOME_SUCCESS, "homename", home.name());
        } else {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.DEL_HOME_SUCCESS_OTHER, "homename", home.name(), "player", home.getOwnersName());
        }
    }


    // /delhome (player) homename (--group groupname)
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        //No TabCompletions for non player command executors
        if (!(commandSender instanceof Player player)) return List.of();

        //No Completion if command is too long
        if (strings.length >= 5) return List.of();

        //Get World group
        WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(player.getWorld());

        //Check permissions
        boolean hasOtherPermission = commandSender.hasPermission("homemanager.commands.delhome.other." + worldGroup.getName());
        boolean hasGroupPermission = commandSender.hasPermission("homemanager.commands.delhome.group." + worldGroup.getName());

        //Define Completions
        List<String> offlinePlayers = completionsHelper.matchAndSort(completionsHelper.getOfflinePlayers(), strings[0]);
        List<String> playersHomes = completionsHelper.getHomeSuggestions(player, player.getName());
        List<String> groupPrefix = List.of("-g", "-group");
        List<String> groups = completionsHelper.getWorldGroups(commandSender, "homemanager.commands.home.groups");
        boolean otherPlayerArgMightBeSet = !offlinePlayers.isEmpty();

        //Define completions for first arg
        if (strings.length == 1) {
            // Check Homes Arg: /home <home>
            List<String> matchedHomeNames = completionsHelper.matchAndSort(playersHomes, strings[0]);
            if (!matchedHomeNames.isEmpty()) return matchedHomeNames;

            //Check OfflinePlayer arg: /home (player)
            if (hasOtherPermission) return offlinePlayers;

            //No permission For other player and wrong homename
            return List.of();
        }

        //Define completions for second arg
        if (strings.length == 2) {
            //Check Home Arg: /home <player> <home>
            if (hasOtherPermission && otherPlayerArgMightBeSet) {
                List<String> otherPlayersHomes = completionsHelper.getHomeSuggestions(player, strings[0]);
                return completionsHelper.matchAndSort(otherPlayersHomes, strings[1]);
            }

            // Complete for group Flag: /home <home> -g
            if (hasGroupPermission) {
                return completionsHelper.matchAndSort(groupPrefix, strings[1]);
            }
            return List.of();
        }

        //Define completions for third arg
        if (strings.length == 3) {
            //Check for /home <player> <home> -g
            if (hasOtherPermission && otherPlayerArgMightBeSet) {
                return completionsHelper.matchAndSort(groupPrefix, strings[2]);
            }

            //Check for /home <home> -g <group>
            List<String> groupPrefixMatches = completionsHelper.matchAndSort(groupPrefix, strings[1]);
            if (hasGroupPermission && !groupPrefixMatches.isEmpty()) {
                return completionsHelper.matchAndSort(groups, strings[2]);
            }

            //All other cases
            return List.of();
        }

        //Define completions for fourth arg
        if (strings.length == 4) {
            if (!hasOtherPermission || !hasGroupPermission) return List.of();
            if (!otherPlayerArgMightBeSet) return List.of();

            //Check for /home <player> <home> -g <group>
            List<String> groupPrefixMatches = completionsHelper.matchAndSort(groupPrefix, strings[2]);
            if (!groupPrefixMatches.isEmpty()) return completionsHelper.matchAndSort(groups, strings[3]);

            return List.of();
        }

        return List.of();
    }

    public static String getCommandFromHome(Home home) {
        WorldGroup group = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(home.location().getWorld());
        return "/delhome " + home.getOwnersName() + " " + home.name() + " -g " + group.getName();
    }
}
