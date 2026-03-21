package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.HomeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabCompletionConnectionListener implements Listener {

    /**
     * Reload all offline players when a new possible unknown player joins.
     *
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        HomeManager.getInstance().getCompletionsHelper().loadOfflinePlayers();
    }

}
