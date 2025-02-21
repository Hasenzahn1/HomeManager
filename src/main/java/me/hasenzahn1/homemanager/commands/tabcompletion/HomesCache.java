package me.hasenzahn1.homemanager.commands.tabcompletion;

import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.group.WorldGroup;

public class HomesCache {

    private final long timestamp;
    private final String homeName;
    private final WorldGroup worldGroup;

    public HomesCache(String homeName, WorldGroup worldGroup) {
        this.timestamp = System.currentTimeMillis();
        this.homeName = homeName;
        this.worldGroup = worldGroup;
    }

    public boolean isValid() {
        return System.currentTimeMillis() - timestamp < DefaultConfig.TAB_COMPLETION_CACHE_EXPIRE_DURATION;
    }

    public String getHomeName() {
        return homeName;
    }

    public boolean isInWorldGroup(WorldGroup worldGroup) {
        return this.worldGroup.equals(worldGroup);
    }

}
