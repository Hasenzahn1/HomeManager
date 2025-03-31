package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;
import org.bukkit.configuration.file.FileConfiguration;

public class DefaultConfig extends CustomConfig {

    public static boolean DEBUG_LOGGING = false;
    public static boolean DEBUG_REPLACE_CONFIG = false;

    public static int TAB_COMPLETION_CACHE_EXPIRE_DURATION = 100000;
    public static long HOME_SEARCH_DURATION_IN_SECONDS = 30;

    public DefaultConfig() {
        super(HomeManager.getInstance(), "config.yml");
        load();
    }

    public void reload() {
        reloadConfig();
        loadValues();
    }

    private void load() {
        loadValues();

        if (DEBUG_REPLACE_CONFIG) {
            HomeManager.getInstance().saveResource("config.yml", true);
            reloadConfig();
            loadValues();
        }
    }

    private void loadValues() {
        FileConfiguration config = getConfig();
        DEBUG_REPLACE_CONFIG = config.getBoolean("debug.regenerateConfigsOnStart", false);
        DEBUG_LOGGING = config.getBoolean("debug.logging", false);
        TAB_COMPLETION_CACHE_EXPIRE_DURATION = config.getInt("tabCompletionCacheExpireDuration", 100000);
        HOME_SEARCH_DURATION_IN_SECONDS = config.getLong("homeSearchDurationInSeconds", 30);
    }
}
