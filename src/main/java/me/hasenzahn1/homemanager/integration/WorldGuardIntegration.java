package me.hasenzahn1.homemanager.integration;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import me.hasenzahn1.homemanager.HomeManager;

/**
 * Integration class for WorldGuard plugin to add custom flags related to home functionality.
 * This class is only loaded when the WorldGuard plugin is loaded on the server
 * <p>
 * This class allows registering flags such as {@code CREATE_HOMES} and {@code TELEPORT_HOMES}
 * that control whether players can set or teleport to homes on specific plots.
 * <p>
 * If a conflict arises during flag registration (e.g. flag already exists with a different type),
 * the WorldGuard integration is disabled.
 */
public class WorldGuardIntegration {

    public static StateFlag homeCreationFlag;
    public static StateFlag homeTeleportFlag;

    private final HomeManager plugin;

    public WorldGuardIntegration(HomeManager plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers the custom WorldGuard flags {@code create-homes} and {@code teleport-home}.
     * <p>
     * If the flag already exists as a {@link StateFlag}, it will be reused.
     * If it exists as another type, the integration is disabled and a {@link RuntimeException} is thrown.
     */
    public void register() {
        homeCreationFlag = registerFlag("create-homes");
        homeTeleportFlag = registerFlag("teleport-homes");
    }

    /**
     * Attempts to register a {@link StateFlag} with the given name in the WorldGuard flag registry.
     * If a conflict occurs and the flag already exists but is not a {@link StateFlag},
     * the integration is disabled.
     *
     * @param name the name of the flag to register
     * @return the registered or existing {@link StateFlag}
     * @throws RuntimeException if the flag exists with a different type
     */
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

            // Disable API Integration if conflict is unrecoverable
            HomeManager.WORLD_GUARD_API_EXISTS = false;
            throw new RuntimeException("Could not register flag as it already exists under another type: " + name);
        }
    }
}
