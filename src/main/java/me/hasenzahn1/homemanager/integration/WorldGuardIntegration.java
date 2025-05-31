package me.hasenzahn1.homemanager.integration;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import me.hasenzahn1.homemanager.HomeManager;

public class WorldGuardIntegration {

    private final HomeManager plugin;

    public static StateFlag homeCreationFlag;
    public static StateFlag homeTeleportFlag;

    public WorldGuardIntegration(HomeManager plugin) {
        this.plugin = plugin;
    }

    public void register() {

        homeCreationFlag = registerFlag("create-homes");
        homeTeleportFlag = registerFlag("teleport-home");
    }

    private StateFlag registerFlag(String name) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag value = new StateFlag(name, true);
            registry.register(value);
            return value;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get(name);
            if (existing instanceof StateFlag) {
                return (StateFlag) existing;
            }

            //Disable API Integration
            HomeManager.WORLD_GUARD_API_EXISTS = false;
            throw new RuntimeException("Could not register flag as it already exists under another type: " + name);

        }
    }

}
