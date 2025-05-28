package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;
import org.bukkit.configuration.file.FileConfiguration;

public class DefaultConfig extends CustomConfig {

    public static boolean DEBUG_LOGGING = false;

    public static int CACHE_EXPIRE_DURATION = 3600;
    public static long HOME_SEARCH_DURATION_IN_SECONDS = 30;

    public static long HOME_ADMIN_CONFIRMATION_DURATION = 10;

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

        if (HomeManager.DEV_MODE) {
            HomeManager.getInstance().saveResource("config.yml", true);
            reloadConfig();
            loadValues();
        }
    }

    private void loadValues() {
        FileConfiguration config = getConfig();
        DEBUG_LOGGING = config.getBoolean("debug.logging", false);
        CACHE_EXPIRE_DURATION = config.getInt("cacheExpireDuration", 3600);
        HOME_SEARCH_DURATION_IN_SECONDS = config.getLong("homeSearchDurationInSeconds", 30);
        HOME_ADMIN_CONFIRMATION_DURATION = config.getLong("homeAdminConfirmationDuration", 30);
    }
}
