package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.homes.Home;

import java.util.HashMap;
import java.util.UUID;

public class ObstructionCheck {

    private static final long SECONDS_TO_MILLIS = 1000;

    private final HashMap<UUID, String> lastHomes;
    private final HashMap<UUID, Long> obstructedTimestamps;

    public ObstructionCheck() {
        lastHomes = new HashMap<>();
        obstructedTimestamps = new HashMap<>();
    }

    public boolean checkForObstruction(PlayerNameGroupArguments arguments, Home requestedHome) {
        boolean sameHome = lastHomes.getOrDefault(arguments.getCmdSender().getUniqueId(), "").equalsIgnoreCase(requestedHome.name());
        boolean within5Seconds = System.currentTimeMillis() - obstructedTimestamps.getOrDefault(arguments.getCmdSender().getUniqueId(), 0L) <= arguments.getWorldGroup().getSettings().getHomeTeleportObstructedHomeRetryDuration() * SECONDS_TO_MILLIS;
        boolean retried = sameHome && within5Seconds;

        //Check for home obstruction
        if (!retried && requestedHome.isObstructed()) {
            obstructedTimestamps.put(arguments.getCmdSender().getUniqueId(), System.currentTimeMillis());
            lastHomes.put(arguments.getCmdSender().getUniqueId(), requestedHome.name());
            return true;
        }
        return false;
    }

}
