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

public class WorldGuardRegionCheck {

    private final StateFlag flag;

    public WorldGuardRegionCheck(StateFlag flag) {
        this.flag = flag;
    }

    public boolean canUseHomes(Player player) {
        RegionQuery query = WorldGuard.getInstance()
                .getPlatform().getRegionContainer()
                .createQuery();
        Location loc = BukkitAdapter.adapt(player.getLocation());
        ApplicableRegionSet set = query.getApplicableRegions(loc);

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        // 1. Ist global erlaubt?
        boolean isAllowed = query.testState(loc, localPlayer, flag);
        if (isAllowed) return true;


        // 2. Finde die höchste Priorität unter DENY-Regionen
        int maxDenyPrio = set.getRegions().stream()
                .filter(r -> r.getFlag(flag) == StateFlag.State.DENY)
                .mapToInt(ProtectedRegion::getPriority)
                .max()
                .orElse(Integer.MIN_VALUE);

        // 3. Checke jede Region mit genau dieser Priorität
        for (ProtectedRegion region : set.getRegions()) {
            if (region.getFlag(flag) == StateFlag.State.DENY
                    && region.getPriority() == maxDenyPrio) {
                if (region.isMember(localPlayer)) return true;

                return false;
            }
        }

        return false;
    }

}
