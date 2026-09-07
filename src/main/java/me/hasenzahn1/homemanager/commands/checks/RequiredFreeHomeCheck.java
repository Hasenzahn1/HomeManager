package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;

import java.util.UUID;

/**
 * Checks whether a player must have at least one free home available before they are
 * allowed to set a new home.
 */
public class RequiredFreeHomeCheck {

    /**
     * Determines whether having a free home is a prerequisite for setting a home in the given group.
     *
     * @param worldGroup the world group whose settings are checked
     * @return {@code true} if free homes are enabled for the group and required for setting a home
     */
    public boolean isRequired(WorldGroup worldGroup) {
        return worldGroup.getSettings().isFreeHomesActive() && worldGroup.getSettings().isFreeHomesRequiredForSetHome();
    }

    /**
     * Checks whether the given player currently has at least one free home left in the given group.
     *
     * @param player     the UUID of the player to check
     * @param worldGroup the world group to check free homes for
     * @param dbSession  the database session used to look up the player's free home count
     * @return {@code true} if the player has more than zero free homes remaining
     */
    public boolean hasFreeHome(UUID player, WorldGroup worldGroup, DatabaseAccessor dbSession) {
        return dbSession.getFreeHomes(player, worldGroup.getName()) > 0;
    }

}
