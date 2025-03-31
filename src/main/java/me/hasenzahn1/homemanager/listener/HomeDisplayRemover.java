package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.homes.HomeDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class HomeDisplayRemover implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(HomeDisplay.DISPLAY_KEY)) {
                entity.remove();
            }
        }
    }
}
