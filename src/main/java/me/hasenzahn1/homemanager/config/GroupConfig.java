package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GroupConfig extends CustomConfig{

    public GroupConfig() {
        super(HomeManager.getInstance(), "groups.yml");
    }

    /**
     * Loads worldGroups from the config.
     * For every loaded Worldgroup the relevant metadata is loaded.
     * All Bukkit worlds that are not in any Worldgroup will be put insideof the global group
     * @return Map of loaded worldgroups with the global group
     */
    public HashMap<String, WorldGroup> loadWorldGroups(){
        FileConfiguration config = getConfig();
        Set<String> worldNames = config.getKeys(false);
        HashMap<String, WorldGroup> worldGroups = new HashMap<>();

        //Load WorldGroups from config
        List<World> groupedWorlds = new ArrayList<>();
        for(String worldName : worldNames){
            WorldGroup worldGroup = new WorldGroup(worldName, config.getConfigurationSection(worldName));
            worldGroups.put(worldGroup.getName(), worldGroup);
            groupedWorlds.addAll(worldGroup.getWorlds());
        }

        //Find all worlds that are not in a group
        List<World> nonGroupedWorlds = Bukkit.getWorlds();
        nonGroupedWorlds.removeAll(groupedWorlds);

        //Create default global group
        WorldGroup globalGroup = worldGroups.getOrDefault("global", new WorldGroup("global"));
        globalGroup.getWorlds().addAll(nonGroupedWorlds);
        worldGroups.put("global", globalGroup);
        return worldGroups;
    }
}
