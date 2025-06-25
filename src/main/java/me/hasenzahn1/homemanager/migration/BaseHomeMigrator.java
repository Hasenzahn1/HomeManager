package me.hasenzahn1.homemanager.migration;

import java.util.List;

/**
 * Abstract base class for implementing home data migration from other plugins or systems.
 * <p>
 * Implementations should provide logic to extract existing home data from an external source
 * and convert it into {@link HomeMigrator.HomeData} objects that can be imported into the current system.
 */
public abstract class BaseHomeMigrator {

    /**
     * Migrates all homes available in the external system.
     *
     * @return A list of {@link HomeMigrator.HomeData} objects representing all homes to migrate.
     */
    public abstract List<HomeMigrator.HomeData> migrateAll();

    /**
     * Migrates homes from a specific world.
     *
     * @param world The name of the world to filter homes by.
     * @return A list of {@link HomeMigrator.HomeData} for the given world.
     */
    public abstract List<HomeMigrator.HomeData> migrate(String world);

    /**
     * Returns the name of this migrator used for the migrate sub command. Typically this would be the name of the plugin
     * or system from which homes are being imported (e.g., "Essentials", "SetHomePlus").
     *
     * @return A human-readable name for this migrator.
     */
    public abstract String getName();
}
