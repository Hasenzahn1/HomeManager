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
import me.hasenzahn1.homemanager.listener.DelayListener;
import me.hasenzahn1.homemanager.listener.HomeDisplayRemover;
import me.hasenzahn1.homemanager.listener.TimeoutListener;
import me.hasenzahn1.homemanager.papi.PlaceholderHomeExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class HomeManager extends JavaPlugin {

    public static String PREFIX = "[HomeManager]";
    private static HomeManager instance;

    private WorldGroupManager worldGroupManager;
    private HomesDatabase database;

    private DefaultConfig config;

    private CompletionsHelper completionsHelper;
    private HomesCache homesCache;

    private TimeoutListener timeoutListener;

    private HashMap<UUID, PlayerTeleportation> teleportations;

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

        //Completions
        completionsHelper = new CompletionsHelper();
        homesCache = new HomesCache();

        //Register Listeners
        timeoutListener = new TimeoutListener(this);
        Bukkit.getPluginManager().registerEvents(timeoutListener, this);
        Bukkit.getPluginManager().registerEvents(new DelayListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HomeDisplayRemover(), this);

        //Teleportations
        teleportations = new HashMap<>();

        //Initialize commands
        registerCommand("sethome", new SetHomeCommand(completionsHelper));
        registerCommand("delhome", new DelHomeCommand(completionsHelper));
        registerCommand("home", new HomeCommand(completionsHelper));
        registerCommand("homes", new HomeListCommand(completionsHelper));

        registerCommand("homeadmin", new HomeAdminCommand());
        registerCommand("homesearch", new HomeSearchCommand());

        //Initialize External Plugins
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new PlaceholderHomeExpansion().register(); //
            Logger.DEBUG.log("Registered PlaceholderAPI Expansion");
        }
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
        homesCache.destroy();
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
