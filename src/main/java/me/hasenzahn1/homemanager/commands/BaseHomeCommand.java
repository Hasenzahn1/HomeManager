package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class BaseHomeCommand implements CommandExecutor {

    public boolean checkInvalidPermissions(CommandSender sender, PlayerNameArguments arguments, String basePerm) {
        //Check base sethome permission
        if (!arguments.senderHasBasePermission(basePerm)) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check other Permission (no permission and players differ)
        if (!arguments.senderHasOtherPermission(basePerm)) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }
        return false;
    }

    public boolean checkInvalidPermissionsWithGroup(CommandSender sender, PlayerNameGroupArguments arguments, String basePerm) {
        if (checkInvalidPermissions(sender, arguments, basePerm))
            return true;

        //Check if the player has the required .group.<group> permission if requested
        if (!arguments.senderHasGroupPermission(basePerm)) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_GROUP)));
            return true;
        }

        return false;
    }

    public boolean checkInvalidPlayerArgs(CommandSender sender, PlayerNameArguments arguments, Command command) {
        if (arguments.invalidArguments()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        //No valid set player
        if (arguments.playerArgInvalid()) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", arguments.getOptionalPlayerArg())));
            return true;
        }
        return false;
    }

    public boolean checkInvalidPlayerGroupArgs(CommandSender sender, PlayerNameGroupArguments arguments, Command command) {
        if (checkInvalidPlayerArgs(sender, arguments, command)) return true;

        if (arguments.groupFlagInvalid()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        if (arguments.groupInvalid()) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_GROUP, "name", arguments.getGroupName())));
            return true;
        }
        return false;
    }

}