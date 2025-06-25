package me.hasenzahn1.homemanager.homes;

import me.hasenzahn1.homemanager.group.WorldGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the collection of homes owned by a specific player.
 * <p>
 * This class manages all homes for a player, allowing operations such as
 * retrieving, or filtering homes. Home names are case-insensitive.
 */
public class PlayerHomes {

    private final HashMap<String, Home> playerHomes;

    /**
     * Constructs a new PlayerHomes instance with the given map of homes.
     *
     * @param playerHomes a map of home names (lowercase) to {@link Home} objects
     */
    public PlayerHomes(HashMap<String, Home> playerHomes) {
        this.playerHomes = playerHomes;
    }

    /**
     * Checks if a home with the given name exists.
     *
     * @param name the name of the home (case-insensitive)
     * @return true if the home exists, false otherwise
     */
    public boolean homeExists(String name) {
        return playerHomes.containsKey(name.toLowerCase());
    }

    /**
     * Retrieves the {@link Home} with the given name.
     *
     * @param name the name of the home (case-insensitive)
     * @return the {@link Home} object, or null if not found
     */
    public Home getHome(String name) {
        return playerHomes.get(name.toLowerCase());
    }

    /**
     * Checks if the player has at least one home.
     *
     * @return true if the player has any homes, false if none
     */
    public boolean hasHomes() {
        return !playerHomes.isEmpty();
    }

    /**
     * Gets a list of all homes the player owns.
     *
     * @return a list of all {@link Home} objects
     */
    public List<Home> getHomes() {
        return new ArrayList<>(playerHomes.values());
    }

    /**
     * Gets the total number of homes the player owns.
     *
     * @return the number of homes
     */
    public int getHomeAmount() {
        return playerHomes.size();
    }

    /**
     * Gets all homes that are located within the specified world group.
     *
     * @param group the {@link WorldGroup} to filter by
     * @return a list of {@link Home} objects in the given world group
     */
    public List<Home> getHomesInWorldGroup(WorldGroup group) {
        return playerHomes.values().stream()
                .filter(home -> group.isInWorldGroup(home.location()))
                .toList();
    }
}
