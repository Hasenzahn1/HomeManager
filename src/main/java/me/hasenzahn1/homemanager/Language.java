package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.config.LanguageConfig;

public class Language {

    public static final String NO_PLAYER = "commands.noPlayer";
    public static final String INVALID_COMMAND = "commands.invalidCommand";
    public static final String UNKNOWN_PLAYER = "commands.unknownPlayer";
    public static final String NO_PERMISSION = "commands.noPermission";
    public static final String NO_PERMISSION_OTHER = "commands.noPermissionOther";
    public static final String SET_HOME_INVALID_HOME_NAME = "commands.sethome.invalidHomeName";

    private static LanguageConfig languageConfig;

    public static void initialize() {
        languageConfig = new LanguageConfig();
    }

    public static String getLang(String key, String... args) {
        String lang = languageConfig.getConfig().getString(key, "&cUnknown language key &6" + key);
        for (int i = 0; i + 1 < args.length; i += 2) {
            lang = lang.replace("%" + args[i] + "%", args[i + 1]);
        }
        return lang.replace("&", "§");
    }


}
