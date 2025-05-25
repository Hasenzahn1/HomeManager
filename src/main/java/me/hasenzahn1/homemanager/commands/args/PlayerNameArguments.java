package me.hasenzahn1.homemanager.commands.args;

import org.bukkit.entity.Player;

public class PlayerNameArguments extends PlayerArguments {

    private final String homeName;

    public PlayerNameArguments(Player cmdSender, String optionalPlayerArg, String homeName, boolean incorrectNumberOfArguments) {
        super(cmdSender, optionalPlayerArg, incorrectNumberOfArguments);

        this.homeName = homeName;
    }

    public String getHomeName() {
        return homeName;
    }

    public boolean isValidHomeName() {
        return homeName.matches("[!-~]{0,30}");
    }

    public static PlayerNameArguments parseArguments(Player cmdSender, String[] args) {
        if (args.length == 0)
            return new PlayerNameArguments(cmdSender, cmdSender.getName(), "", true);
        if (args.length == 1)
            return new PlayerNameArguments(cmdSender, cmdSender.getName(), args[0], false);
        if (args.length == 2)
            return new PlayerNameArguments(cmdSender, args[0], args[1], false);
        return new PlayerNameArguments(cmdSender, "", "", true);
    }
}
