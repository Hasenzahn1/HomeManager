package me.hasenzahn1.homemanager.homes.caching;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class HomesCache {

    private final HashMap<UUID, CachedHomeData> cache;

    private final DatabaseAccessor session;

    private BukkitTask cacheClearTask;

    public HomesCache() {
        session = DatabaseAccessor.openSession();
        cache = new HashMap<>();

        startCacheClearTask();
    }

    public CachedHomeData get(UUID uuid) {
        if (!cache.containsKey(uuid)) cache.put(uuid, new CachedHomeData(session, uuid));

        return cache.get(uuid);
    }

    public void invalidateAll() {
        cache.clear();
    }

    public void destroy() {
        if (cacheClearTask != null) cacheClearTask.cancel();
        session.destroy();
        cache.clear();
    }

    private void startCacheClearTask() {
        cacheClearTask = new BukkitRunnable() {

            @Override
            public void run() {
                ArrayList<UUID> toRemove = new ArrayList<>();
                for (UUID uuid : cache.keySet()) {
                    if (!cache.get(uuid).isValid()) toRemove.add(uuid);
                }

                for (UUID uuid : toRemove) {
                    cache.remove(uuid);
                }

                if (!toRemove.isEmpty()) Logger.DEBUG.log("Removed " + toRemove.size() + " invalid Home caches");
            }
        }.runTaskTimer(HomeManager.getInstance(), 0, 20 * 60);
    }

    public void invalidateCache(UUID player) {
        if (!cache.containsKey(player)) return;

        cache.remove(player);
    }
}
