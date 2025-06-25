package me.hasenzahn1.homemanager.homes.teleportation;

import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.homes.Home;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manages teleportation requests and operations for players in the HomeManager plugin.
 * Handles creation, cancellation, and removal of teleportations, ensuring that only one teleportation request
 * is active at a time per player.
 */
public class TeleportationManager {

    // Singleton instance of the TeleportationManager
    private static TeleportationManager instance;

    // A map to store active teleportations for players, keyed by their UUID
    private final HashMap<UUID, PlayerTeleportation> teleportations;

    /**
     * Private constructor to prevent instantiation from other classes.
     * Initializes the teleportations map.
     */
    private TeleportationManager() {
        teleportations = new HashMap<>();
    }

    /**
     * Creates and starts a new home teleportation request for a player.
     * If the player already has an active teleportation, it will be canceled before creating the new one.
     *
     * @param arguments  The arguments provided by the player initiating the teleportation.
     * @param home       The home the player is teleporting to.
     * @param delay      The delay in seconds before teleportation.
     * @param experience The experience cost for the teleportation.
     */
    public void createHomeTeleportation(PlayerNameArguments arguments, Home home, int delay, int experience) {
        // Cancel any existing teleportation for the player
        if (teleportations.containsKey(arguments.getCmdSender().getUniqueId())) {
            cancelTeleportation(arguments.getCmdSender().getUniqueId());
        }

        // Create and start a new teleportation request
        PlayerTeleportation teleportation = new PlayerTeleportation(arguments, home, experience);
        teleportations.put(arguments.getCmdSender().getUniqueId(), teleportation);
        teleportation.startTeleportation(delay);
    }

    /**
     * Removes a teleportation request for a player.
     *
     * @param player The player whose teleportation request is to be removed.
     */
    public void removeTeleportation(Player player) {
        teleportations.remove(player.getUniqueId());
    }

    /**
     * Cancels the teleportation for the player with the given UUID.
     *
     * @param uuid The unique identifier (UUID) of the player whose teleportation is to be canceled.
     */
    public void cancelTeleportation(UUID uuid) {
        if (!teleportations.containsKey(uuid)) return;
        teleportations.get(uuid).cancel();
    }

    /**
     * Retrieves the singleton instance of the TeleportationManager.
     * Creates the instance if it doesn't exist yet.
     *
     * @return The singleton instance of TeleportationManager.
     */
    public static TeleportationManager getInstance() {
        if (instance == null) {
            instance = new TeleportationManager();
        }
        return instance;
    }
}
