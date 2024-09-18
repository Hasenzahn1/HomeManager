package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.config.GroupConfig;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public final class HomeManager extends JavaPlugin {

    private static HomeManager instance;

    private GroupConfig groupConfig;
    private HashMap<String, WorldGroup> worldGroupsByName;
    private HashMap<World, WorldGroup> worldGroupsByWorld;

    @Override
    public void onEnable() {
        instance = this;

        //Load Groups from Config
        new File(getDataFolder(), "groups.yml").delete();
        groupConfig = new GroupConfig();
        worldGroupsByName = groupConfig.loadWorldGroups();

        //Map worlds to their respective worldGroup
        worldGroupsByWorld = new HashMap<>();
        for (WorldGroup worldGroup : worldGroupsByName.values()) {
            for(World world : worldGroup.getWorlds()) {
                worldGroupsByWorld.put(world, worldGroup);
            }
        }
    }


    @Override
    public void onDisable() {
    }

    public static HomeManager getInstance() {
        return instance;
    }
}
