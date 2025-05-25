package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.group.WorldGroupManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GroupConfig extends CustomConfig {

    public GroupConfig() {
        super(HomeManager.getInstance(), "groups.yml");
    }

    /**
     * Loads worldGroups from the config.
     * For every loaded Worldgroup the relevant metadata is loaded.
     * All Bukkit worlds that are not in any Worldgroup will be put insideof the global group
     *
     * @return Map of loaded worldgroups with the global group
     */
    public HashMap<String, WorldGroup> loadWorldGroups() {
        FileConfiguration config = getConfig();
        Set<String> worldGroupNames = config.getKeys(false);
        HashMap<String, WorldGroup> worldGroups = new HashMap<>();

        //Load WorldGroups from config
        List<World> groupedWorlds = new ArrayList<>();
        for (String groupName : worldGroupNames) {
            WorldGroup worldGroup = new WorldGroup(groupName, config.getConfigurationSection(groupName));
            worldGroups.put(worldGroup.getName(), worldGroup);
            groupedWorlds.addAll(worldGroup.getWorlds());
            Logger.DEBUG.log("Successfully loaded world group " + worldGroup.getName());
        }

        //Find all worlds that are not in a group
        List<World> nonGroupedWorlds = Bukkit.getWorlds();
        nonGroupedWorlds.removeAll(groupedWorlds);

        //Create default global group
        WorldGroup globalGroup = worldGroups.getOrDefault(WorldGroupManager.GLOBAL_GROUP, new WorldGroup(WorldGroupManager.GLOBAL_GROUP));
        globalGroup.getWorlds().addAll(nonGroupedWorlds);
        worldGroups.put(WorldGroupManager.GLOBAL_GROUP, globalGroup);
        return worldGroups;
    }
}
