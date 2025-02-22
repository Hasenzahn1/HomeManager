package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.UUID;

public class TimeoutListener implements Listener {

    private final HomeManager manager;
    private final HashMap<UUID, Long> damageTimestamps;

    public TimeoutListener(HomeManager manager) {
        this.manager = manager;

        damageTimestamps = new HashMap<>();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        WorldGroup group = manager.getWorldGroupManager().getWorldGroup(event.getEntity().getWorld());
        if (!group.getSettings().isTimeoutActive()) return;
        if (!group.getSettings().getTimoutCauses().contains(event.getCause())) return;

        damageTimestamps.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
    }

    public HashMap<UUID, Long> getDamageTimestamps() {
        return damageTimestamps;
    }
}
