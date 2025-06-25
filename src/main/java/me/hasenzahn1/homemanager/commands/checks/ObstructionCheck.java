package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.homes.Home;

import java.util.HashMap;
import java.util.UUID;

/**
 * Checks whether a teleportation to a home is obstructed. If this is the case a countdown is started
 * in which the command has to be retyped.
 */
public class ObstructionCheck {

    private static final long SECONDS_TO_MILLIS = 1000;

    private final HashMap<UUID, String> lastHomes;
    private final HashMap<UUID, Long> obstructedTimestamps;

    public ObstructionCheck() {
        lastHomes = new HashMap<>();
        obstructedTimestamps = new HashMap<>();
    }

    /**
     * Checks if the given home is currently obstructed and whether a retry countdown should be started.
     * <p>
     * Logic:
     * <ul>
     *     <li>If the player is teleporting to the same home as before and within the retry timeout,
     *         the retry is allowed (returns {@code false}).</li>
     *     <li>If the home is obstructed and not a retry, the obstruction is recorded (returns {@code true}).</li>
     *     <li>If this is a retry after obstruction and timeout has passed, the obstruction new obstruction is recorded (return {@code true}).</li>
     * </ul>
     *
     * @param arguments     the command sender and parsed arguments
     * @param requestedHome the home being teleported to
     * @return {@code true} if the teleportation is currently obstructed, {@code false} otherwise
     */
    public boolean checkForObstruction(PlayerNameGroupArguments arguments, Home requestedHome) {
        UUID playerId = arguments.getCmdSender().getUniqueId();
        String lastHome = lastHomes.getOrDefault(playerId, "");
        long lastTime = obstructedTimestamps.getOrDefault(playerId, 0L);
        long retryTimeoutMillis = arguments.getWorldGroup().getSettings().getObstructedHomeCheckRetryDurationInSeconds() * SECONDS_TO_MILLIS;

        boolean sameHome = lastHome.equalsIgnoreCase(requestedHome.name());
        boolean withinRetryWindow = System.currentTimeMillis() - lastTime <= retryTimeoutMillis;
        boolean isRetry = sameHome && withinRetryWindow;

        if (!isRetry && requestedHome.isObstructed()) {
            // First obstruction detected
            obstructedTimestamps.put(playerId, System.currentTimeMillis());
            lastHomes.put(playerId, requestedHome.name());
            return true;
        }

        if (isRetry) {
            // Clear state on retry
            obstructedTimestamps.remove(playerId);
            lastHomes.remove(playerId);
        }

        return false;
    }
}
