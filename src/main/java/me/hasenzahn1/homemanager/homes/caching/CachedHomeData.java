package me.hasenzahn1.homemanager.homes.caching;

import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;

import java.util.*;

/**
 * Represents cached home data for a specific player.
 * <p>
 * This class stores home names and the number of free homes per {@link WorldGroup}
 * and ensures that the data is valid within a given expiration time.
 */
public class CachedHomeData {

    private final DatabaseAccessor session;
    private final UUID uuid;
    private long creationTimestamp;
    private final HashMap<WorldGroup, DataPerGroup> data;
    private boolean valid;

    /**
     * Constructs a new {@code CachedHomeData} instance for a given player and database session.
     *
     * @param session the database accessor used to load data
     * @param uuid    the UUID of the player
     */
    public CachedHomeData(DatabaseAccessor session, UUID uuid) {
        this.session = session;
        this.uuid = uuid;
        this.creationTimestamp = 0;
        this.data = new HashMap<>();
        this.valid = false;
    }

    /**
     * Returns the names of the player's homes in the specified {@link WorldGroup}.
     * Loads data from the database if the cache is invalid.
     *
     * @param group the world group to query
     * @return a list of home names
     */
    public List<String> getHomeNames(WorldGroup group) {
        ensureValid();

        DataPerGroup dataPerGroup = data.get(group);
        if (dataPerGroup == null) return List.of();

        return dataPerGroup.homes();
    }

    /**
     * Returns the number of free home slots the player has in the specified {@link WorldGroup}.
     * Loads data from the database if the cache is invalid.
     *
     * @param group the world group to query
     * @return the number of free homes
     */
    public int getFreeHomes(WorldGroup group) {
        ensureValid();

        DataPerGroup dataPerGroup = data.get(group);
        if (dataPerGroup == null) return 0;
        return dataPerGroup.freehomes;
    }

    /**
     * Returns the number of existing homes the player has in the specified {@link WorldGroup}.
     *
     * @param group the world group to query
     * @return number of homes
     */
    public int getHomeCount(WorldGroup group) {
        return getHomeNames(group).size();
    }

    /**
     * Loads home data and free home slots from the database and builds the cache.
     */
    private void build() {
        HashMap<WorldGroup, List<String>> dbHomes = session.getAllHomeNamesFromPlayer(uuid);
        HashMap<WorldGroup, Integer> dbFreeHomes = session.getAllFreeHomes(uuid);

        data.clear();
        for (Map.Entry<WorldGroup, List<String>> entry : dbHomes.entrySet()) {
            WorldGroup group = entry.getKey();
            ArrayList<String> homes = new ArrayList<>(entry.getValue());
            int freeHomes = dbFreeHomes.getOrDefault(group, 0);

            data.put(group, new DataPerGroup(homes, freeHomes));
        }

        // Merge remaining free home entries not present in dbHomes
        for (Map.Entry<WorldGroup, Integer> entry : dbFreeHomes.entrySet()) {
            WorldGroup group = entry.getKey();
            int freeHomes = entry.getValue();

            data.putIfAbsent(group, new DataPerGroup(new ArrayList<>(), freeHomes));
        }

        valid = true;
        creationTimestamp = System.currentTimeMillis();
        Logger.DEBUG.log("Cached homes and freehomes for player " + uuid);
    }

    /**
     * Ensures the cache is valid. If it is not, it rebuilds it from the database.
     */
    private void ensureValid() {
        if (isValid()) return;
        build();
    }

    /**
     * Checks whether the cached data is still valid.
     * The cache is considered valid if it has not been loaded for more than {@link DefaultConfig#CACHE_EXPIRE_DURATION} seconds
     * and has not been invalidated otherwise
     *
     * @return true if the cache is valid, false otherwise
     */
    public boolean isValid() {
        return valid && creationTimestamp > System.currentTimeMillis() - DefaultConfig.CACHE_EXPIRE_DURATION * 1000L;
    }

    /**
     * Marks the cache entry as invalid, so it will be rebuilt on the next access.
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * Container record for the cached data per world group.
     *
     * @param homes     the list of home names
     * @param freehomes the number of free home slots
     */
    public record DataPerGroup(ArrayList<String> homes, int freehomes) {
    }
}

