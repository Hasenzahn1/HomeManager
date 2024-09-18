package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.config.GroupConfig;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;

public class WorldGroupManager {

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

    public HashMap<World, WorldGroup> groupsByWorld() {
        return worldGroupsByWorld;
    }

    public HashMap<String, WorldGroup> groupsByName() {
        return worldGroupsByName;
    }
}
