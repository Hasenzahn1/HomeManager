package me.hasenzahn1.homemanager.migration;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class BasicHomesMigrator extends PluginMigrator {

    @Override
    public void migrateAll() {
        ArrayList<HomeData> data = getDataFromMigrationFolder();
        DatabaseAccessor session = DatabaseAccessor.openSession();
        session.bulkAddHomeFromMigration(data);
        session.destroy();
    }

    @Override
    public void migrate(String world) {
        ArrayList<HomeData> data = getDataFromMigrationFolder();
        DatabaseAccessor session = DatabaseAccessor.openSession();
        session.bulkAddHomeFromMigration(data.stream().filter(d -> d.world().equalsIgnoreCase(world)).toList());
        session.destroy();
    }

    private ArrayList<HomeData> getDataFromMigrationFolder() {
        File importFolder = new File(HomeManager.getInstance().getDataFolder(), "migrate");
        if (!importFolder.exists()) importFolder.mkdirs();

        ArrayList<HomeData> datas = new ArrayList<>();
        for (File file : importFolder.listFiles()) {
            YamlConfiguration config = getYamlFromFile(file);

            UUID player = UUID.fromString(file.getName().replace(".yml", ""));

            ConfigurationSection section = config.getConfigurationSection("Home");
            if (section == null) continue;

            for (String home : section.getKeys(false)) {
                HomeData data = parseHomeEntry(player, section, home);
                datas.add(data);
            }
        }
        datas.removeIf(Objects::isNull);
        return datas;
    }

    private HomeData parseHomeEntry(UUID uuid, ConfigurationSection section, String home) {
        String homeWorld = section.getString(home + ".World");
        double homeX = section.getDouble(home + ".X");
        double homeY = section.getDouble(home + ".Y");
        double homeZ = section.getDouble(home + ".Z");
        double homeYaw = section.getDouble(home + ".Yaw");
        double homePitch = section.getDouble(home + ".Pitch");

        if (homeWorld != null)
            return new HomeData(uuid, home, homeWorld, homeX, homeY, homeZ, (float) homeYaw, (float) homePitch);
        return null;
    }

    public YamlConfiguration getYamlFromFile(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

}
