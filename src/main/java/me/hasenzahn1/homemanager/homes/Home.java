package me.hasenzahn1.homemanager.homes;

import me.hasenzahn1.homemanager.util.PlayerNameUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public record Home(UUID uuid, String name, Location location) {

    /**
     * Teleport a player to this home
     *
     * @param player The player to teleport
     */
    public void teleport(Player player) {
        if (!location.isWorldLoaded()) return;
        player.teleport(location);
    }

    /**
     * Checks whether this home is obstructed by a collidable block.
     * <p>
     * The method considers a home obstructed if either the block at the teleportation
     * location or the block directly above it is collidable (i.e., would prevent a player from standing there).
     * <p>
     * Note: This method does <b>not</b> guarantee the location is safe (e.g., the player might still take damage).
     * It only evaluates block collision and does <b>load the chunk</b> the home is in if it is not already loaded.
     *
     * @return {@code true} if the home location is obstructed by a collidable block; {@code false} otherwise.
     */
    public boolean isObstructed() {
        if (!location.isChunkLoaded()) location.getChunk().load();

        if (location.getBlock().isCollidable()) return true;
        if (location.clone().add(0, 1, 0).getBlock().isCollidable()) return true;

        return false;
    }

    /**
     * Retrieves the name of the player who owns this home.
     * <p>
     * The name is resolved from the player's UUID using {@link PlayerNameUtils#getPlayerNameFromUUID(UUID)}.
     * If the player name cannot be resolved (e.g., offline and unknown), this may return {@code null} or a placeholder.
     *
     * @return The name of the home owner, or {@code null} if it cannot be resolved.
     */
    public String getOwnersName() {
        return PlayerNameUtils.getPlayerNameFromUUID(uuid);
    }

}
