package me.hasenzahn1.homemanager.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlayerNameUtils {


    public static UUID getUUIDFromString(String arg) {
        if (arg.isEmpty()) return null;

        UUID fromUUID = tryParseUUID(arg);
        if (fromUUID != null) return fromUUID;

        OfflinePlayer fromName = Bukkit.getOfflinePlayerIfCached(arg);
        if (fromName != null) return fromName.getUniqueId();

        return null;
    }

    private static UUID tryParseUUID(String arg) {
        try {
            return UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String getPlayerNameFromUUID(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() == null ? uuid.toString() : player.getName();
    }

}
