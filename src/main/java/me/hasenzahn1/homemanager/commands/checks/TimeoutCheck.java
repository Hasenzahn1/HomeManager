package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.entity.Player;

/**
 * Utility class to check whether a player is currently within a timeout period
 * that prevents home-related actions.
 * <p>
 * This class checks based on the values of the Timoutlistener class obtained by {@link HomeManager#getTimeoutListener()}
 */
public class TimeoutCheck {

    /**
     * Checks whether the given player is still in a timeout period within the specified world group.
     * <p>
     * This timeout is typically set when taking damage.
     *
     * @param player     the player to check
     * @param worldGroup the world group whose timeout setting is used
     * @return {@code true} if the player is still in the timeout period, otherwise {@code false}
     */
    public boolean isInTimeout(Player player, WorldGroup worldGroup) {
        return System.currentTimeMillis() - HomeManager.getInstance().getTimeoutListener().getLastTimestamp(player)
                < worldGroup.getSettings().getTimeoutDurationInSeconds() * 1000L;
    }

    /**
     * Gets the number of seconds remaining in the timeout period for the given player.
     * <p>
     * This is useful for informing players how much longer they must wait before
     * performing the next action (e.g., teleporting to a home).
     *
     * @param arguments the parsed player and world group arguments
     * @return the remaining timeout duration in whole seconds (rounded)
     */
    public int getRemainingSeconds(PlayerNameGroupArguments arguments) {
        double durationInMillis = arguments.getWorldGroup().getSettings().getTimeoutDurationInSeconds() * 1000L
                - (System.currentTimeMillis() - HomeManager.getInstance().getTimeoutListener().getLastTimestamp(arguments.getCmdSender()));
        return (int) Math.round(durationInMillis / 1000.0);
    }

}
