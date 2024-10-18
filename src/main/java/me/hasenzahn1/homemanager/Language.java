package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.config.LanguageConfig;
import me.hasenzahn1.homemanager.group.WorldGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class Language {

    public static final String NO_PLAYER = "commands.noPlayer";
    public static final String INVALID_COMMAND = "commands.invalidCommand";
    public static final String UNKNOWN_PLAYER = "commands.unknownPlayer";
    public static final String NO_PERMISSION = "commands.noPermission";
    public static final String NO_PERMISSION_OTHER = "commands.noPermissionOther";
    public static final String NO_PERMISSION_GROUP = "commands.noPermissionGroup";
    public static final String UNKNOWN_GROUP = "commands.unknownGroup";
    public static final String UNKNOWN_HOME = "commands.unknownHome";

    public static final String SET_HOME_MAX_HOMES = "commands.sethome.maxHomes";
    public static final String SET_HOME_DUPLICATE_HOME = "commands.sethome.duplicateHome";
    public static final String SET_HOME_NO_EXP = "commands.sethome.noExp";
    public static final String SET_HOME_SUCCESS = "commands.sethome.success";

    public static final String DEL_HOME_SUCCESS = "commands.delhome.success";

    public static final String HOME_SUCCESS = "commands.home.success";
    public static final String HOME_LIST_HEADER = "commands.homeList.header";

    public static final String HOME_ADMIN_MIGRATE_SUCCESS = "commands.homeadmin.migrate.success";

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

    public static void sendInvalidArgumentMessage(Player player, Command command, boolean displayHomeNameArg, WorldGroup worldGroup) {
        String cmd = command.getName().equalsIgnoreCase("homes") ? "homelist" : command.getName().toLowerCase();

        boolean hasOtherPermission = player.hasPermission("homemanager.commands." + cmd + ".other." + worldGroup.getName());
        boolean hasGroupPermission = player.hasPermission("homemanager.commands." + cmd + ".group." + worldGroup.getName());
        if (command.getName().toLowerCase().contains("set")) hasGroupPermission = false;

        String cmdSyntax = "/" + command.getName().toLowerCase() + " ";
        if (hasOtherPermission) cmdSyntax += "(player) ";
        if (displayHomeNameArg) cmdSyntax += "<homename> ";
        if (hasGroupPermission) cmdSyntax += "(-g group) ";
        String basetext = getLang(INVALID_COMMAND, "command", cmdSyntax);

        player.sendMessage(Component.text(HomeManager.PREFIX + basetext));
    }


}
