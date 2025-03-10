package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DelayListener implements Listener {

    private final HomeManager manager;

    public DelayListener(HomeManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        WorldGroup group = manager.getWorldGroupManager().getWorldGroup(event.getEntity().getWorld());
        if (!group.getSettings().isTimeoutActive()) return;
        if (!group.getSettings().getDelayInterruptCauses().contains(event.getCause())) return;

        manager.cancelTeleportation(event.getEntity().getUniqueId());
    }

}
