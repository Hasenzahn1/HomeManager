package me.hasenzahn1.homemanager;

import me.clip.placeholderapi.PlaceholderAPI;
import me.hasenzahn1.homemanager.commands.args.PlayerGroupArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.group.WorldGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Manages sending messages to players in various scenarios, including invalid arguments,
 * unknown homes, and home-related messages. It provides methods to send regular messages,
 * action bar messages, and messages for specific home-related errors.
 */
public class MessageManager {

    /**
     * Sends a message informing the player about the correct command syntax when an invalid argument is provided.
     * <p>
     * This includes details about the command usage, the player argument, and the group argument based on the permissions
     * the player has.
     *
     * @param player             the player who receives the message
     * @param command            the command the player executed
     * @param displayHomeNameArg flag indicating whether the home name argument should be displayed
     * @param worldGroup         the world group context for the command
     */
    public static void sendInvalidArgumentMessage(Player player, Command command, boolean displayHomeNameArg, WorldGroup worldGroup) {
        String cmd = command.getName().equalsIgnoreCase("homes") ? "homelist" : command.getName().toLowerCase();

        boolean hasOtherPermission = player.hasPermission("homemanager.commands." + cmd + ".other." + worldGroup.getName());
        boolean hasGroupPermission = player.hasPermission("homemanager.commands." + cmd + ".group." + worldGroup.getName());
        if (command.getName().toLowerCase().contains("set")) hasGroupPermission = false;

        String cmdSyntax = "/" + command.getName().toLowerCase() + " ";
        if (hasOtherPermission) cmdSyntax += "(player) ";
        if (displayHomeNameArg) cmdSyntax += "<homename> ";
        if (hasGroupPermission) cmdSyntax += "(-g group) ";

        MessageManager.sendMessage(player, Language.INVALID_COMMAND, "command", cmdSyntax);
    }

    /**
     * Sends a message informing the player that the home they specified was not found.
     * <p>
     * If the home belongs to the player themselves, a specific message is sent, otherwise, the message includes the player's name.
     *
     * @param arguments the arguments containing the home name and the player details
     */
    public static void sendUnknownHomeMessage(PlayerNameGroupArguments arguments) {
        if (arguments.isSelf()) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.UNKNOWN_HOME, "homename", arguments.getHomeName());
        } else {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.UNKNOWN_HOME_OTHER, "player", arguments.getOptionalPlayerName(), "homename", arguments.getHomeName());
        }
    }

    /**
     * Sends a message to the player using the specified language key and optional replacements for placeholders.
     * <p>
     * The message can be sent either as a regular message or as an action bar message based on the configuration.
     * If the config contains the {@code <key>Actionbar} key, the message is sent as an actionbar message.
     * If the config contains the {@code <key>} key the message will be sent as a normal message.
     * <p>
     * Applies PAPI Placeholders if PlaceholderAPI is available
     *
     * @param player       the player or command sender to receive the message
     * @param languageKey  the language key to retrieve the message
     * @param replacements the key-value pairs to replace placeholders in the message
     */
    public static void sendMessage(CommandSender player, String languageKey, String... replacements) {
        boolean messageHasBeenSent = false;
        if (Language.containsLang(languageKey + "Actionbar")) {
            messageHasBeenSent = sendActionbarMessage(player, languageKey + "Actionbar", replacements);
        }

        if (Language.containsLang(languageKey) || !messageHasBeenSent) {
            String message = getPAPILang(player, languageKey, replacements);
            player.sendMessage(Component.text(HomeManager.PREFIX + message));
        }
    }

    public static String getPAPILang(CommandSender player, String languageKey, String... replacements) {
        String message = Language.getLang(languageKey);

        //Apply Papi placeholders
        if (player instanceof OfflinePlayer && HomeManager.PLACEHOLDER_API_EXISTS) {
            message = PlaceholderAPI.setPlaceholders((OfflinePlayer) player, message);
        }

        //Apply plugin internal replacements
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        return message;
    }

    /**
     * Sends a message to the player as an action bar message.
     * <p>
     * Applies PAPI Placeholders if PlaceholderAPI is available
     *
     * @param player       the player to receive the action bar message
     * @param languageKey  the language key to retrieve the action bar message
     * @param replacements the key-value pairs to replace placeholders in the message
     * @return true if the message was successfully sent, false otherwise
     */
    private static boolean sendActionbarMessage(CommandSender player, String languageKey, String... replacements) {
        if (!(player instanceof Player)) return false;
        String message = getPAPILang(player, languageKey, replacements);
        player.sendActionBar(Component.text(message));
        return true;
    }

    /**
     * Sends a message to the player informing them that they have no homes.
     * <p>
     * If the player is querying their own homes, a different message is sent than when querying another player's homes.
     *
     * @param arguments the arguments containing the player details
     */
    public static void sendNoHomesMessage(PlayerGroupArguments arguments) {
        if (arguments.isSelf()) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_LIST_NO_HOMES);
        } else {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_LIST_NO_HOMES_OTHER, "player", arguments.getOptionalPlayerName());
        }
    }

    /**
     * Sends a message to the player when they try to set a duplicate home.
     * <p>
     * The message varies depending on whether the home belongs to the player themselves or another player.
     *
     * @param sender    the command sender (player or console)
     * @param arguments the arguments containing the home details and player information
     */
    public static void sendDuplicateHomesMessage(CommandSender sender, PlayerNameArguments arguments) {
        if (arguments.isSelf()) {
            MessageManager.sendMessage(sender, Language.SET_HOME_DUPLICATE_HOME, "homename", arguments.getHomeName());
        } else {
            MessageManager.sendMessage(sender, Language.SET_HOME_DUPLICATE_HOME_OTHER, "homename", arguments.getHomeName(), "player", arguments.getOptionalPlayerName());
        }
    }
}
