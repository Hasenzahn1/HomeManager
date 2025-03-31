package me.hasenzahn1.homemanager.papi;

import it.unimi.dsi.fastutil.Pair;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.util.PermissionUtils;
import me.hasenzahn1.homemanager.util.PlayerNameUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlaceholderHomeExpansion extends PlaceholderExpansion {

    public static DatabaseAccessor SESSION;

    private final HomeManager plugin;

    public PlaceholderHomeExpansion() {
        plugin = HomeManager.getInstance();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "homemanager";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hasenzahn1";
    }

    @Override
    public @NotNull String getVersion() {
        return HomeManager.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return List.of("%homemanager_homes_<worldgroup-or-'this'>_<player-or-'self'>%",
                "%homemanager_freehomes_<worldgroup-or-'this'>_<player-or-'self'>%",
                "%homemanager_maxhomes_<worldgroup-or-'this'>_<player-or-'self'>%",
                "%homemanager_nexthomexp_<worldgroup-or-'this'>_<player-or-'self'>%",
                "%homemanager_currentgroup%");
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer requester, @NotNull String params) {
        if (requester == null) return null;

        String[] args = params.split("_");

        if (args[0].equalsIgnoreCase("homes")) {
            return handleHomeCountPlaceholder(requester, args);
        }

        if (args[0].equalsIgnoreCase("freehomes")) {
            return handleFreeHomesCountPlaceholder(requester, args);
        }

        if (args[0].equalsIgnoreCase("maxhomes")) {
            return handleMaxHomesPlaceholder(requester, args);
        }

        if (args[0].equalsIgnoreCase("nexthomexp")) {
            return handleNextExperiencePlaceholder(requester, args);
        }


        if (args[0].equalsIgnoreCase("currentgroup")) {
            if (args.length != 1) return null;
            if (!(requester instanceof Player)) return "";
            return plugin.getWorldGroupManager().getWorldGroup(((Player) requester).getWorld()).getName();
        }


        return null;
    }

    private Pair<UUID, WorldGroup> parseArgs(OfflinePlayer requester, String[] args) {
        if (args.length < 2) return Pair.of(null, null);
        String player = args[args.length - 1];
        String worldGroupName = String.join("_", Arrays.copyOfRange(args, 1, args.length - 1));

        UUID playerUUID;
        if (player.equalsIgnoreCase("self")) playerUUID = requester.getUniqueId();
        else playerUUID = PlayerNameUtils.getUUIDFromString(player);

        WorldGroup worldGroup;
        if (worldGroupName.equalsIgnoreCase("this")) {
            if (!(requester instanceof Player)) {
                worldGroup = null;
            } else worldGroup = plugin.getWorldGroupManager().getWorldGroup(((Player) requester).getWorld());
        } else worldGroup = plugin.getWorldGroupManager().getWorldGroup(worldGroupName);

        return Pair.of(playerUUID, worldGroup);
    }

    private String handleNextExperiencePlaceholder(OfflinePlayer requester, String[] args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "";

        DatabaseAccessor session = getSession();
        int homeCount = session.getHomeCountFromPlayer(parsedArgs.key(), parsedArgs.value().getName());
        return String.valueOf(parsedArgs.value().getSettings().getRequiredExperience(homeCount));
    }

    private String handleMaxHomesPlaceholder(OfflinePlayer requester, String[] args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "";

        Player p = Bukkit.getOfflinePlayer(parsedArgs.key()).getPlayer();
        if (p == null) return "";

        return String.valueOf(PermissionUtils.getMaxHomesFromPermission(p, parsedArgs.value().getName()));
    }

    private String handleHomeCountPlaceholder(OfflinePlayer requester, String[] args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "";

        DatabaseAccessor session = getSession();
        return String.valueOf(session.getHomeCountFromPlayer(parsedArgs.key(), parsedArgs.value().getName()));
    }

    private String handleFreeHomesCountPlaceholder(OfflinePlayer requester, String[] args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "";

        DatabaseAccessor session = getSession();
        return String.valueOf(session.getFreeHomes(parsedArgs.key(), parsedArgs.value().getName()));
    }

    public static DatabaseAccessor getSession() {
        if (SESSION == null) SESSION = DatabaseAccessor.openSession();
        return SESSION;
    }

    public static void closeSession() {
        if (SESSION == null) return;
        SESSION.destroy();
        SESSION = null;
    }
}
