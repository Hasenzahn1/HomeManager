package me.hasenzahn1.homemanager.commands.checks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

/**
 * Utility class to check whether a player is allowed to use homes
 * in their current WorldGuard region context based on a specified {@link StateFlag}.
 * <p>
 * This considers global region rules and region priority for DENY flags,
 * but still allows members of the denying region to bypass restrictions.
 */
public class WorldGuardRegionCheck {

    private final StateFlag flag;

    public WorldGuardRegionCheck(StateFlag flag) {
        this.flag = flag;
    }

    /**
     * Determines if the given player is allowed to use homes at their current location.
     * <p>
     * The check follows these steps:
     * <ul>
     *     <li>Checks if the flag is globally allowed in the current region set.</li>
     *     <li>If denied, finds the highest-priority region with {@code DENY} for this flag.</li>
     *     <li>If the player is a member of that region, permission is granted anyway.</li>
     *     <li>Otherwise, access is denied.</li>
     * </ul>
     *
     * @param player the player to check
     * @return {@code true} if the player may use homes here; otherwise {@code false}
     */
    public boolean canUseHomes(Player player) {
        RegionQuery query = WorldGuard.getInstance()
                .getPlatform().getRegionContainer()
                .createQuery();
        Location loc = BukkitAdapter.adapt(player.getLocation());
        ApplicableRegionSet set = query.getApplicableRegions(loc);

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        //TODO: WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(player) | has bypass perm
        //TODO: WorldConfiguration worldConfig = WorldGuardPlugin.inst().getConfigManager().get(world); | worldConfig.useRegions Is Restricted world checken

        // 1. Check global allowance
        boolean isAllowed = query.testState(loc, localPlayer, flag);
        if (isAllowed) return true;

        // 2. Find highest-priority region with DENY
        int maxDenyPrio = set.getRegions().stream()
                .filter(r -> r.getFlag(flag) == StateFlag.State.DENY)
                .mapToInt(ProtectedRegion::getPriority)
                .max()
                .orElse(Integer.MIN_VALUE);

        // 3. Check if the player is a member of the denying region
        for (ProtectedRegion region : set.getRegions()) {
            if (region.getFlag(flag) == StateFlag.State.DENY
                    && region.getPriority() == maxDenyPrio) {
                return region.isMember(localPlayer);
            }
        }

        return false;
    }

}
