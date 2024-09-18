package me.hasenzahn1.homemanager.config;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CustomConfig {

    private File configFile;
    private FileConfiguration config;
    private JavaPlugin plugin;

    public CustomConfig(JavaPlugin plugin, String name) {
        this.plugin = plugin;

        createCustomConfigFromName(plugin, name);
    }

    public CustomConfig(JavaPlugin plugin, File configFile) {
        this.plugin = plugin;

        createCustomConfigFromFile(configFile);
    }

    /**
     * Creates and loads a custom config file from the jar
     *
     * @param plugin The plugin loading the config
     * @param name   The name of the config file
     */
    public void createCustomConfigFromName(JavaPlugin plugin, String name) {
        configFile = new File(plugin.getDataFolder(), name);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Loads a new config from a file.
     *
     * @param file The file to load from
     */
    public void createCustomConfigFromFile(File file) {
        this.configFile = file;
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Reloads the config from the given file
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        final InputStream defConfigStream = plugin.getResource("config.yml");
        if (defConfigStream == null) {
            return;
        }

        config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    /**
     * Saves a config to the disk
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the yaml config.
     *
     * @return
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
