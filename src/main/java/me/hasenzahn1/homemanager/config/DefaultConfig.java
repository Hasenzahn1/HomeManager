package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;
import org.bukkit.configuration.file.FileConfiguration;

public class DefaultConfig extends CustomConfig {

    public static boolean DEBUG_LOGGING = false;
    public static boolean DEBUG_REPLACE_CONFIG = false;

    public static int TAB_COMPLETION_CACHE_EXPIRE_DURATION = 100000;

    public DefaultConfig() {
        super(HomeManager.getInstance(), "config.yml");
        load();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        load();
    }

    public void load() {
        FileConfiguration config = getConfig();
        DEBUG_LOGGING = config.getBoolean("debug.logging", false);
        DEBUG_REPLACE_CONFIG = config.getBoolean("debug.regenerateConfigsOnStart", false);
        TAB_COMPLETION_CACHE_EXPIRE_DURATION = config.getInt("tabCompletionCacheExpireDuration", 100000);
    }
}
