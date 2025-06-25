package me.hasenzahn1.homemanager.group;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of worlds in the game, with associated settings and a list of worlds.
 * This class provides functionality to manage worlds and their related settings within a world group.
 */
public class WorldGroup {

    private final String name;
    private final List<World> worlds;
    private final WorldGroupSettings settings;

    /**
     * Constructs a WorldGroup from the given name and configuration section.
     * The worlds listed in the configuration are added to the group if they exist in the game.
     *
     * @param name    The name of the world group.
     * @param section The configuration section containing the worlds and settings for the group.
     */
    public WorldGroup(String name, ConfigurationSection section) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();

        for (String worldName : section.getStringList("worlds")) {
            if (Bukkit.getWorld(worldName) != null)
                worlds.add(Bukkit.getWorld(worldName));
        }

        settings = new WorldGroupSettings(section, this);
    }

    /**
     * Constructs a WorldGroup with a given name and default settings.
     * The group is initialized without any specific worlds, and the default settings are applied.
     * This represents the Global WorldGroup
     *
     * @param name The name of the world group.
     */
    public WorldGroup(String name) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();

        settings = WorldGroupSettings.DEFAULT;
        settings.setParentWorldGroup(this);
    }

    /**
     * Retrieves the name of the world group.
     *
     * @return The name of the world group.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the list of worlds associated with this world group.
     *
     * @return A list of {@link World} objects that belong to this world group.
     */
    public List<World> getWorlds() {
        return worlds;
    }

    /**
     * Checks whether a given location is in one of the worlds of this world group.
     *
     * @param location The location to check.
     * @return {@code true} if the location is in one of the worlds of the group, {@code false} otherwise.
     */
    public boolean isInWorldGroup(Location location) {
        return worlds.contains(location.getWorld());
    }

    /**
     * Retrieves the settings associated with this world group.
     *
     * @return The {@link WorldGroupSettings} object associated with this group.
     */
    public WorldGroupSettings getSettings() {
        return settings;
    }
}

