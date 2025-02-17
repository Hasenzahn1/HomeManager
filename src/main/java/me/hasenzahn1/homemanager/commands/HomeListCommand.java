package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerGroupArguments;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            Language.sendMessage(commandSender, Language.NO_PLAYER);
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
            sendNoHomesMessage(arguments);
            return true;
        }

        //Create homes list
        Component homeListText = createHomeListText(arguments, playerHomes);
        commandSender.sendMessage(homeListText);
        return true;
    }

    private Component createHomeListText(PlayerGroupArguments arguments, PlayerHomes playerHomes) {
        Component display = Component.text(Language.getLang(Language.HOME_LIST_HEADER, "prefix", HomeManager.PREFIX));
        OfflinePlayer player = Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID());

        List<Home> homes = playerHomes.getHomes().stream().sorted(Comparator.comparing(Home::name)).toList();
        List<Component> components = new ArrayList<>();

        //Create Components
        for (Home home : homes) {
            Component currentHome = Component.text(Language.getLang(Language.HOME_LIST_HOME, "name", home.name()));
            currentHome = currentHome.clickEvent(ClickEvent.runCommand("/home " + player.getName() + " " + home.name() + " -g " + arguments.getWorldGroup().getName()));
            components.add(currentHome);
            components.add(Component.text(Language.getLang(Language.HOME_LIST_SEPARATOR)));
        }

        //Combine Components
        for (int i = 0; i < components.size() - 1; i++) display = display.append(components.get(i));
        return display;
    }

    private void sendNoHomesMessage(PlayerGroupArguments arguments) {
        if (arguments.isSelf()) {
            Language.sendMessage(arguments.getCmdSender(), Language.HOME_LIST_NO_HOMES);
        } else {
            Language.sendMessage(arguments.getCmdSender(), Language.HOME_LIST_NO_HOMES_OTHER, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName());
        }
    }

    // /homes (player) (--group groupname)
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return List.of();

        WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(player.getWorld());

        List<String> offlinePlayers = commandSender.hasPermission("homemanager.commands.homelist.other." + worldGroup.getName()) ? completionsHelper.matchAndSort(completionsHelper.getOfflinePlayers(), strings[0]) : List.of();
        List<String> groupPrefix = List.of("-g", "-group");
        List<String> groups = commandSender.hasPermission("homemanager.commands.homelist.group." + worldGroup.getName()) ? completionsHelper.getWorldGroups(commandSender, "homemanager.commands.homelist.groups") : List.of();


        if (strings.length == 1) {
            if (!offlinePlayers.isEmpty()) return offlinePlayers;
            if (!completionsHelper.matchAndSort(groupPrefix, strings[0]).isEmpty() && !groups.isEmpty())
                return completionsHelper.matchAndSort(groupPrefix, strings[0]);
            return List.of();
        }
        if (strings.length == 2) {
            if (groups.isEmpty()) return List.of();
            if (!completionsHelper.matchAndSort(groupPrefix, strings[0]).isEmpty())
                return completionsHelper.matchAndSort(groups, strings[1]);
            if (strings[0].startsWith("-")) return List.of();
            return completionsHelper.matchAndSort(groupPrefix, strings[1]);
        }
        if (strings.length == 3) {
            if (!completionsHelper.matchAndSort(groupPrefix, strings[1]).isEmpty())
                return completionsHelper.matchAndSort(groups, strings[2]);
            return List.of();
        }


        return List.of();
    }
}
