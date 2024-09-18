package me.hasenzahn1.homemanager.config;

import me.hasenzahn1.homemanager.HomeManager;

public class LanguageConfig extends CustomConfig {

    public LanguageConfig() {
        super(HomeManager.getInstance(), "lang.yml");
    }
}
