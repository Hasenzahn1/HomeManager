package me.hasenzahn1.homemanager.group;

import me.hasenzahn1.homemanager.util.ExpressionEvaluator;
import me.hasenzahn1.homemanager.util.PermissionUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Objects;

/**
 * Represents the settings for a specific world group in the HomeManager plugin.
 * This class holds various configuration options such as experience requirements, delays, timeouts, and other settings
 * for home management in a given world group.
 */
public class WorldGroupSettings {

    // Default settings for world groups
    public static WorldGroupSettings DEFAULT = new WorldGroupSettings();

    private WorldGroup parentWorldGroup;

    // Home management settings
    private boolean setHomeExperienceActive = false;
    private List<Integer> setHomeExperiencePerHome = List.of();
    private String setHomeExperienceFormula = "";

    private boolean freeHomesActive = false;
    private boolean freeHomesDisableInCreative = true;

    private boolean homeTeleportExperienceActive = false;
    private String homeTeleportExperienceFormula = "";

    private boolean delayActive = false;
    private int delayDurationInSeconds = 5;
    private boolean delayDisableInCreative = true;
    private List<EntityDamageEvent.DamageCause> delayInterruptCauses = List.of();

    private boolean timeoutActive = false;
    private int timeoutDurationInSeconds = 5;
    private List<EntityDamageEvent.DamageCause> timeoutCauses = List.of();

    private boolean safeTeleportActive = false;

    private boolean obstructedHomeCheckActive = false;
    private boolean obstructedHomeCheckDisableInCreative = true;
    private int obstructedHomeCheckRetryDurationInSeconds = 5;

    private boolean homeTeleportOnGroundCheckActive = false;
    private boolean homeTeleportOnGroundCheckDisableInCreative = true;

    private int maxHomes = 10;

    /**
     * Constructs a WorldGroupSettings object from a configuration section.
     * This constructor loads the settings for a specific world group from the provided configuration.
     * If a key is not present in the section a default value will be used.
     *
     * @param section          The configuration section containing the world group settings.
     * @param parentWorldGroup The parent world group that this settings object belongs to.
     */
    public WorldGroupSettings(ConfigurationSection section, WorldGroup parentWorldGroup) {
        this.parentWorldGroup = parentWorldGroup;
        setHomeExperienceActive = section.getBoolean("setHomeExperience.active", setHomeExperienceActive);
        setHomeExperienceFormula = section.getString("setHomeExperience.experienceFormula", setHomeExperienceFormula);
        setHomeExperiencePerHome = section.getIntegerList("setHomeExperience.experiencePerHome");

        freeHomesActive = section.getBoolean("freeHomes.active", freeHomesActive);
        freeHomesDisableInCreative = section.getBoolean("freeHomes.disableInCreative", freeHomesDisableInCreative);

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

        safeTeleportActive = section.getBoolean("safeTeleport.active", safeTeleportActive);

        obstructedHomeCheckActive = section.getBoolean("obstructedHomeCheck.active", obstructedHomeCheckActive);
        obstructedHomeCheckDisableInCreative = section.getBoolean("obstructedHomeCheck.disableInCreative", obstructedHomeCheckDisableInCreative);
        obstructedHomeCheckRetryDurationInSeconds = section.getInt("obstructedHomeCheck.retryDuration", obstructedHomeCheckRetryDurationInSeconds);

        homeTeleportOnGroundCheckActive = section.getBoolean("homeTeleportOnGroundCheck.active", homeTeleportOnGroundCheckActive);
        homeTeleportOnGroundCheckDisableInCreative = section.getBoolean("homeTeleportOnGroundCheck.disableInCreative", homeTeleportOnGroundCheckDisableInCreative);

        maxHomes = section.getInt("maxHomes", maxHomes);
    }

    /**
     * Default constructor for WorldGroupSettings (used for the default settings).
     */
    private WorldGroupSettings() {
    }

    /**
     * Sets the parent world group for this settings object.
     *
     * @param worldGroup The parent world group.
     */
    public void setParentWorldGroup(WorldGroup worldGroup) {
        this.parentWorldGroup = worldGroup;
    }

    /**
     * Calculates the required experience for a player to set a home, based on the number of homes they already have.
     *
     * @param currentHomes The number of homes the player currently has.
     * @return The amount of experience required to set another home.
     */
    public int getRequiredExperience(int currentHomes) {
        if (setHomeExperiencePerHome.size() > currentHomes) return setHomeExperiencePerHome.get(currentHomes);
        if (!setHomeExperienceFormula.isEmpty())
            return (int) ExpressionEvaluator.eval(setHomeExperienceFormula.replace("amount", String.valueOf(currentHomes)));
        if (!setHomeExperiencePerHome.isEmpty())
            return setHomeExperiencePerHome.get(setHomeExperiencePerHome.size() - 1);
        return 0;
    }

    /**
     * Retrieves the maximum number of homes a player can have based on their permissions and the world group's settings.
     *
     * @param player The player whose home limit is being checked.
     * @return The maximum number of homes the player can have.
     */
    public int getMaxHomes(CommandSender player) {
        int maxHomes = PermissionUtils.getMaxHomesFromPermission(player, parentWorldGroup.getName());
        if (maxHomes < 0) {
            maxHomes = this.maxHomes;
        }
        return maxHomes;
    }

    /**
     * Calculates the experience required for a home teleportation based on the distance and whether the world changes.
     *
     * @param start The starting location of the teleportation.
     * @param end   The destination location of the teleportation.
     * @return The amount of experience required for the teleportation.
     */
    public int getHomeTeleportExperience(Location start, Location end) {
        if (homeTeleportExperienceFormula.isEmpty()) return 0;
        double dist = start.toVector().distance(end.toVector());
        boolean world = start.getWorld().equals(end.getWorld());
        return (int) ExpressionEvaluator.eval(homeTeleportExperienceFormula.replace("dist", String.valueOf(dist)).replace("worldChange", String.valueOf(world ? 1 : 0)));
    }

    // Getters for various settings

    public boolean isSetHomeExperienceActive() {
        return setHomeExperienceActive;
    }

    public boolean isFreeHomesActive() {
        return freeHomesActive;
    }

    public boolean isHomeTeleportExperienceActive() {
        return homeTeleportExperienceActive;
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

    public boolean isSafeTeleportActive() {
        return safeTeleportActive;
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

    public boolean isFreeHomesDisableInCreative() {
        return freeHomesDisableInCreative;
    }
}
