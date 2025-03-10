package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.config.LanguageConfig;

public class Language {

    public static final String NO_PLAYER = "commands.noPlayer";
    public static final String INVALID_COMMAND = "commands.invalidCommand";
    public static final String UNKNOWN_PLAYER = "commands.unknownPlayer";
    public static final String NO_PERMISSION = "commands.noPermission";
    public static final String NO_PERMISSION_OTHER = "commands.noPermissionOther";
    public static final String NO_PERMISSION_GROUP = "commands.noPermissionGroup";
    public static final String UNKNOWN_GROUP = "commands.unknownGroup";
    public static final String UNKNOWN_HOME = "commands.unknownHome";
    public static final String UNKNOWN_HOME_OTHER = "commands.unknownHomeOther";

    public static final String SET_HOME_MAX_HOMES = "commands.sethome.maxHomes";
    public static final String SET_HOME_DUPLICATE_HOME = "commands.sethome.duplicateHome";
    public static final String SET_HOME_DUPLICATE_HOME_OTHER = "commands.sethome.duplicateHomeOther";
    public static final String SET_HOME_NO_EXP = "commands.sethome.noExp";
    public static final String SET_HOME_SUCCESS = "commands.sethome.success";
    public static final String SET_HOME_SUCCESS_OTHER = "commands.sethome.successOther";
    public static final String SET_HOME_INVALID_NAME = "commands.sethome.invalidName";

    public static final String DEL_HOME_SUCCESS = "commands.delhome.success";
    public static final String DEL_HOME_SUCCESS_OTHER = "commands.delhome.successOther";

    public static final String HOME_NO_EXP = "commands.home.noExp";
    public static final String HOME_NOT_ON_GROUND = "commands.home.notOnGround";
    public static final String HOME_TIMEOUT = "commands.home.timeout";

    public static final String HOME_LIST_NO_HOMES = "commands.homeList.noHomes";
    public static final String HOME_LIST_NO_HOMES_OTHER = "commands.homeList.noHomesOther";
    public static final String HOME_LIST_HEADER = "commands.homeList.header";
    public static final String HOME_LIST_HOME = "commands.homeList.home";
    public static final String HOME_LIST_SEPARATOR = "commands.homeList.separator";

    public static final String WARNING_WORLD_NOT_EXISTING_BASE_TEXT = "warnings.worldDoesNotExist.message";
    public static final String WARNING_WORLD_NOT_EXISTING_DELETE_TEXT = "warnings.worldDoesNotExist.deleteMessage";
    public static final String WARNING_HOME_OBSTRUCTED = "warnings.homeObstructed";

    public static final String HOME_ADMIN_MIGRATE_SUCCESS = "commands.homeadmin.migrate.success";

    public static final String TELEPORTATION_DELAY_MESSAGE = "teleportationDelay.message";
    public static final String TELEPORTATION_CANCELLED = "teleportationDelay.teleportationCancelled";
    public static final String TELEPORTATION_SUCCESS = "teleportationDelay.teleportationSuccess";
    public static final String TELEPORTATION_SUCCESS_OTHER = "teleportationDelay.teleportationSuccessOther";

    private static LanguageConfig languageConfig;

    public static void initialize() {
        Logger.DEBUG.log("Initializing language system");
        HomeManager.getInstance().saveResource("lang.yml", DefaultConfig.DEBUG_REPLACE_CONFIG);
        languageConfig = new LanguageConfig();
    }

    public static String getLang(String key, String... args) {
        String lang = languageConfig.getConfig().getString(key, "&cUnknown language key &6" + key);
        for (int i = 0; i + 1 < args.length; i += 2) {
            lang = lang.replace("%" + args[i] + "%", args[i + 1]);
        }
        return lang.replace("&", "§");
    }

    public static boolean containsLang(String key) {
        return languageConfig.getConfig().contains(key);
    }
}
