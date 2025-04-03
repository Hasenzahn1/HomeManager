package me.hasenzahn1.homemanager.migration;

import java.util.List;

public abstract class BaseHomeMigrator {

    public abstract List<HomeMigrator.HomeData> migrateAll();

    public abstract List<HomeMigrator.HomeData> migrate(String world);

    public abstract String getName();
}
