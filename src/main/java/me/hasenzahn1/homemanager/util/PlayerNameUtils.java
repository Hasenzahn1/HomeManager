package me.hasenzahn1.homemanager.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * A utility class that provides methods for converting between player names and UUIDs.
 */
public class PlayerNameUtils {

    /**
     * Retrieves the UUID corresponding to a given player name or UUID string.
     * <p>
     * If the provided string is a valid UUID, it is directly parsed. If it is not, the method attempts to find
     * the UUID of the player with the given name.
     *
     * @param arg the player name or UUID string
     * @return the corresponding UUID, or null if the player could not be found or the input is invalid
     */
    public static UUID getUUIDFromString(String arg) {
        if (arg.isEmpty()) return null;

        // Try to parse the string as a UUID
        UUID fromUUID = tryParseUUID(arg);
        if (fromUUID != null) return fromUUID;

        // If it's not a UUID, attempt to find the offline player by name
        OfflinePlayer fromName = Bukkit.getOfflinePlayerIfCached(arg);
        if (fromName != null) return fromName.getUniqueId();

        return null;
    }

    /**
     * Tries to parse a string as a UUID.
     *
     * @param arg the string to parse
     * @return the parsed UUID if valid, or null if the string is not a valid UUID
     */
    private static UUID tryParseUUID(String arg) {
        try {
            return UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Retrieves the player name corresponding to a given UUID.
     * <p>
     * If the player is offline or has no name, the UUID string will be returned instead.
     *
     * @param uuid the UUID of the player
     * @return the player's name if available, otherwise the UUID as a string
     */
    public static String getPlayerNameFromUUID(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() == null ? uuid.toString() : player.getName();
    }

}
