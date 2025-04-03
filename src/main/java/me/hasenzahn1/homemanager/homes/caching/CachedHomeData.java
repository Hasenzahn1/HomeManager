package me.hasenzahn1.homemanager.homes.caching;

import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;

import java.util.*;

public class CachedHomeData {

    private final DatabaseAccessor session;
    private final UUID uuid;

    private long creationTimestamp;
    private final HashMap<WorldGroup, DataPerGroup> data;
    private boolean valid;

    public CachedHomeData(DatabaseAccessor session, UUID uuid) {
        this.session = session;
        this.uuid = uuid;
        this.creationTimestamp = 0;
        data = new HashMap<>();
        valid = false;
    }


    public List<String> getHomeNames(WorldGroup group) {
        ensureValid();

        DataPerGroup dataPerGroup = data.get(group);
        if (dataPerGroup == null) return List.of();

        return dataPerGroup.homes();
    }

    public int getFreeHomes(WorldGroup group) {
        ensureValid();

        DataPerGroup dataPerGroup = data.get(group);
        if (dataPerGroup == null) return 0;
        return dataPerGroup.freehomes;
    }

    public int getHomeCount(WorldGroup group) {
        return getHomeNames(group).size();
    }

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

        // Merge free homes for groups that are not in dbHomes
        for (Map.Entry<WorldGroup, Integer> entry : dbFreeHomes.entrySet()) {
            WorldGroup group = entry.getKey();
            int freeHomes = entry.getValue();

            data.putIfAbsent(group, new DataPerGroup(new ArrayList<>(), freeHomes));
        }

        valid = true;
        creationTimestamp = System.currentTimeMillis();
        Logger.DEBUG.log("Cached homes and freehomes for player " + uuid);
    }

    private void ensureValid() {
        if (isValid()) return;
        build();
    }

    public boolean isValid() {
        return valid && creationTimestamp > System.currentTimeMillis() - DefaultConfig.CACHE_EXPIRE_DURATION * 1000L;
    }

    public void invalidate() {
        valid = false;
    }


    public record DataPerGroup(ArrayList<String> homes, int freehomes) {
    }
}
