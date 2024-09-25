package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.commands.DelHomeCommand;
import me.hasenzahn1.homemanager.commands.HomeCommand;
import me.hasenzahn1.homemanager.commands.SetHomeCommand;
import me.hasenzahn1.homemanager.db.HomesDatabase;
import me.hasenzahn1.homemanager.group.WorldGroupManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HomeManager extends JavaPlugin {

    public static String PREFIX = "[HomeManager]";
    private static HomeManager instance;

    private WorldGroupManager worldGroupManager;
    private HomesDatabase database;

    @Override
    public void onEnable() {
        instance = this;

        //Initialize Language
        Language.initialize();
        PREFIX = Language.getLang("prefix");

        //Load groups form config
        worldGroupManager = new WorldGroupManager();

        //Create and initialize database
        database = new HomesDatabase();
        database.init();

        //Initialize commands
        getCommand("sethome").setExecutor(new SetHomeCommand());
        getCommand("delhome").setExecutor(new DelHomeCommand());
        getCommand("home").setExecutor(new HomeCommand());
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
}
