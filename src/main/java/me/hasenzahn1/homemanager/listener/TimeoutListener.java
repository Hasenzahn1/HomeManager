package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Listener that tracks damage timestamps for players based on configured timeout causes.
 * <p>
 * This is used to enforce a timeout after receiving certain types of damage before the
 * player can execute specific actions (e.g., teleporting home).
 */
public class TimeoutListener implements Listener {

    private final HomeManager manager;
    private final HashMap<UUID, Long> damageTimestamps;

    public TimeoutListener(HomeManager manager) {
        this.manager = manager;
        this.damageTimestamps = new HashMap<>();
    }

    /**
     * Handles {@link EntityDamageEvent} and records the time if the event matches
     * the timeout-causing damage types for the current world group.
     *
     * @param event The entity damage event
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        WorldGroup group = manager.getWorldGroupManager().getWorldGroup(event.getEntity().getWorld());
        if (!group.getSettings().isTimeoutActive()) return;
        if (!group.getSettings().getTimoutCauses().contains(event.getCause())) return;

        damageTimestamps.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Returns the timestamp of the last relevant damage received by the given player.
     *
     * @param player The player to check
     * @return The timestamp in milliseconds since epoch, or {@code 0L} if no relevant damage occurred
     */
    public long getLastTimestamp(Player player) {
        return damageTimestamps.getOrDefault(player.getUniqueId(), 0L);
    }
}