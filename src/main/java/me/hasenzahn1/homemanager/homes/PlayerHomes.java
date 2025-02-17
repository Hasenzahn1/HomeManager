package me.hasenzahn1.homemanager.homes;

import me.hasenzahn1.homemanager.group.WorldGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerHomes {

    private final HashMap<String, Home> playerHomes;

    public PlayerHomes(HashMap<String, Home> playerHomes) {
        this.playerHomes = playerHomes;
    }

    public boolean homeExists(String name) {
        return playerHomes.containsKey(name.toLowerCase());
    }

    public Home getHome(String name) {
        return playerHomes.get(name.toLowerCase());
    }

    public boolean hasHomes() {
        return !playerHomes.isEmpty();
    }

    public List<Home> getHomes() {
        return new ArrayList<>(playerHomes.values());
    }

    public int getHomeAmount() {
        return playerHomes.size();
    }

    public List<Home> getHomesInWorldGroup(WorldGroup group) {
        return playerHomes.values().stream().filter(home -> group.isInWorldGroup(home.location())).toList();
    }

}
