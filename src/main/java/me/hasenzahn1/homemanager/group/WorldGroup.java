package me.hasenzahn1.homemanager.group;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class WorldGroup {

    private final String name;
    private final List<World> worlds;

    public WorldGroup(String name, ConfigurationSection section) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();

        for (String worldName : section.getStringList("worlds")){
            if(Bukkit.getWorld(worldName) != null)
                worlds.add(Bukkit.getWorld(worldName));
        }
    }

    public WorldGroup(String name) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<World> getWorlds() {
        return worlds;
    }
}
