package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.config.GroupConfig;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldGroupManager {

    public static final String GLOBAL_GROUP = "global";

    private final GroupConfig groupConfig;
    private final HashMap<String, WorldGroup> worldGroupsByName;
    private final HashMap<World, WorldGroup> worldGroupsByWorld;

    public WorldGroupManager() {
        new File(HomeManager.getInstance().getDataFolder(), "groups.yml").delete(); //TODO: DEBUG REMOVE
        groupConfig = new GroupConfig();

        worldGroupsByName = new HashMap<>();
        worldGroupsByWorld = new HashMap<>();

        loadGroupsFromConfig();
    }

    public void reloadFromDisk() {
        Logger.DEBUG.log("Reloading groups from disk.");
        groupConfig.reloadConfig();
        loadGroupsFromConfig();
    }

    private void loadGroupsFromConfig() {
        //Load Worlds from config
        worldGroupsByName.clear();
        worldGroupsByName.putAll(groupConfig.loadWorldGroups());

        //Map worlds to their respective worldGroup for easier accessing later
        worldGroupsByWorld.clear();
        for (WorldGroup worldGroup : worldGroupsByName.values()) {
            for (World world : worldGroup.getWorlds()) {
                worldGroupsByWorld.put(world, worldGroup);
            }
        }
    }

    public WorldGroup getWorldGroup(World world) {
        if (worldGroupsByWorld.containsKey(world)) {
            return worldGroupsByWorld.get(world);
        } else {
            return worldGroupsByName.get(GLOBAL_GROUP);
        }
    }

    public WorldGroup getWorldGroup(String name) {
        if (worldGroupsByName.containsKey(name)) {
            return worldGroupsByName.get(name);
        } else {
            return worldGroupsByName.get(GLOBAL_GROUP);
        }
    }

    public boolean groupExists(String name) {
        return worldGroupsByName.containsKey(name);
    }

    public boolean groupExists(World name) {
        return worldGroupsByWorld.containsKey(name);
    }

    public List<String> getWorldGroupNames() {
        return new ArrayList<>(worldGroupsByName.keySet());
    }

    public WorldGroup getOrDefault(String name, WorldGroup defaultGroup) {
        return worldGroupsByName.getOrDefault(name, defaultGroup);
    }
}
