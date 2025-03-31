package me.hasenzahn1.homemanager.permission;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.command.CommandSender;

public class PermissionValidator {

    public static boolean checkInvalidPermissions(CommandSender sender, PlayerNameArguments arguments, String basePerm) {
        //Check base sethome permission
        if (!arguments.senderHasBasePermission(basePerm)) {
            MessageManager.sendMessage(sender, Language.NO_PERMISSION);
            return true;
        }

        //Check other Permission (no permission and players differ)
        if (!arguments.senderHasOtherPermission(basePerm)) {
            MessageManager.sendMessage(sender, Language.NO_PERMISSION_OTHER);
            return true;
        }
        return false;
    }

    public static boolean checkInvalidPermissionsWithGroup(CommandSender sender, PlayerNameGroupArguments arguments, String basePerm) {
        if (checkInvalidPermissions(sender, arguments, basePerm))
            return true;

        //Check if the player has the required .group.<group> permission if requested
        if (!arguments.senderHasGroupPermission(basePerm)) {
            MessageManager.sendMessage(sender, Language.NO_PERMISSION_GROUP);
            return true;
        }

        return false;
    }

    public static boolean hasBypassPermission(CommandSender sender, WorldGroup worldGroup) {
        return sender.hasPermission("homemanager.bypass." + worldGroup.getName());
    }

}
