package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.util.PlayerNameUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class FreeHomesSubCommand implements ISubCommand {

    @Override
    public void onCommand(CommandSender executor, String[] args) {
        if (args.length < 2 || args.length > 4) {
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin " + getName() + " <add/get/remove/set> <player> <amount> (worldgroup)");
            return;
        }

        //Validate player input
        UUID playerUUID = PlayerNameUtils.getUUIDFromString(args[1]);
        if (playerUUID == null) {
            MessageManager.sendMessage(executor, Language.UNKNOWN_PLAYER, "player", args[1]);
            return;
        }

        //Handle get command
        if (args[0].equalsIgnoreCase("get")) {
            handleGet(executor, playerUUID, args);
            return;
        }

        //Validate Amount arg
        if (!isInt(args[2])) {
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin " + getName() + " <add/get/remove/set> <player> <amount> (worldgroup)");
            return;
        }

        //Validate and get WorldGroup optional arg
        WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup("global");
        if (Bukkit.getPlayer(playerUUID) != null) {
            worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(Bukkit.getPlayer(playerUUID).getWorld());
        } else if (executor instanceof Player) {
            worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(((Player) executor).getWorld());
        }

        if (args.length == 4) {
            worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(args[3]);
            if (worldGroup == null) {
                MessageManager.sendMessage(executor, Language.UNKNOWN_GROUP, "group", args[3]);
                return;
            }
        }

        int amount = Integer.parseInt(args[2]);
        switch (args[0].toLowerCase()) {
            case "add":
                handleAdd(executor, playerUUID, worldGroup, amount);
                return;
            case "remove":
                handleRemove(executor, playerUUID, worldGroup, amount);
                return;
            case "set":
                handleSet(executor, playerUUID, worldGroup, amount);
                return;
        }

        //Invalid subcommand
        MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin " + getName() + " <add/get/remove/set> <player> <amount> (worldgroup)");
    }

    private void handleAdd(CommandSender player, UUID uuid, WorldGroup worldGroup, int amount) {
        DatabaseAccessor session = DatabaseAccessor.openSession();
        int freeHomes = session.getFreeHomes(uuid, worldGroup.getName());
        session.saveFreeHomes(uuid, worldGroup.getName(), freeHomes + amount);
        HomeManager.getInstance().getHomesCache().invalidateCache(uuid);

        MessageManager.sendMessage(player, Language.HOME_ADMIN_FREE_HOME_ADD_SUCCESS, "name", PlayerNameUtils.getPlayerNameFromUUID(uuid), "amount", String.valueOf(freeHomes + amount), "add", String.valueOf(amount));
    }

    private void handleRemove(CommandSender player, UUID uuid, WorldGroup worldGroup, int amount) {
        DatabaseAccessor session = DatabaseAccessor.openSession();
        int freeHomes = session.getFreeHomes(uuid, worldGroup.getName());
        session.saveFreeHomes(uuid, worldGroup.getName(), Math.max(0, freeHomes - amount));
        HomeManager.getInstance().getHomesCache().invalidateCache(uuid);

        MessageManager.sendMessage(player, Language.HOME_ADMIN_FREE_HOME_REMOVE_SUCCESS, "name", PlayerNameUtils.getPlayerNameFromUUID(uuid), "amount", String.valueOf(Math.max(0, freeHomes - amount)), "remove", String.valueOf(amount));
    }

    private void handleSet(CommandSender player, UUID uuid, WorldGroup worldGroup, int amount) {
        DatabaseAccessor session = DatabaseAccessor.openSession();
        session.saveFreeHomes(uuid, worldGroup.getName(), amount);
        session.destroy();
        HomeManager.getInstance().getHomesCache().invalidateCache(uuid);

        MessageManager.sendMessage(player, Language.HOME_ADMIN_FREE_HOME_SET_SUCCESS, "name", PlayerNameUtils.getPlayerNameFromUUID(uuid), "amount", String.valueOf(amount));
    }

    private void handleGet(CommandSender executor, UUID uuid, String[] args) {
        if (args.length > 3) {
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin " + getName() + " <add/get/remove/set> <player> <amount> (worldgroup)");
            return;
        }

        WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup("global");
        if (Bukkit.getPlayer(uuid) != null) {
            worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(Bukkit.getPlayer(uuid).getWorld());
        } else if (executor instanceof Player) {
            worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(((Player) executor).getWorld());
        }

        //Validate and get WorldGroup optional arg
        if (args.length == 3) {
            worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(args[2]);
            if (worldGroup == null) {
                MessageManager.sendMessage(executor, Language.UNKNOWN_GROUP, "group", args[2]);
                return;
            }
        }

        DatabaseAccessor session = DatabaseAccessor.openSession();
        int amount = session.getFreeHomes(uuid, worldGroup.getName());
        session.destroy();

        MessageManager.sendMessage(executor, Language.HOME_ADMIN_FREE_HOME_GET_SUCCESS, "player", PlayerNameUtils.getPlayerNameFromUUID(uuid), "amount", String.valueOf(amount), "group", worldGroup.getName());
    }

    private boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender executor, String[] args) {
        if (args.length == 1) {
            return Stream.of("add", "get", "remove", "set").filter(f -> f.toLowerCase().startsWith(args[0].toLowerCase())).sorted().toList();
        }

        if (args.length == 2) {
            if (!List.of("add", "remove", "set", "get").contains(args[0].toLowerCase())) return List.of();
            return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(f -> f.toLowerCase().startsWith(args[1].toLowerCase())).sorted().toList();
        }

        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (player == null) return List.of();

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("get"))
                return HomeManager.getInstance().getWorldGroupManager().getWorldGroupNames().stream().filter(f -> f.toLowerCase().startsWith(args[2].toLowerCase())).toList();

            if (!List.of("add", "remove", "set").contains(args[0].toLowerCase())) return List.of();
            return List.of("<amount>");
        }

        if (args.length == 4) {
            if (!List.of("add", "remove", "set").contains(args[0].toLowerCase())) return List.of();

            return HomeManager.getInstance().getWorldGroupManager().getWorldGroupNames().stream().filter(f -> f.toLowerCase().startsWith(args[3].toLowerCase())).toList();
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "freehomes";
    }
}
