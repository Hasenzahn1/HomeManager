package me.hasenzahn1.homemanager.homes;


import org.bukkit.Location;

public class PlayerHome {

    private String name;
    private Location location;

    public PlayerHome(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
