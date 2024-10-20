package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.util.ExpressionEvaluator;
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
    private final List<Integer> experienceTable;

    public WorldGroup(String name, ConfigurationSection section) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();

        for (String worldName : section.getStringList("worlds")) {
            if (Bukkit.getWorld(worldName) != null)
                worlds.add(Bukkit.getWorld(worldName));
        }

        setHomeExperienceForEachHome = section.getBoolean("sethome.experienceForEveryHome", false);
        setHomeExperienceFormula = section.getString("sethome.experienceFormula", "");
        experienceTable = section.getIntegerList("sethome.experienceTable");
        setHomeRequiresExperience = section.getBoolean("sethome.requiresExperience", false);
    }

    //Defines the basic Global Region
    public WorldGroup(String name) {
        this.name = name.toLowerCase();
        this.worlds = new ArrayList<>();

        setHomeRequiresExperience = false;
        setHomeExperienceForEachHome = false;
        setHomeExperienceFormula = "";
        experienceTable = new ArrayList<>();
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

    public int getRequiredExperience(int currentHomes) {
        if (experienceTable.size() > currentHomes) return experienceTable.get(currentHomes);
        if (!setHomeExperienceFormula.isEmpty())
            return (int) ExpressionEvaluator.eval(setHomeExperienceFormula.replace("amount", String.valueOf(currentHomes)));
        if (!experienceTable.isEmpty()) return experienceTable.get(experienceTable.size() - 1);
        return 0;
    }
}
