package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.config.GroupConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages world groups, which are collections of worlds in the game.
 * This class provides functionality to load, reload, and retrieve world groups by name or world.
 */
public class WorldGroupManager {

    public static final String GLOBAL_GROUP = "global";

    private final HashMap<String, WorldGroup> worldGroupsByName;
    private final HashMap<World, WorldGroup> worldGroupsByWorld;

    private final File groupsFolder;

    /**
     * Initializes the WorldGroupManager, setting up the necessary folder for world group data and
     * loading the world groups from disk.
     * WorldGroups are loaded with a delay of 2 ticks as we have to wait for all worlds to be loaded
     */
    public WorldGroupManager() {
        groupsFolder = new File(HomeManager.getInstance().getDataFolder(), "/groups/");
        if (!groupsFolder.exists()) groupsFolder.mkdirs();

        if (HomeManager.DEV_MODE) {
            new File(groupsFolder, "global.yml").delete();
        }

        worldGroupsByName = new HashMap<>();
        worldGroupsByWorld = new HashMap<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                loadFromDisk();
            }
        }.runTaskLater(HomeManager.getInstance(), 2L);
    }

    /**
     * Loads the world groups from disk and maps them to both their respective world names and world group names.
     * This method also registers the "global" world group for worlds that are not explicitly in any group.
     */
    private void loadFromDisk() {
        worldGroupsByWorld.clear();
        worldGroupsByName.clear();

        if (!new File(groupsFolder, "global.yml").exists()) {
            HomeManager.getInstance().saveResource("groups/global.yml", false);
        }

        for (File file : groupsFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                GroupConfig groupConfig = new GroupConfig(file);
                loadGroupsFromConfig(groupConfig);
            }
        }

        // Get Global world group
        WorldGroup globalGroup = worldGroupsByName.get(GLOBAL_GROUP);
        if (globalGroup == null) globalGroup = new WorldGroup(GLOBAL_GROUP);

        // Find all worlds that are not in a group
        for (World world : Bukkit.getWorlds()) {
            if (!worldGroupsByWorld.containsKey(world)) {
                globalGroup.getWorlds().add(world);
                worldGroupsByWorld.put(world, globalGroup);
                Logger.DEBUG.log("Registered world " + world.getName() + " for group " + globalGroup.getName());
            }
        }

        worldGroupsByName.put(GLOBAL_GROUP, globalGroup);
    }

    /**
     * Reloads all world groups from disk, clearing existing data and reloading them.
     */
    public void reloadFromDisk() {
        Logger.DEBUG.log("Reloading groups from disk.");
        loadFromDisk();
    }

    /**
     * Loads world groups from the provided configuration and adds them to the manager.
     *
     * @param groupConfig The configuration to load the world groups from.
     */
    private void loadGroupsFromConfig(GroupConfig groupConfig) {
        // Load Worlds from config
        HashMap<String, WorldGroup> worldGroups = groupConfig.loadWorldGroups(this);
        worldGroupsByName.putAll(worldGroups);

        // Map worlds to their respective world group for easier accessing later
        for (WorldGroup worldGroup : worldGroups.values()) {
            loadWorldGroup(worldGroup);
        }
    }

    /**
     * Registers the worlds in the given world group to the manager skipping duplicate group definitions.
     *
     * @param worldGroup The world group to register worlds from.
     */
    private void loadWorldGroup(WorldGroup worldGroup) {
        for (World world : worldGroup.getWorlds()) {
            if (worldGroupsByWorld.containsKey(world)) {
                Logger.ERROR.log("Duplicate world " + world.getName() + " in worldgroups. Skipping world for worldgroup " + worldGroup.getName());
                continue;
            }

            worldGroupsByWorld.put(world, worldGroup);
            Logger.DEBUG.log("Registered world " + world.getName() + " for group " + worldGroup.getName());
        }
    }

    /**
     * Retrieves the world group associated with a given world.
     * If the world is not part of any group, it returns the global world group.
     *
     * @param world The world whose associated world group is to be retrieved.
     * @return The world group for the specified world.
     */
    public WorldGroup getWorldGroup(World world) {
        if (worldGroupsByWorld.containsKey(world)) {
            return worldGroupsByWorld.get(world);
        } else {
            return worldGroupsByName.get(GLOBAL_GROUP);
        }
    }

    /**
     * Retrieves the world group associated with the given name.
     * If the name does not correspond to any world group, it returns the global world group.
     *
     * @param name The name of the world group to retrieve.
     * @return The world group corresponding to the specified name.
     */
    public WorldGroup getWorldGroup(String name) {
        if (worldGroupsByName.containsKey(name)) {
            return worldGroupsByName.get(name);
        } else {
            return worldGroupsByName.get(GLOBAL_GROUP);
        }
    }

    /**
     * Checks if a world group with the specified name exists.
     *
     * @param name The name of the world group to check.
     * @return {@code true} if the world group exists, {@code false} otherwise.
     */
    public boolean groupExists(String name) {
        return worldGroupsByName.containsKey(name);
    }

    /**
     * Checks if a world is part of any world group.
     *
     * @param world The world to check.
     * @return {@code true} if the world is part of any group, {@code false} otherwise.
     */
    public boolean groupExists(World world) {
        return worldGroupsByWorld.containsKey(world);
    }

    /**
     * Retrieves a list of all world group names.
     *
     * @return A list of world group names.
     */
    public List<String> getWorldGroupNames() {
        return new ArrayList<>(worldGroupsByName.keySet());
    }

    /**
     * Retrieves the world group associated with the given name, or returns the default group if not found.
     *
     * @param name         The name of the world group.
     * @param defaultGroup The default world group to return if the specified group is not found.
     * @return The world group corresponding to the name, or the default group if not found.
     */
    public WorldGroup getOrDefault(String name, WorldGroup defaultGroup) {
        return worldGroupsByName.getOrDefault(name, defaultGroup);
    }
}
