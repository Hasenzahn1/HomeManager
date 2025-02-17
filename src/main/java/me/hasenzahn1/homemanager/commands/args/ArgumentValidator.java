package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ArgumentValidator {

    public static boolean checkInvalidPlayerArgs(CommandSender sender, PlayerNameArguments arguments, Command command) {
        if (arguments.invalidArguments()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        //No valid set player
        if (arguments.playerArgInvalid()) {
            Language.sendMessage(sender, Language.UNKNOWN_PLAYER, "name", arguments.getOptionalPlayerArg());
            return true;
        }
        return false;
    }

    public static boolean checkInvalidPlayerGroupArgs(CommandSender sender, PlayerNameGroupArguments arguments, Command command) {
        if (checkInvalidPlayerArgs(sender, arguments, command)) return true;

        if (arguments.groupFlagInvalid()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        if (arguments.groupInvalid()) {
            Language.sendMessage(sender, Language.UNKNOWN_GROUP, "name", arguments.getGroupName());
            return true;
        }
        return false;
    }

}
