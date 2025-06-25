package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.teleportation.TeleportationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener that handles interruption of delayed teleportation caused by certain events such as
 * damage, death, or player disconnect.
 * <p>
 * This class checks if the current {@link WorldGroup} has delay settings enabled and
 * cancels the teleportation process accordingly.
 */
public class DelayListener implements Listener {

    private final HomeManager manager;

    public DelayListener(HomeManager manager) {
        this.manager = manager;
    }

    /**
     * Cancels a player's teleportation if they take damage from a cause listed
     * in the {@link WorldGroup}'s teleport delay interrupt settings.
     *
     * @param event The damage event
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        WorldGroup group = manager.getWorldGroupManager().getWorldGroup(event.getEntity().getWorld());
        if (!group.getSettings().isDelayActive()) return;
        if (!group.getSettings().getDelayInterruptCauses().contains(event.getCause())) return;

        TeleportationManager.getInstance().cancelTeleportation(event.getEntity().getUniqueId());
    }

    /**
     * Cancels a player's teleportation if they die while waiting for a delayed teleport.
     *
     * @param event The player death event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        WorldGroup group = manager.getWorldGroupManager().getWorldGroup(event.getEntity().getWorld());
        if (!group.getSettings().isTimeoutActive()) return;

        TeleportationManager.getInstance().cancelTeleportation(event.getEntity().getUniqueId());
    }

    /**
     * Cancels a player's teleportation if they disconnect during the teleport delay.
     *
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        WorldGroup group = manager.getWorldGroupManager().getWorldGroup(event.getPlayer().getWorld());
        if (!group.getSettings().isTimeoutActive()) return;

        TeleportationManager.getInstance().cancelTeleportation(event.getPlayer().getUniqueId());
    }

}