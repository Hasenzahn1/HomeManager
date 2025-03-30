package me.hasenzahn1.homemanager.homes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public record Home(UUID uuid, String name, Location location) {

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

    public String getOwnersName() {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null) return uuid.toString();
        return player.getName();
    }

}
