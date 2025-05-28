package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.group.WorldGroupManager;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GroupConfig extends CustomConfig {

    public GroupConfig(File file) {
        super(HomeManager.getInstance(), file);
    }

    /**
     * Loads worldGroups from the config.
     * For every loaded Worldgroup the relevant metadata is loaded.
     * All Bukkit worlds that are not in any Worldgroup will be put insideof the global group
     *
     * @return Map of loaded worldgroups with the global group
     */
    public HashMap<String, WorldGroup> loadWorldGroups(WorldGroupManager worldGroupManager) {
        FileConfiguration config = getConfig();
        Set<String> worldGroupNames = config.getKeys(false);
        HashMap<String, WorldGroup> worldGroups = new HashMap<>();

        //Load WorldGroups from config
        List<World> groupedWorlds = new ArrayList<>();
        for (String groupName : worldGroupNames) {
            if (worldGroups.containsKey(groupName) || worldGroupManager.groupExists(groupName)) {
                Logger.DEBUG.log("Skipping worldgroup " + groupName + " as it already exists");
                continue;
            }
            WorldGroup worldGroup = new WorldGroup(groupName, config.getConfigurationSection(groupName));
            worldGroups.put(worldGroup.getName(), worldGroup);

            //Remove duplicate Worlds
            worldGroup.getWorlds().removeIf(world -> groupedWorlds.contains(world) || worldGroupManager.groupExists(world));

            //Add Worlds to groupWorlds
            groupedWorlds.addAll(worldGroup.getWorlds());
            Logger.DEBUG.log("Successfully loaded world group " + worldGroup.getName());
        }
        return worldGroups;
    }
}
