package me.hasenzahn1.homemanager.permission;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.command.CommandSender;

/**
 * Utility class to validate permissions for commands in the HomeManager plugin.
 */
public class PermissionValidator {

    /**
     * Checks if the sender has permission to execute a command affecting a player.
     * <p>
     * It first checks for the base permission, then whether the sender is allowed
     * to act on other players.
     *
     * @param sender    The command sender.
     * @param arguments The argument wrapper containing permission logic.
     * @param basePerm  The base permission node (e.g., "homemanager.sethome").
     * @return true if the sender is missing required permissions and a message was sent; false otherwise.
     */
    public static boolean checkInvalidPermissions(CommandSender sender, PlayerNameArguments arguments, String basePerm) {
        // Check base permission
        if (!arguments.senderHasBasePermission(basePerm)) {
            MessageManager.sendMessage(sender, Language.NO_PERMISSION);
            return true;
        }

        // Check "other" permission if player targets differ
        if (!arguments.senderHasOtherPermission(basePerm)) {
            MessageManager.sendMessage(sender, Language.NO_PERMISSION_OTHER);
            return true;
        }
        return false;
    }

    /**
     * Checks if the sender has permission to execute a command affecting a player in a specific group.
     * <p>
     * In addition to base and other permissions, this method also checks for group-specific permission.
     *
     * @param sender    The command sender.
     * @param arguments The argument wrapper containing permission and group information.
     * @param basePerm  The base permission node.
     * @return true if the sender is missing any required permissions and a message was sent; false otherwise.
     */
    public static boolean checkInvalidPermissionsWithGroup(CommandSender sender, PlayerNameGroupArguments arguments, String basePerm) {
        if (checkInvalidPermissions(sender, arguments, basePerm))
            return true;

        // Check group-specific permission
        if (!arguments.senderHasGroupPermission(basePerm)) {
            MessageManager.sendMessage(sender, Language.NO_PERMISSION_GROUP);
            return true;
        }

        return false;
    }

    /**
     * Checks whether the sender has the bypass permission for a specific world group.
     *
     * @param sender     The command sender.
     * @param worldGroup The world group the command affects.
     * @return true if the sender has the bypass permission; false otherwise.
     */
    public static boolean hasBypassPermission(CommandSender sender, WorldGroup worldGroup) {
        return sender.hasPermission("homemanager.bypass." + worldGroup.getName());
    }
}

