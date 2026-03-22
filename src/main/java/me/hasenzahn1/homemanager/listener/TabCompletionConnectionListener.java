package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.HomeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TabCompletionConnectionListener implements Listener {

    /**
     * Reload all offline players when a new possible unknown player joins.
     *
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPlayedBefore()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                HomeManager.getInstance().getCompletionsHelper().loadOfflinePlayers();
            }
        }.runTaskLater(HomeManager.getInstance(), 20);

    }

}
