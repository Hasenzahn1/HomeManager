package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.homes.Home;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class DefaultConfig extends CustomConfig {

    public static boolean DEBUG_LOGGING = false;

    public static int CACHE_EXPIRE_DURATION = 3600;
    public static long HOME_SEARCH_DURATION_IN_SECONDS = 30;

    public static long HOME_ADMIN_CONFIRMATION_DURATION = 10;
    public static String SET_HOME_VALIDATION_REGEX = "[A-Za-z0-9\\-_]{0,30}";

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
        SET_HOME_VALIDATION_REGEX = config.getString("validHomeNameRegex", SET_HOME_VALIDATION_REGEX);

        DatabaseAccessor accessor = DatabaseAccessor.openSession();
        List<Home> homes = accessor.getHomesThatDontMatchRegex(SET_HOME_VALIDATION_REGEX);
        accessor.destroy();

        if (homes.isEmpty()) return;
        Logger.WARN.log("Some homes don't match the home name validation regex: ");
        for (Home home : homes) {
            Logger.WARN.log(" - Player: " + home.getOwnersName() + " -- World: " + home.location().getWorld().getName() + " -- Home: " + home.name());
        }
    }
}
