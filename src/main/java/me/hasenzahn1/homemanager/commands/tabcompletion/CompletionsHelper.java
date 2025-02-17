package me.hasenzahn1.homemanager.commands.tabcompletion;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.util.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CompletionsHelper {

    private final HashMap<UUID, ArrayList<HomesCache>> cachedHomes;

    public CompletionsHelper() {
        cachedHomes = new HashMap<>();
    }

    public List<String> matchAndSort(List<String> options, String start) {
        return options.stream().filter(f -> f.toLowerCase().startsWith(start.toLowerCase())).sorted().toList();
    }

    public List<String> getWorldGroups(CommandSender sender, String permissionStub) {
        return PermissionUtils.getGroupsFromOtherPermission(sender, permissionStub);
    }

    public List<String> getOfflinePlayers() {
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList();
    }

    public List<String> getHomeSuggestions(Player sender, String playerHomesToList) {
        WorldGroup group = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(sender.getWorld());
        OfflinePlayer playerToGetHomesFrom = !playerHomesToList.isEmpty() ? Bukkit.getOfflinePlayerIfCached(playerHomesToList) : null;
        if (playerToGetHomesFrom == null) return List.of();

        if (isCachedHomesInvalid(playerToGetHomesFrom.getUniqueId(), group)) {
            revalidateCaches(playerToGetHomesFrom.getUniqueId(), group);
        }

        return cachedHomes.get(playerToGetHomesFrom.getUniqueId()).stream()
                .filter(f -> f.isInWorldGroup(group)).map(HomesCache::getHomeName).toList();
    }


    private boolean isCachedHomesInvalid(UUID uuid, WorldGroup group) {
        if (!cachedHomes.containsKey(uuid)) return true;
        if (cachedHomes.get(uuid).isEmpty()) return true;

        for (HomesCache cache : cachedHomes.get(uuid)) {
            if (cache.isInWorldGroup(group) && !cache.isValid()) return true;
        }
        return false;
    }

    private void revalidateCaches(UUID uuid, WorldGroup group) {
        if (!cachedHomes.containsKey(uuid)) cachedHomes.put(uuid, new ArrayList<>());
        DatabaseAccessor session = DatabaseAccessor.openSession();
        PlayerHomes playerHomes = session.getHomesFromPlayer(uuid, group.getName());
        List<HomesCache> homes = playerHomes.getHomesInWorldGroup(group).stream()
                .map(Home::name)
                .map(homeName -> new HomesCache(uuid, homeName, group)).toList();

        cachedHomes.get(uuid).addAll(homes);
        session.destroy();
    }

    public void invalidatePlayerHomes(UUID player) {
        cachedHomes.remove(player);
    }
}
