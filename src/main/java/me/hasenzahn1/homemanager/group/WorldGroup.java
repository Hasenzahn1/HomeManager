package me.hasenzahn1.homemanager.group;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class WorldGroup {

    private final String name;
    private final List<World> worlds;

    //SetHome
    private final boolean setHomeRequiresExperience;
    private final boolean setHomeExperienceForEachHome;
    private final String setHomeExperienceFormula;

    public WorldGroup(String name, ConfigurationSection section) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();

        for (String worldName : section.getStringList("worlds")) {
            if (Bukkit.getWorld(worldName) != null)
                worlds.add(Bukkit.getWorld(worldName));
        }

        setHomeExperienceForEachHome = section.getBoolean("sethome.experienceForEveryHome", false);
        setHomeExperienceFormula = section.getString("sethome.experienceFormula", "");
        setHomeRequiresExperience = section.getBoolean("sethome.requiresExperience", false);
    }

    //Defines the basic Global Region
    public WorldGroup(String name) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();

        setHomeRequiresExperience = false;
        setHomeExperienceForEachHome = false;
        setHomeExperienceFormula = "";
    }

    public String getName() {
        return name;
    }

    public List<World> getWorlds() {
        return worlds;
    }

    public boolean isSetHomeExperienceForEachHome() {
        return setHomeExperienceForEachHome;
    }

    public String getSetHomeExperienceFormula() {
        return setHomeExperienceFormula;
    }

    public boolean isSetHomeRequiresExperience() {
        return setHomeRequiresExperience;
    }
}
