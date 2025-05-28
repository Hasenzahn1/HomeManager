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

public class WorldGroupManager {

    public static final String GLOBAL_GROUP = "global";

    private final HashMap<String, WorldGroup> worldGroupsByName;
    private final HashMap<World, WorldGroup> worldGroupsByWorld;

    private final File groupsFolder;

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
        }.runTaskLater(HomeManager.getInstance(), 20L);
    }

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

        //Get Global worldgroup
        WorldGroup globalGroup = worldGroupsByName.get(GLOBAL_GROUP);
        if (globalGroup == null) globalGroup = new WorldGroup(GLOBAL_GROUP);

        //Find all worlds that are not in a group
        for (World world : Bukkit.getWorlds()) {
            if (!worldGroupsByWorld.containsKey(world)) {
                globalGroup.getWorlds().add(world);
                worldGroupsByWorld.put(world, globalGroup);
                Logger.DEBUG.log("Registered world " + world.getName() + " for group " + globalGroup.getName());
            }
        }

        worldGroupsByName.put(GLOBAL_GROUP, globalGroup);
    }

    public void reloadFromDisk() {
        Logger.DEBUG.log("Reloading groups from disk.");
        loadFromDisk();
    }

    private void loadGroupsFromConfig(GroupConfig groupConfig) {
        //Load Worlds from config
        HashMap<String, WorldGroup> worldGroups = groupConfig.loadWorldGroups(this);
        worldGroupsByName.putAll(worldGroups);

        //Map worlds to their respective worldGroup for easier accessing later
        for (WorldGroup worldGroup : worldGroups.values()) {
            loadWorldGroup(worldGroup);
        }
    }

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
