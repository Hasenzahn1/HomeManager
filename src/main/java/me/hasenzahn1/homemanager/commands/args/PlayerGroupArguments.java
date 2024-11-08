package me.hasenzahn1.homemanager.commands.args;

import org.bukkit.entity.Player;

public class PlayerGroupArguments extends PlayerNameGroupArguments {


    public PlayerGroupArguments(Player cmdSender, String optionalPlayerArg, String groupFlagArg, String groupName, boolean incorrectNumberOfArguments) {
        super(cmdSender, optionalPlayerArg, "", groupFlagArg, groupName, incorrectNumberOfArguments);
    }

    public static PlayerGroupArguments parseArguments(Player cmdSender, String[] args) {
        if (args.length == 0)
            return new PlayerGroupArguments(cmdSender, cmdSender.getName(), "", "", false);
        if (args.length == 1)
            return new PlayerGroupArguments(cmdSender, args[0], "", "", false);
        if (args.length == 2)
            return new PlayerGroupArguments(cmdSender, cmdSender.getName(), args[0], args[1], false);
        if (args.length == 3)
            return new PlayerGroupArguments(cmdSender, args[0], args[1], args[2], false);
        return new PlayerGroupArguments(cmdSender, "", "", "", true);
    }
}
