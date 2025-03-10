package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.commands.args.PlayerGroupArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.group.WorldGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageManager {

    public static void sendInvalidArgumentMessage(Player player, Command command, boolean displayHomeNameArg, WorldGroup worldGroup) {
        String cmd = command.getName().equalsIgnoreCase("homes") ? "homelist" : command.getName().toLowerCase();

        boolean hasOtherPermission = player.hasPermission("homemanager.commands." + cmd + ".other." + worldGroup.getName());
        boolean hasGroupPermission = player.hasPermission("homemanager.commands." + cmd + ".group." + worldGroup.getName());
        if (command.getName().toLowerCase().contains("set")) hasGroupPermission = false;

        String cmdSyntax = "/" + command.getName().toLowerCase() + " ";
        if (hasOtherPermission) cmdSyntax += "(player) ";
        if (displayHomeNameArg) cmdSyntax += "<homename> ";
        if (hasGroupPermission) cmdSyntax += "(-g group) ";
        String basetext = Language.getLang(Language.INVALID_COMMAND, "command", cmdSyntax);

        player.sendMessage(Component.text(HomeManager.PREFIX + basetext));
    }

    public static void sendUnknownHomeMessage(PlayerNameGroupArguments arguments) {
        if (arguments.isSelf()) {
            sendMessage(arguments.getCmdSender(), Language.UNKNOWN_HOME, "name", arguments.getHomeName());
        } else {
            sendMessage(arguments.getCmdSender(), Language.UNKNOWN_HOME_OTHER, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName(), "name", arguments.getHomeName());
        }
    }

    public static void sendMessage(CommandSender player, String languageKey, String... replacements) {
        boolean messageHasBeenSent = false;
        if (Language.containsLang(languageKey + "Actionbar")) {
            messageHasBeenSent = sendActionbarMessage(player, languageKey + "Actionbar", replacements);
        }

        if (Language.containsLang(languageKey) || !messageHasBeenSent) {
            player.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(languageKey, replacements)));
        }
    }

    private static boolean sendActionbarMessage(CommandSender player, String languageKey, String... replacements) {
        if (!(player instanceof Player)) return false;

        String message = Language.getLang(languageKey, replacements);
        player.sendActionBar(Component.text(message));
        return true;
    }

    public static void sendNoHomesMessage(PlayerGroupArguments arguments) {
        if (arguments.isSelf()) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_LIST_NO_HOMES);
        } else {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_LIST_NO_HOMES_OTHER, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName());
        }
    }

    public static void sendDuplicateHomesMessage(CommandSender sender, PlayerNameArguments arguments) {
        if (arguments.isSelf()) {
            MessageManager.sendMessage(sender, Language.SET_HOME_DUPLICATE_HOME, "name", arguments.getHomeName());
        } else {
            MessageManager.sendMessage(sender, Language.SET_HOME_DUPLICATE_HOME_OTHER, "name", arguments.getHomeName(), "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName());
        }
    }

}
