package me.hasenzahn1.homemanager.homes;

import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class HomeDisplay {

    protected Player initiator;
    protected Home home;

    public HomeDisplay(Player initiator, Home home) {
        this.initiator = initiator;
        this.home = home;

        display();
    }

    /**
     * Spawns the glowing marker slime and the display. Loading the chunk in the process
     */
    public abstract void display();

    /**
     * Destroys the glowing marker and the name display.
     */
    public abstract void destroy();

    /**
     * Checks if this home display has been spawned in the world.
     * <p>
     * A display is considered spawned if its visual representation
     * has been created and placed into the Minecraft world.
     *
     * @return true if the display has been spawned, false otherwise
     */
    public abstract boolean hasBeenSpawned();

    /**
     * Generates a deterministic RGB color code based on the hash of the given UUID.
     * <p>
     * This method extracts the red, green, and blue components from the UUID's hash code
     * and combines them into a single RGB integer (0xRRGGBB).
     *
     * @param uuid The UUID from which to generate the color.
     * @return An integer representing the RGB color.
     */
    protected int getColorFromUUID(UUID uuid) {
        int hash = uuid.hashCode();

        int r = (hash >> 16) & 0xFF;
        int g = (hash >> 8) & 0xFF;
        int b = hash & 0xFF;

        return (r << 16) | (g << 8) | b;
    }

}
