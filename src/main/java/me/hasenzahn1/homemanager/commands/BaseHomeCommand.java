package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class BaseHomeCommand implements CommandExecutor, TabCompleter {

    protected final CompletionsHelper completionsHelper;

    public BaseHomeCommand(CompletionsHelper completionsHelper) {
        this.completionsHelper = completionsHelper;
    }

    public boolean checkInvalidPermissions(CommandSender sender, PlayerNameArguments arguments, String basePerm) {
        //Check base sethome permission
        if (!arguments.senderHasBasePermission(basePerm)) {
            Language.sendMessage(sender, Language.NO_PERMISSION);
            return true;
        }

        //Check other Permission (no permission and players differ)
        if (!arguments.senderHasOtherPermission(basePerm)) {
            Language.sendMessage(sender, Language.NO_PERMISSION_OTHER);
            return true;
        }
        return false;
    }

    public boolean checkInvalidPermissionsWithGroup(CommandSender sender, PlayerNameGroupArguments arguments, String basePerm) {
        if (checkInvalidPermissions(sender, arguments, basePerm))
            return true;

        //Check if the player has the required .group.<group> permission if requested
        if (!arguments.senderHasGroupPermission(basePerm)) {
            Language.sendMessage(sender, Language.NO_PERMISSION_GROUP);
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
            Language.sendMessage(sender, Language.UNKNOWN_PLAYER, "name", arguments.getOptionalPlayerArg());
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
            Language.sendMessage(sender, Language.UNKNOWN_GROUP, "name", arguments.getGroupName());
            return true;
        }
        return false;
    }

}
