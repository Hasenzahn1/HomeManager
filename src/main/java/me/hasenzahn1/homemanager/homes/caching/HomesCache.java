package me.hasenzahn1.homemanager.homes.caching;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Manages a temporary in-memory cache of player homes to reduce database access.
 * <p>
 * This class maintains a cache of {@link CachedHomeData} for each player identified by their UUID.
 * It periodically clears invalid cache entries and allows manual invalidation or cleanup.
 * This cache is only used for TabCompletion and the PaPi extension.
 * One database session is kept open to retrieve and update homes from.
 */
public class HomesCache {

    private final HashMap<UUID, CachedHomeData> cache;
    private final DatabaseAccessor session;
    private BukkitTask cacheClearTask;

    /**
     * Creates a new {@code HomesCache} instance and starts the automatic cache clearing task.
     */
    public HomesCache() {
        session = DatabaseAccessor.openSession();
        cache = new HashMap<>();

        startCacheClearTask();
    }

    /**
     * Returns the cached home data for the specified player.
     * If not present, it loads and caches the data.
     *
     * @param uuid the UUID of the player
     * @return the {@link CachedHomeData} for the player
     */
    public CachedHomeData get(UUID uuid) {
        if (!cache.containsKey(uuid)) cache.put(uuid, new CachedHomeData(session, uuid));
        return cache.get(uuid);
    }

    /**
     * Clears the entire cache, removing all cached player home data.
     */
    public void invalidateAll() {
        cache.clear();
    }

    /**
     * Cancels the cache clear task and cleans up all resources,
     * including the database session and cached data.
     */
    public void destroy() {
        if (cacheClearTask != null) cacheClearTask.cancel();
        session.destroy();
        cache.clear();
    }

    /**
     * Starts a scheduled task that periodically checks the cache
     * for invalid entries and removes them.
     */
    private void startCacheClearTask() {
        cacheClearTask = new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<UUID> toRemove = new ArrayList<>();
                for (UUID uuid : cache.keySet()) {
                    if (!cache.get(uuid).isValid()) {
                        toRemove.add(uuid);
                    }
                }

                for (UUID uuid : toRemove) {
                    cache.remove(uuid);
                }

                if (!toRemove.isEmpty()) {
                    Logger.DEBUG.log("Removed " + toRemove.size() + " invalid Home caches");
                }
            }
        }.runTaskTimer(HomeManager.getInstance(), 0, 20 * 60); // Run every 60 seconds
    }

    /**
     * Invalidates the cached home data for a specific player, if present.
     *
     * @param player the UUID of the player whose cache should be invalidated
     */
    public void invalidateCache(UUID player) {
        if (!cache.containsKey(player)) return;
        cache.remove(player);
    }
}

