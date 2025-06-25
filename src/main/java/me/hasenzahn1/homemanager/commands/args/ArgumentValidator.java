package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Validates command arguments related to player and player group operations.
 * This class provides methods for checking invalid arguments, such as invalid player names or group names,
 * and sending appropriate error messages to the user.
 */
public class ArgumentValidator {

    /**
     * Checks if the arguments related to the player are invalid.
     * <p>
     * This method validates whether the player arguments are missing or incorrect,
     * and sends an error message to the sender if any issues are found.
     *
     * @param sender    the sender of the command (player or console)
     * @param arguments the player name arguments to validate
     * @param command   the command that was executed
     * @return true if there are invalid arguments, false otherwise
     */
    public static boolean checkInvalidPlayerArgs(CommandSender sender, PlayerNameArguments arguments, Command command) {
        // Check if the arguments are invalid (e.g., missing or incorrect player name)
        if (arguments.invalidArguments()) {
            MessageManager.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        // Check if the specified player argument is invalid
        if (arguments.playerArgInvalid()) {
            MessageManager.sendMessage(sender, Language.UNKNOWN_PLAYER, "player", arguments.getOptionalPlayerArg());
            return true;
        }
        return false;
    }

    /**
     * Checks if the arguments related to the player and group are invalid.
     * <p>
     * This method validates both the player and group arguments and sends error messages if any issues are found.
     * It uses the {@link #checkInvalidPlayerArgs(CommandSender, PlayerNameArguments, Command)} method to check player arguments.
     *
     * @param sender    the sender of the command (player or console)
     * @param arguments the player name and group arguments to validate
     * @param command   the command that was executed
     * @return true if there are invalid arguments, false otherwise
     */
    public static boolean checkInvalidPlayerGroupArgs(CommandSender sender, PlayerNameGroupArguments arguments, Command command) {
        // First, check if the player arguments are invalid
        if (checkInvalidPlayerArgs(sender, arguments, command)) return true;

        // Check if the group flag is invalid
        if (arguments.groupFlagInvalid()) {
            MessageManager.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        // Check if the specified group is invalid
        if (arguments.groupInvalid()) {
            MessageManager.sendMessage(sender, Language.UNKNOWN_GROUP, "group", arguments.getGroupName());
            return true;
        }
        return false;
    }

}
