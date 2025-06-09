package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.commands.*;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.db.HomesDatabase;
import me.hasenzahn1.homemanager.group.WorldGroupManager;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerTeleportation;
import me.hasenzahn1.homemanager.homes.caching.HomesCache;
import me.hasenzahn1.homemanager.integration.PlotsquaredIntegration;
import me.hasenzahn1.homemanager.integration.WorldGuardIntegration;
import me.hasenzahn1.homemanager.listener.DelayListener;
import me.hasenzahn1.homemanager.listener.HomeDisplayRemover;
import me.hasenzahn1.homemanager.listener.TimeoutListener;
import me.hasenzahn1.homemanager.migration.BasicHomesMigrator;
import me.hasenzahn1.homemanager.migration.HomeMigrator;
import me.hasenzahn1.homemanager.papi.PlaceholderHomeExpansion;
import me.hasenzahn1.homemanager.updates.VersionUpgrader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class HomeManager extends JavaPlugin {

    public static int PLUGIN_VERSION = 1;
    public static boolean PLACEHOLDER_API_EXISTS;
    public static boolean WORLD_GUARD_API_EXISTS;
    public static boolean PLOTSQUARED_API_EXISTS;
    public static boolean DEV_MODE = true;

    public static String PREFIX = "[HomeManager]";
    private static HomeManager instance;

    private WorldGroupManager worldGroupManager;
    private HomesDatabase database;

    private DefaultConfig config;

    private CompletionsHelper completionsHelper;
    private HomesCache homesCache;
    private HomeMigrator homeMigrator;

    private TimeoutListener timeoutListener;

    private HashMap<UUID, PlayerTeleportation> teleportations;

    public void onLoad() {
        //Register Worldguard Integration
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            WORLD_GUARD_API_EXISTS = true;
            new WorldGuardIntegration(this).register();
            Logger.DEBUG.log("Registered WorldGuard Integration");
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        //Load Default config
        config = new DefaultConfig();

        //Initialize Language
        Language.initialize();
        PREFIX = Language.getLang("prefix");

        //Load groups form config
        worldGroupManager = new WorldGroupManager();

        //Create and initialize database
        database = new HomesDatabase();
        database.init();

        //Upgrade Versions
        VersionUpgrader versionUpgrader = new VersionUpgrader();
        versionUpgrader.startUpgrade();

        //Completions
        completionsHelper = new CompletionsHelper();
        homesCache = new HomesCache();

        //Register Home Migrators
        homeMigrator = new HomeMigrator();
        homeMigrator.registerMigrator(new BasicHomesMigrator());

        //Register Listeners
        timeoutListener = new TimeoutListener(this);
        Bukkit.getPluginManager().registerEvents(timeoutListener, this);
        Bukkit.getPluginManager().registerEvents(new DelayListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HomeDisplayRemover(), this);

        //Teleportations
        teleportations = new HashMap<>();

        //Initialize External Plugins
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PLACEHOLDER_API_EXISTS = true;
            new PlaceholderHomeExpansion().register();
            Logger.DEBUG.log("Registered PlaceholderAPI Expansion");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
            System.out.println("WHYYY DOES THIS NOT WORK!!!");
            PLOTSQUARED_API_EXISTS = true;
            new PlotsquaredIntegration(this).register();
            Logger.DEBUG.log("Registered Plotsquared Expansion");
        }

        //Initialize commands
        System.out.println("Register Commands");
        System.out.println("Sethome");
        registerCommand("sethome", new SetHomeCommand(completionsHelper));
        System.out.println("Delhome");
        registerCommand("delhome", new DelHomeCommand(completionsHelper));
        System.out.println("Home");
        registerCommand("home", new HomeCommand(completionsHelper));
        System.out.println("Homes");
        registerCommand("homes", new HomeListCommand(completionsHelper));

        registerCommand("homeadmin", new HomeAdminCommand());
        registerCommand("homesearch", new HomeSearchCommand());
    }

    private <T extends CommandExecutor & TabCompleter> void registerCommand(String name, T command) {
        getCommand(name).setExecutor(command);
        getCommand(name).setTabCompleter(command);
    }

    public HomesDatabase getDatabase() {
        return database;
    }

    public WorldGroupManager getWorldGroupManager() {
        return worldGroupManager;
    }

    @Override
    public void onDisable() {
        if (homesCache != null) homesCache.destroy();
        HomeSearchCommand.destroy();
    }

    public TimeoutListener getTimeoutListener() {
        return timeoutListener;
    }

    public CompletionsHelper getCompletionsHelper() {
        return completionsHelper;
    }

    public HomesCache getHomesCache() {
        return homesCache;
    }

    public HomeMigrator getHomeMigrator() {
        return homeMigrator;
    }

    public void reloadConfig() {
        config.reload();
    }

    public static HomeManager getInstance() {
        return instance;
    }

    public void createHomeTeleportation(PlayerNameArguments arguments, Home home, int delay, int experience) {
        if (teleportations.containsKey(arguments.getCmdSender().getUniqueId())) {
            teleportations.get(arguments.getCmdSender().getUniqueId()).cancel();
        }
        PlayerTeleportation teleportation = new PlayerTeleportation(arguments, home, experience);
        teleportations.put(arguments.getCmdSender().getUniqueId(), teleportation);
        teleportation.startTeleportation(delay);
    }

    public void removeTeleportation(Player player) {
        teleportations.remove(player.getUniqueId());
    }

    public void cancelTeleportation(UUID uuid) {
        if (!teleportations.containsKey(uuid)) return;
        teleportations.get(uuid).cancel();
    }
}
