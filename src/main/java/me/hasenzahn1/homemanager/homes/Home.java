package me.hasenzahn1.homemanager.homes;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public record Home(String name, Location location) {

    public void teleport(Player player) {
        player.teleport(location);
    }

}
