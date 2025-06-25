package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.permission.PermissionValidator;

/**
 * Abstract base class for checking experience requirements when setting or modifying homes.
 * <p>
 * Subclasses must implement how the required experience is calculated.
 */
public abstract class HomeExperienceCheck {

    /**
     * Calculates the number of experience levels required for a given home action.
     *
     * @param arguments     the parsed player and home arguments
     * @param currentHomes  the number of homes the player currently owns
     * @param requestedHome the home being created or modified
     * @return the number of experience levels required
     */
    public abstract int getRequiredExperience(PlayerNameArguments arguments, int currentHomes, Home requestedHome);

    /**
     * Checks whether the player lacks the required experience levels for the action.
     * If a player does not have to pay experience this is ignored.
     *
     * @param arguments             the parsed player and home arguments
     * @param currentHomes          the number of homes the player currently owns
     * @param requestedHome         the home being created or modified
     * @param disableWithBypassPerm whether bypass permission disables the cost
     * @return {@code true} if the player does not have enough experience, {@code false} otherwise
     */
    public boolean checkForInvalidExperience(PlayerNameArguments arguments, int currentHomes, Home requestedHome, boolean disableWithBypassPerm) {
        int requiredLevels = getRequiredExperience(arguments, currentHomes, requestedHome);
        boolean hasToPayExperience = hasToPayExperience(arguments, disableWithBypassPerm);

        return hasToPayExperience && (arguments.getCmdSender().getLevel() < requiredLevels);
    }

    /**
     * Determines if a player is required to pay experience for the action.
     * <p>
     * A player is exempt from cost if any of the following apply:
     * <ul>
     *     <li>They are not the target player (e.g., setting for another player)</li>
     *     <li>They are in a game mode where experience is not relevant (e.g., Creative)</li>
     *     <li>They have a bypass permission and {@code disableWithBypassPermission} is true</li>
     * </ul>
     *
     * @param arguments                   the parsed player and home arguments
     * @param disableWithBypassPermission whether having a bypass permission disables the cost
     * @return {@code true} if the player must pay experience, {@code false} otherwise
     */
    public boolean hasToPayExperience(PlayerNameArguments arguments, boolean disableWithBypassPermission) {
        return arguments.isSelf()
                && !arguments.getCmdSender().getGameMode().isInvulnerable()
                && (!disableWithBypassPermission || !PermissionValidator.hasBypassPermission(arguments.getCmdSender(), arguments.getWorldGroup()));
    }
}

