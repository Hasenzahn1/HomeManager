package me.hasenzahn1.homemanager.db;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.db.system.Database;
import me.hasenzahn1.homemanager.db.tables.GroupInfosTable;
import me.hasenzahn1.homemanager.db.tables.HomesTable;
import me.hasenzahn1.homemanager.db.tables.VersionTable;

public class HomesDatabase extends Database {

    public HomesDatabase() {
        super(HomeManager.getInstance(), "data/homes");

        addTable(new HomesTable(this));
        addTable(new GroupInfosTable(this));
        addTable(new VersionTable(this));
    }
}