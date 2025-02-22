package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.commands.*;
import me.hasenzahn1.homemanager.commands.homeadmin.HomeAdminCommand;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.db.HomesDatabase;
import me.hasenzahn1.homemanager.group.WorldGroupManager;
import me.hasenzahn1.homemanager.listener.TimeoutListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HomeManager extends JavaPlugin {

    public static String PREFIX = "[HomeManager]";
    private static HomeManager instance;

    private WorldGroupManager worldGroupManager;
    private HomesDatabase database;

    private DefaultConfig config;

    private CompletionsHelper completionsHelper;

    private TimeoutListener timeoutListener;

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

        //Register Listeners
        timeoutListener = new TimeoutListener(this);
        Bukkit.getPluginManager().registerEvents(timeoutListener, this);

        //Initialize commands
        registerCommand("sethome", new SetHomeCommand(completionsHelper));
        registerCommand("delhome", new DelHomeCommand(completionsHelper));
        registerCommand("home", new HomeCommand(completionsHelper));
        registerCommand("homes", new HomeListCommand(completionsHelper));

        getCommand("homeadmin").setExecutor(new HomeAdminCommand());
    }

    private void registerCommand(String name, BaseHomeCommand command) {
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
    }

    public static HomeManager getInstance() {
        return instance;
    }

    public TimeoutListener getTimeoutListener() {
        return timeoutListener;
    }
}
