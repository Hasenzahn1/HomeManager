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

public class BasicHomesMigrator extends BaseHomeMigrator {

    @Override
    public List<HomeMigrator.HomeData> migrateAll() {
        return getDataFromMigrationFolder();
    }

    @Override
    public List<HomeMigrator.HomeData> migrate(String world) {
        ArrayList<HomeMigrator.HomeData> data = getDataFromMigrationFolder();
        return data.stream().filter(d -> d.world().equalsIgnoreCase(world)).toList();
    }

    @Override
    public String getName() {
        return "basichomes";
    }

    private ArrayList<HomeMigrator.HomeData> getDataFromMigrationFolder() {
        File importFolder = new File(HomeManager.getInstance().getDataFolder(), "migrate");
        if (!importFolder.exists()) importFolder.mkdirs();

        ArrayList<HomeMigrator.HomeData> datas = new ArrayList<>();
        for (File file : importFolder.listFiles()) {
            YamlConfiguration config = getYamlFromFile(file);

            UUID player = UUID.fromString(file.getName().replace(".yml", ""));

            ConfigurationSection section = config.getConfigurationSection("Home");
            if (section == null) continue;

            for (String home : section.getKeys(false)) {
                HomeMigrator.HomeData data = parseHomeEntry(player, section, home);

                if (data == null) Logger.WARN.log("Could not parse home key " + home + " from file " + file.getName());

                datas.add(data);
            }
        }
        datas.removeIf(Objects::isNull);
        return datas;
    }

    private HomeMigrator.HomeData parseHomeEntry(UUID uuid, ConfigurationSection section, String home) {
        String homeWorld = section.getString(home + ".World");
        double homeX = section.getDouble(home + ".X");
        double homeY = section.getDouble(home + ".Y");
        double homeZ = section.getDouble(home + ".Z");
        double homeYaw = section.getDouble(home + ".Yaw");
        double homePitch = section.getDouble(home + ".Pitch");

        if (homeWorld != null)
            return new HomeMigrator.HomeData(uuid, home, homeWorld, homeX, homeY, homeZ, (float) homeYaw, (float) homePitch);
        return null;
    }

    public YamlConfiguration getYamlFromFile(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

}
