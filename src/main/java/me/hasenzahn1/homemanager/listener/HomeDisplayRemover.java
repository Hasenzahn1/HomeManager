package me.hasenzahn1.homemanager.listener;

import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.homes.HomeDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class HomeDisplayRemover implements Listener {

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
