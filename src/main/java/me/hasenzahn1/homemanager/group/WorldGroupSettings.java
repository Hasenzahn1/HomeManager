package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.util.ExpressionEvaluator;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class WorldGroupSettings {

    private boolean setHomeRequiresExperience;
    private boolean setHomeExperienceForEachHome;
    private String setHomeExperienceFormula;
    private List<Integer> experienceTable;

    public WorldGroupSettings(ConfigurationSection section) {
        if (section == null) {
            initializeDefaultSettings();
        } else {
            parseFromSettings(section);
        }

    }

    private void parseFromSettings(ConfigurationSection section) {
        setHomeExperienceForEachHome = section.getBoolean("sethome.experienceForEveryHome", false);
        setHomeExperienceFormula = section.getString("sethome.experienceFormula", "");
        experienceTable = section.getIntegerList("sethome.experienceTable");
        setHomeRequiresExperience = section.getBoolean("sethome.requiresExperience", false);
    }

    private void initializeDefaultSettings() {
        setHomeRequiresExperience = false;
        setHomeExperienceForEachHome = false;
        setHomeExperienceFormula = "";
        experienceTable = new ArrayList<>();
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
