package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.homes.HomeDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Listener that removes home display entities when a chunk is unloaded.
 * <p>
 * Ensures that display entities related to homes are not left behind when their
 * chunk is unloaded, preventing potential memory leaks or entity persistence issues.
 */
public class HomeDisplayRemover implements Listener {

    /**
     * Handles the {@link ChunkUnloadEvent} and removes any entities that are marked as
     * home displays via the {@link HomeDisplay#DISPLAY_KEY} in their persistent data container.
     *
     * @param event The chunk unload event
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        int count = 0;
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(HomeDisplay.DISPLAY_KEY)) {
                entity.remove();
                count++;
            }
        }

        if (count > 0) {
            Logger.ERROR.log("Unloaded " + count + " homedisplays from world " + event.getWorld().getName());
        }
    }
}