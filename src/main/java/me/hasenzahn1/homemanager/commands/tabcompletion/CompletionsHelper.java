package me.hasenzahn1.homemanager.commands.tabcompletion;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.util.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CompletionsHelper {

    public CompletionsHelper() {
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

        return HomeManager.getInstance().getHomesCache().get(playerToGetHomesFrom.getUniqueId()).getHomeNames(group);
    }
}
