package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerGroupArguments;
import me.hasenzahn1.homemanager.commands.system.BaseHomeCommand;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeListCommand extends BaseHomeCommand {

    public HomeListCommand(CompletionsHelper completionsHelper) {
        super(completionsHelper);
    }

    // /homes (player) (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            MessageManager.sendMessage(commandSender, Language.NO_PLAYER);
            return true;
        }

        //Parse arguments
        PlayerGroupArguments arguments = PlayerGroupArguments.parseArguments(((Player) commandSender), args);

        //Check for permissions
        if (PermissionValidator.checkInvalidPermissionsWithGroup(commandSender, arguments, "homemanager.commands.homelist"))
            return true;

        //Validate Player Args
        if (ArgumentValidator.checkInvalidPlayerGroupArgs(commandSender, arguments, command))
            return true;

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        PlayerHomes playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        dbSession.destroy();

        //No Homes Message
        if (!playerHomes.hasHomes()) {
            MessageManager.sendNoHomesMessage(arguments);
            return true;
        }

        //Create homes list
        Component homeListText = createHomeListText(arguments, playerHomes);
        commandSender.sendMessage(homeListText);
        return true;
    }

    private Component createHomeListText(PlayerGroupArguments arguments, PlayerHomes playerHomes) {
        Component display = Component.text(Language.getLang(Language.HOME_LIST_HEADER, "prefix", HomeManager.PREFIX));
        String player = playerHomes.getHomes().get(0).getOwnersName();

        List<Home> homes = playerHomes.getHomes().stream().sorted(Comparator.comparing(Home::name)).toList();
        List<Component> components = new ArrayList<>();

        //Create Components
        for (Home home : homes) {
            Component currentHome = Component.text(Language.getLang(Language.HOME_LIST_HOME, "name", home.name()));
            currentHome = currentHome.clickEvent(ClickEvent.runCommand("/home " + player + " " + home.name() + " -g " + arguments.getWorldGroup().getName()));
            components.add(currentHome);
            components.add(Component.text(Language.getLang(Language.HOME_LIST_SEPARATOR)));
        }

        //Combine Components
        for (int i = 0; i < components.size() - 1; i++) display = display.append(components.get(i));
        return display;
    }


    // /homes (player) (--group groupname)
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        //No TabCompletion for non player command executors
        if (!(commandSender instanceof Player player)) return List.of();

        //No Completion if command is too long
        if (args.length >= 4) return List.of();

        //Get World group
        WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(player.getWorld());

        //Check permissions
        boolean playerHasOtherPermission = commandSender.hasPermission("homemanager.commands.homelist.other." + worldGroup.getName());
        boolean playerHasGroupPermission = commandSender.hasPermission("homemanager.commands.homelist.group." + worldGroup.getName());

        //Define Completions
        List<String> offlinePlayers = completionsHelper.matchAndSort(completionsHelper.getOfflinePlayers(), args[0]);
        List<String> groupPrefix = List.of("-g", "-group");
        List<String> groups = completionsHelper.getWorldGroups(commandSender, "homemanager.commands.homelist.groups");
        boolean otherPlayerArgMightBeSet = !offlinePlayers.isEmpty();

        //Define Completions for first argument
        if (args.length == 1) {
            //Check Other Player Arg: /homes (player)
            if (playerHasOtherPermission && otherPlayerArgMightBeSet) return offlinePlayers;

            //Check Prefixes: /homes -g
            List<String> prefixMatches = completionsHelper.matchAndSort(groupPrefix, args[0]);
            if (playerHasGroupPermission && !prefixMatches.isEmpty())
                return completionsHelper.matchAndSort(groupPrefix, args[0]);

            //No arguments
            return List.of();
        }

        //Define completions for second argument
        if (args.length == 2) {
            //Check if the player has no group permission: /homes <player>
            if (!playerHasGroupPermission) return List.of();

            List<String> prefixMatchesInFirstArg = completionsHelper.matchAndSort(groupPrefix, args[0]);
            List<String> prefixMatchesInSecondArg = completionsHelper.matchAndSort(groupPrefix, args[1]);
            List<String> groupMatchesInSecondArg = completionsHelper.matchAndSort(groups, args[1]);

            //Display group: /homes -g <group>
            if (!prefixMatchesInFirstArg.isEmpty()) return groupMatchesInSecondArg;

            //Display Prefix: /homes <player> -g
            if (!offlinePlayers.isEmpty() && !prefixMatchesInSecondArg.isEmpty()) return prefixMatchesInSecondArg;

            return List.of();
        }

        //Define completions for third arg
        if (args.length == 3) {
            if (!playerHasGroupPermission) return List.of();

            List<String> prefixMatchesInSecondArg = completionsHelper.matchAndSort(groupPrefix, args[1]);
            List<String> groupMatchesInThirdArg = completionsHelper.matchAndSort(groups, args[2]);

            // Display Group: /homes <player> -g <group>
            if (!offlinePlayers.isEmpty() && !prefixMatchesInSecondArg.isEmpty()) return groupMatchesInThirdArg;

            return List.of();
        }
        return List.of();
    }
}
