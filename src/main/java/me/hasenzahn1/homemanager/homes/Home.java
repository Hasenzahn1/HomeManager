package me.hasenzahn1.homemanager.homes;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public record Home(String name, Location location) {

    public void teleport(Player player) {
        if (!location.isWorldLoaded()) return;
        player.teleport(location);
    }

    public boolean isObstructed() {
        if (!location.isChunkLoaded()) location.getChunk().load();

        if (location.getBlock().isCollidable()) return true;
        if (location.clone().add(0, 1, 0).getBlock().isCollidable()) return true;

        return false;
    }

}
