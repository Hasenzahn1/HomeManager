package me.hasenzahn1.homemanager.migration;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Migrates home data from the BasicHomes format.
 * <p>
 * Expected file structure:
 * Each player has a file named <code>(uuid).yml</code> in the <code>migrate</code> folder.
 * Each file contains a "Home" section listing named homes with location data.
 * <p>
 * Example YAML structure:
 * <pre>{@code
 * Home:
 *   spawn:
 *     World: world
 *     X: 100.5
 *     Y: 64
 *     Z: -20.3
 *     Yaw: 90.0
 *     Pitch: 0.0
 * }</pre>
 */
public class BasicHomesMigrator extends BaseHomeMigrator {

    /**
     * Loads all home data from every file in the <code>/migrate</code> folder.
     *
     * @return A list of {@link HomeMigrator.HomeData} containing all imported homes.
     */
    @Override
    public List<HomeMigrator.HomeData> migrateAll() {
        return getDataFromMigrationFolder();
    }

    /**
     * Loads home data only for the given world name.
     *
     * @param world The name of the world to filter homes by.
     * @return A filtered list of {@link HomeMigrator.HomeData} for that world.
     */
    @Override
    public List<HomeMigrator.HomeData> migrate(String world) {
        ArrayList<HomeMigrator.HomeData> data = getDataFromMigrationFolder();
        return data.stream().filter(d -> d.world().equalsIgnoreCase(world)).toList();
    }

    /**
     * @return The name of this migrator used to identify it in migration tools or logs.
     */
    @Override
    public String getName() {
        return "basichomes";
    }

    /**
     * Loads all valid home data from YAML files in the migration folder.
     *
     * @return A list of parsed {@link HomeMigrator.HomeData}.
     */
    private ArrayList<HomeMigrator.HomeData> getDataFromMigrationFolder() {
        File importFolder = new File(HomeManager.getInstance().getDataFolder(), "migrate");
        if (!importFolder.exists()) importFolder.mkdirs();

        ArrayList<HomeMigrator.HomeData> datas = new ArrayList<>();
        for (File file : Objects.requireNonNull(importFolder.listFiles())) {
            if (!file.getName().endsWith(".yml")) continue;

            UUID player;
            try {
                player = UUID.fromString(file.getName().replace(".yml", ""));
            } catch (IllegalArgumentException e) {
                Logger.WARN.log("Skipping file with invalid UUID name: " + file.getName());
                continue;
            }

            YamlConfiguration config = getYamlFromFile(file);
            ConfigurationSection section = config.getConfigurationSection("Home");
            if (section == null) continue;

            for (String home : section.getKeys(false)) {
                HomeMigrator.HomeData data = parseHomeEntry(player, section, home);
                if (data == null) {
                    Logger.WARN.log("Could not parse home '" + home + "' in file " + file.getName());
                    continue;
                }
                datas.add(data);
            }
        }

        return datas;
    }

    /**
     * Parses a single home entry from a player's config section.
     *
     * @param uuid    The UUID of the player owning the home.
     * @param section The configuration section containing home data.
     * @param home    The name of the home to parse.
     * @return A {@link HomeMigrator.HomeData} object, or null if parsing failed.
     */
    private HomeMigrator.HomeData parseHomeEntry(UUID uuid, ConfigurationSection section, String home) {
        String homeWorld = section.getString(home + ".World");
        if (homeWorld == null) return null;

        double homeX = section.getDouble(home + ".X");
        double homeY = section.getDouble(home + ".Y");
        double homeZ = section.getDouble(home + ".Z");
        float homeYaw = (float) section.getDouble(home + ".Yaw");
        float homePitch = (float) section.getDouble(home + ".Pitch");

        return new HomeMigrator.HomeData(uuid, home, homeWorld, homeX, homeY, homeZ, homeYaw, homePitch);
    }

    /**
     * Loads a YAML configuration from a given file.
     *
     * @param file The YAML file to load.
     * @return A {@link YamlConfiguration} representing the file content.
     */
    public YamlConfiguration getYamlFromFile(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }
}
