package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.entity.Player;

public class PlayerNameGroupArguments extends PlayerNameArguments {

    private final String groupFlagArg;
    private final String groupName;

    private final WorldGroup worldGroup;

    public PlayerNameGroupArguments(Player cmdSender, String optionalPlayerArg, String homeName, String groupFlagArg, String groupName, boolean incorrectNumberOfArguments) {
        super(cmdSender, optionalPlayerArg, homeName, incorrectNumberOfArguments);

        this.groupFlagArg = groupFlagArg;
        this.groupName = groupName;

        worldGroup = HomeManager.getInstance().getWorldGroupManager().getOrDefault(groupName, cmdSenderWorldGroup);
    }

    public boolean senderHasValidGroupPermission(String commandBasePerm) {
        if (cmdSenderWorldGroup.equals(worldGroup)) return true;
        return cmdSender.hasPermission(commandBasePerm + ".group." + worldGroup.getName());
    }

    public boolean groupFlagInvalid() {
        if (groupFlagArg.isEmpty()) return false;
        return !groupFlagArg.equalsIgnoreCase("-g") && !groupFlagArg.equalsIgnoreCase("--group");
    }

    public boolean groupInvalid() {
        if (groupName.isEmpty()) return false;
        return !worldGroup.getName().equalsIgnoreCase(groupName);
    }

    @Override
    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    public String getGroupName() {
        return groupName;
    }

    public static PlayerNameGroupArguments parseArguments(Player cmdSender, String[] args) {
        if (args.length == 0)
            return new PlayerNameGroupArguments(cmdSender, "", "", "", "", true);
        if (args.length == 1)
            return new PlayerNameGroupArguments(cmdSender, cmdSender.getName(), args[0], "", "", false);
        if (args.length == 2)
            return new PlayerNameGroupArguments(cmdSender, args[0], args[1], "", "", false);
        if (args.length == 3)
            return new PlayerNameGroupArguments(cmdSender, cmdSender.getName(), args[0], args[1], args[2], false);
        if (args.length == 4)
            return new PlayerNameGroupArguments(cmdSender, args[0], args[1], args[2], args[3], false);
        return new PlayerNameGroupArguments(cmdSender, "", "", "", "", true);
    }
}
