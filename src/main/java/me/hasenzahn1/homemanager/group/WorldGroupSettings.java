package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.util.ExpressionEvaluator;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Objects;

public class WorldGroupSettings {

    public static WorldGroupSettings DEFAULT = new WorldGroupSettings();

    //new
    private boolean setHomeExperienceActive = false;
    private String setHomeExperienceFormula = "";
    private List<Integer> setHomeExperiencePerHome = List.of();

    private boolean freeHomesActive = true;

    private boolean homeTeleportExperienceActive = false;
    private String homeTeleportExperienceFormula = "";

    private boolean delayActive = false;
    private boolean delayDisableInCreative = true;
    private int delayDurationInSeconds = 5;
    private List<EntityDamageEvent.DamageCause> delayInterruptCauses = List.of();

    private boolean timeoutActive = false;
    private int timeoutDurationInSeconds = 5;
    private List<EntityDamageEvent.DamageCause> timeoutCauses = List.of();

    private boolean obstructedHomeCheckActive = false;
    private boolean obstructedHomeCheckDisableInCreative = true;
    private int obstructedHomeCheckRetryDurationInSeconds = 5;

    private boolean homeTeleportOnGroundCheckActive = false;
    private boolean homeTeleportOnGroundCheckDisableInCreative = true;


    public WorldGroupSettings(ConfigurationSection section) {
        setHomeExperienceActive = section.getBoolean("setHomeExperience.active", setHomeExperienceActive);
        setHomeExperienceFormula = section.getString("setHomeExperience.experienceFormula", setHomeExperienceFormula);
        setHomeExperiencePerHome = section.getIntegerList("setHomeExperience.experiencePerHome");

        freeHomesActive = section.getBoolean("freeHomes.active", freeHomesActive);

        homeTeleportExperienceActive = section.getBoolean("homeTeleportExperience.active", homeTeleportExperienceActive);
        homeTeleportExperienceFormula = section.getString("homeTeleportExperience.formula", homeTeleportExperienceFormula);

        delayActive = section.getBoolean("delay.active", delayActive);
        delayDisableInCreative = section.getBoolean("delay.disableInCreative", delayDisableInCreative);
        delayDurationInSeconds = section.getInt("delay.duration", delayDurationInSeconds);
        delayInterruptCauses = section.getStringList("delay.interruptCauses").stream().map(c -> {
            try {
                return EntityDamageEvent.DamageCause.valueOf(c);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();

        timeoutActive = section.getBoolean("timeout.active", timeoutActive);
        timeoutDurationInSeconds = section.getInt("timeout.duration", timeoutDurationInSeconds);
        timeoutCauses = section.getStringList("timeout.causes").stream().map(c -> {
            try {
                return EntityDamageEvent.DamageCause.valueOf(c);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();

        obstructedHomeCheckActive = section.getBoolean("obstructedHomeCheck.active", obstructedHomeCheckActive);
        obstructedHomeCheckDisableInCreative = section.getBoolean("obstructedHomeCheck.disableInCreative", obstructedHomeCheckDisableInCreative);
        obstructedHomeCheckRetryDurationInSeconds = section.getInt("obstructedHomeCheck.retryDuration", obstructedHomeCheckRetryDurationInSeconds);

        homeTeleportOnGroundCheckActive = section.getBoolean("homeTeleportOnGroundCheck.active", homeTeleportOnGroundCheckActive);
        homeTeleportOnGroundCheckDisableInCreative = section.getBoolean("homeTeleportOnGroundCheck.disableInCreative", homeTeleportOnGroundCheckDisableInCreative);

    }


    private WorldGroupSettings() {
    }

    public int getRequiredExperience(int currentHomes) {
        if (setHomeExperiencePerHome.size() > currentHomes) return setHomeExperiencePerHome.get(currentHomes);
        if (!setHomeExperienceFormula.isEmpty())
            return (int) ExpressionEvaluator.eval(setHomeExperienceFormula.replace("amount", String.valueOf(currentHomes)));
        if (!setHomeExperiencePerHome.isEmpty())
            return setHomeExperiencePerHome.get(setHomeExperiencePerHome.size() - 1);
        return 0;
    }

    public boolean isSetHomeExperienceActive() {
        return setHomeExperienceActive;
    }

    public boolean isFreeHomesActive() {
        return freeHomesActive;
    }

    public boolean isHomeTeleportExperienceActive() {
        return homeTeleportExperienceActive;
    }

    public int getHomeTeleportExperience(Location start, Location end) {
        if (homeTeleportExperienceFormula.isEmpty()) return 0;
        double dist = start.toVector().distance(end.toVector());
        boolean world = start.getWorld().equals(end.getWorld());
        return (int) ExpressionEvaluator.eval(homeTeleportExperienceFormula.replace("dist", String.valueOf(dist)).replace("worldChange", String.valueOf(world ? 1 : 0)));
    }

    public boolean isDelayActive() {
        return delayActive;
    }

    public boolean isDelayDisableInCreative() {
        return delayDisableInCreative;
    }

    public int getDelayDurationInSeconds() {
        return delayDurationInSeconds;
    }

    public List<EntityDamageEvent.DamageCause> getDelayInterruptCauses() {
        return delayInterruptCauses;
    }

    public boolean isTimeoutActive() {
        return timeoutActive;
    }

    public int getTimeoutDurationInSeconds() {
        return timeoutDurationInSeconds;
    }

    public List<EntityDamageEvent.DamageCause> getTimoutCauses() {
        return timeoutCauses;
    }

    public boolean isHomeTeleportOnGroundCheckActive() {
        return homeTeleportOnGroundCheckActive;
    }

    public boolean isObstructedHomeCheckActive() {
        return obstructedHomeCheckActive;
    }

    public boolean isObstructedHomeCheckDisableInCreative() {
        return obstructedHomeCheckDisableInCreative;
    }

    public int getObstructedHomeCheckRetryDurationInSeconds() {
        return obstructedHomeCheckRetryDurationInSeconds;
    }

    public boolean isHomeTeleportOnGroundCheckDisableInCreative() {
        return homeTeleportOnGroundCheckDisableInCreative;
    }
}
