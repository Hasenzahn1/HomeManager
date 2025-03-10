package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.util.ExpressionEvaluator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Objects;

public class WorldGroupSettings {

    public static WorldGroupSettings DEFAULT = new WorldGroupSettings();

    //new
    private boolean setHomeExperienceActive = false;
    private boolean homeDeletionGrantsFreeHome = true;
    private String experienceFormula = "";
    private List<Integer> experiencePerHome = List.of();

    private boolean homeTeleportExperienceActive = false;
    private int homeTeleportExperienceAmount = 1;

    private boolean delayActive = false;
    private int delayDurationInSeconds = 5;
    private List<EntityDamageEvent.DamageCause> delayInterruptCauses = List.of();

    private boolean timeoutActive = false;
    private int timeoutDurationInSeconds = 5;
    private List<EntityDamageEvent.DamageCause> timeoutCauses = List.of();

    private boolean homeTeleportGroundCheck = false;

    private boolean homeTeleportObstructedHomeCheck = true;
    private int homeTeleportObstructedHomeRetryDuration = 5;


    public WorldGroupSettings(ConfigurationSection section) {
        setHomeExperienceActive = section.getBoolean("setHomeExperience.active", setHomeExperienceActive);
        homeDeletionGrantsFreeHome = section.getBoolean("setHomeExperience.homeDeletionGrantsFreeHome", homeDeletionGrantsFreeHome);
        experienceFormula = section.getString("setHomeExperience.experienceFormula", experienceFormula);
        experiencePerHome = section.getIntegerList("setHomeExperience.experiencePerHome");

        homeTeleportExperienceActive = section.getBoolean("homeTeleportExperience.active", homeTeleportExperienceActive);
        homeTeleportExperienceAmount = section.getInt("homeTeleportExperience.amount", homeTeleportExperienceAmount);

        delayActive = section.getBoolean("delay.active", delayActive);
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

        homeTeleportGroundCheck = section.getBoolean("homeTeleport.groundCheck", homeTeleportGroundCheck);
        homeTeleportObstructedHomeCheck = section.getBoolean("obstructedHomeCheck.active", homeTeleportObstructedHomeCheck);
        homeTeleportObstructedHomeRetryDuration = section.getInt("obstructedHomeCheck.retryDuration", homeTeleportObstructedHomeRetryDuration);
    }


    private WorldGroupSettings() {
    }

    public int getRequiredExperience(int currentHomes) {
        if (experiencePerHome.size() > currentHomes) return experiencePerHome.get(currentHomes);
        if (!experienceFormula.isEmpty())
            return (int) ExpressionEvaluator.eval(experienceFormula.replace("amount", String.valueOf(currentHomes)));
        if (!experiencePerHome.isEmpty()) return experiencePerHome.get(experiencePerHome.size() - 1);
        return 0;
    }

    public boolean isSetHomeExperienceActive() {
        return setHomeExperienceActive;
    }

    public boolean isHomeDeletionGrantsFreeHome() {
        return homeDeletionGrantsFreeHome;
    }

    public String getExperienceFormula() {
        return experienceFormula;
    }

    public List<Integer> getExperiencePerHome() {
        return experiencePerHome;
    }

    public boolean isHomeTeleportExperienceActive() {
        return homeTeleportExperienceActive;
    }

    public int getHomeTeleportExperienceAmount() {
        return homeTeleportExperienceAmount;
    }

    public boolean isDelayActive() {
        return delayActive;
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

    public boolean isHomeTeleportGroundCheck() {
        return homeTeleportGroundCheck;
    }

    public boolean isHomeTeleportObstructedHomeCheck() {
        return homeTeleportObstructedHomeCheck;
    }

    public int getHomeTeleportObstructedHomeRetryDuration() {
        return homeTeleportObstructedHomeRetryDuration;
    }

}
