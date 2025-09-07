package me.hasenzahn1.homemanager.papi;

import it.unimi.dsi.fastutil.Pair;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.util.PlayerNameUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * PlaceholderAPI expansion for the HomeManager plugin.
 * Provides support for home-related placeholders such as home counts, limits, etc.
 */
public class PlaceholderHomeExpansion extends PlaceholderExpansion {

    private final HomeManager plugin;

    /**
     * Constructs the PlaceholderHomeExpansion using the HomeManager instance.
     */
    public PlaceholderHomeExpansion() {
        plugin = HomeManager.getInstance();
    }

    /**
     * Returns the identifier used in the placeholder (e.g., %homemanager_<placeholder>%).
     */
    @Override
    public @NotNull String getIdentifier() {
        return "homemanager";
    }

    /**
     * Returns the author of this expansion.
     */
    @Override
    public @NotNull String getAuthor() {
        return "Hasenzahn1";
    }

    /**
     * Returns the version of this expansion (same as plugin version).
     */
    @Override
    public @NotNull String getVersion() {
        return HomeManager.getInstance().getDescription().getVersion();
    }

    /**
     * Whether the expansion should persist through PlaceholderAPI reloads.
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Returns a list of supported placeholder formats.
     */
    @Override
    public @NotNull List<String> getPlaceholders() {
        return List.of("%homemanager_homes_<worldgroup or 'this'>:<player or 'self'>%",
                "%homemanager_freehomes_<worldgroup or 'this'>:<player or 'self'>%",
                "%homemanager_maxhomes_<worldgroup or 'this'>:<player or 'self'>%",
                "%homemanager_nexthomexp_<worldgroup or 'this'>:<player or 'self'>%",
                "%homemanager_currentgroup_<player or 'self'>%");
    }

    /**
     * Handles the placeholder request from PlaceholderAPI.
     *
     * @param requester The player requesting the placeholder (may be offline).
     * @param params    The placeholder parameters (after the identifier).
     * @return The resolved value or null if unsupported.
     */
    @Override
    public @Nullable String onRequest(OfflinePlayer requester, @NotNull String params) {
        if (requester == null) return null;

        String[] args = params.split("_");
        String text = String.join("_", Arrays.copyOfRange(args, 1, args.length));

        if (args[0].equalsIgnoreCase("homes")) {
            return handleHomeCountPlaceholder(requester, text);
        }

        if (args[0].equalsIgnoreCase("freehomes")) {
            return handleFreeHomesCountPlaceholder(requester, text);
        }

        if (args[0].equalsIgnoreCase("maxhomes")) {
            return handleMaxHomesPlaceholder(requester, text);
        }

        if (args[0].equalsIgnoreCase("nexthomexp")) {
            return handleNextExperiencePlaceholder(requester, text);
        }

        if (args[0].equalsIgnoreCase("currentgroup")) {
            return handleCurrentGroupPlaceholder(requester, text);
        }

        return null;
    }

    /**
     * Parses the UUID and WorldGroup from the placeholder arguments.
     *
     * @param requester The requesting player.
     * @param args      The placeholder arguments split by '_'.
     * @return A Pair containing the UUID and WorldGroup, or null values on failure.
     */
    private Pair<UUID, WorldGroup> parseArgs(OfflinePlayer requester, String args) {
        String[] parts = args.split(":");
        if (parts.length != 2) return Pair.of(null, null);

        String worldGroupName = parts[0];
        String player = parts[1];

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

    private String handleCurrentGroupPlaceholder(OfflinePlayer requester, String args) {
        UUID playerToCheckUUID;
        String playerName = args;
        if (playerName.equalsIgnoreCase("self")) playerToCheckUUID = requester.getUniqueId();
        else playerToCheckUUID = PlayerNameUtils.getUUIDFromString(playerName);

        if (playerToCheckUUID == null) return null;

        OfflinePlayer playerToCheck = Bukkit.getOfflinePlayer(playerToCheckUUID);
        if (!playerToCheck.isOnline()) return "-";
        if (playerToCheck.getPlayer() == null) return "-";
        return plugin.getWorldGroupManager().getWorldGroup(playerToCheck.getPlayer().getWorld()).getName();
    }

    /**
     * Resolves the %nexthomexp% placeholder.
     */
    private String handleNextExperiencePlaceholder(OfflinePlayer requester, String args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "-";

        int homeCount = plugin.getHomesCache().get(parsedArgs.key()).getHomeCount(parsedArgs.value());
        return String.valueOf(parsedArgs.value().getSettings().getRequiredExperience(homeCount));
    }

    /**
     * Resolves the %maxhomes% placeholder.
     */
    private String handleMaxHomesPlaceholder(OfflinePlayer requester, String args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "-";

        Player p = Bukkit.getOfflinePlayer(parsedArgs.key()).getPlayer();
        if (p == null) return "-";
        return String.valueOf(parsedArgs.value().getSettings().getMaxHomes(p));
    }

    /**
     * Resolves the %homes% placeholder.
     */
    private String handleHomeCountPlaceholder(OfflinePlayer requester, String args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "-";

        return String.valueOf(plugin.getHomesCache().get(parsedArgs.key()).getHomeCount(parsedArgs.value()));
    }

    /**
     * Resolves the %freehomes% placeholder.
     */
    private String handleFreeHomesCountPlaceholder(OfflinePlayer requester, String args) {
        Pair<UUID, WorldGroup> parsedArgs = parseArgs(requester, args);
        if (parsedArgs.key() == null || parsedArgs.value() == null) return "-";

        return String.valueOf(plugin.getHomesCache().get(parsedArgs.key()).getFreeHomes(parsedArgs.value()));
    }
}
