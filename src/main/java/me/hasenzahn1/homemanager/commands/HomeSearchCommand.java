package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.HomeDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HomeSearchCommand implements CommandExecutor, TabCompleter {

    private static final HashMap<UUID, List<HomeDisplay>> HOME_DISPLAYS = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Logger.DEBUG.log(commandSender.getName() + " executed /" + command.getName() + " " + String.join(" ", args));
        if (!commandSender.hasPermission("homeadmin.commands.homesearch")) {
            MessageManager.sendMessage(commandSender, Language.NO_PERMISSION);
            return true;
        }

        if (!(commandSender instanceof Player)) {
            MessageManager.sendMessage(commandSender, Language.NO_PLAYER);
            return true;
        }

        if (args.length != 1) {
            MessageManager.sendMessage(commandSender, Language.INVALID_COMMAND, "command", "/homesearch <radius>");
            return true;
        }

        if (!isInt(args[0])) {
            MessageManager.sendMessage(commandSender, Language.INVALID_COMMAND, "command", "/homesearch <radius>");
            return true;
        }

        Player player = (Player) commandSender;
        int radius = Integer.parseInt(args[0]);

        //Get Homes from database
        DatabaseAccessor session = DatabaseAccessor.openSession();
        List<Home> homes = session.getHomesInRadius(player.getLocation(), radius);
        session.destroy();

        if (homes.isEmpty()) {
            MessageManager.sendMessage(player, Language.HOME_SEARCH_NO_HOMES_FOUND,
                    "radius", String.valueOf(radius),
                    "x", String.valueOf(player.getLocation().getBlockX()),
                    "y", String.valueOf(player.getLocation().getBlockY()),
                    "z", String.valueOf(player.getLocation().getBlockZ())
            );
            return true;
        }

        spawnDisplays(player, homes);
        displayHomes(player, radius, homes);

        return true;
    }

    private void spawnDisplays(Player player, List<Home> homes) {
        HOME_DISPLAYS.getOrDefault(player.getUniqueId(), new ArrayList<>()).forEach(HomeDisplay::destroy);
        HOME_DISPLAYS.put(player.getUniqueId(), new ArrayList<>());
        // Create displays
        List<HomeDisplay> displays = homes.stream().map(h -> new HomeDisplay(player, h)).toList();

        long spawned = displays.stream().filter(HomeDisplay::hasBeenSpawned).count();
        Logger.DEBUG.log("Homesearch spawned " + spawned + " homes (" + (displays.size() - spawned) + " in unloaded chunks) for player " + player.getName());

        HOME_DISPLAYS.put(player.getUniqueId(), new ArrayList<>(displays));
        new BukkitRunnable() {

            @Override
            public void run() {
                displays.forEach(HomeDisplay::destroy);
                HOME_DISPLAYS.remove(player.getUniqueId());
                Logger.DEBUG.log("Homesearch removed displays for player " + player.getName());
            }
        }.runTaskLater(HomeManager.getInstance(), DefaultConfig.HOME_SEARCH_DURATION_IN_SECONDS * 20);
    }

    private void displayHomes(Player player, int radius, List<Home> homes) {
        HashMap<String, List<Home>> playerHomes = new HashMap<>();

        for (Home home : homes) {
            if (!playerHomes.containsKey(home.getOwnersName()))
                playerHomes.put(home.getOwnersName(), new ArrayList<>());
            playerHomes.get(home.getOwnersName()).add(home);
        }

        //Display header "%prefix% &aFound &6%amount% &ahomes at &7(%x%, %y%, %z%) &awith radius &6%radius%"
        MessageManager.sendMessage(player, Language.HOME_SEARCH_HEADER,
                "prefix", HomeManager.PREFIX,
                "amount", String.valueOf(homes.size()),
                "radius", String.valueOf(radius),
                "x", String.valueOf(player.getLocation().getBlockY()),
                "y", String.valueOf(player.getLocation().getBlockY()),
                "z", String.valueOf(player.getLocation().getBlockZ()));


        //Display homes
        for (String key : playerHomes.keySet().stream().sorted().toList()) {
            player.sendMessage(MessageManager.getPAPILang(player, Language.HOME_SEARCH_PLAYER_LINE, "player", key));

            for (Home home : playerHomes.get(key)) {
                Component homeLine = Component.text(MessageManager.getPAPILang(player, Language.HOME_SEARCH_HOME_LINE,
                        "homename", home.name(),
                        "x", String.valueOf(home.location().getBlockX()),
                        "y", String.valueOf(home.location().getBlockY()),
                        "z", String.valueOf(home.location().getBlockZ())));
                homeLine = homeLine.hoverEvent(HoverEvent.showText(Component.text(HomeCommand.getCommandFromHome(home))))
                        .clickEvent(ClickEvent.runCommand(HomeCommand.getCommandFromHome(home)));

                Component deleteBtn = Component.text(MessageManager.getPAPILang(player, Language.HOME_SEARCH_DELETE_BUTTON))
                        .clickEvent(ClickEvent.runCommand(DelHomeCommand.getCommandFromHome(home)))
                        .hoverEvent(Component.text(DelHomeCommand.getCommandFromHome(home)));

                player.sendMessage(homeLine.append(Component.text(" ")).append(deleteBtn));
            }
        }
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return List.of("<radius>");
        }
        return List.of();
    }

    private boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void destroy() {
        for (List<HomeDisplay> list : HOME_DISPLAYS.values()) {
            for (HomeDisplay homeDisplay : list) homeDisplay.destroy();
        }
        HOME_DISPLAYS.clear();
    }
}
