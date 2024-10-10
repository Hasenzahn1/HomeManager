package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HomeAndDelHomeArguments {

    private final Player cmdSender;

    private final String actionPlayer;
    private final String homeName;

    private final UUID cmdSenderUUID;
    private final UUID actionPlayerUUID;

    private final String groupFlag;
    private final String groupFlagArg;
    private final WorldGroup worldGroup;
    private final WorldGroup sendersCurrentWorldGroup;

    private final boolean incorrectNumberOfArguments;


    private HomeAndDelHomeArguments(Player cmdSender, String actionPlayer, String homeName, String worldGroupName, boolean incorrectNumberOfArguments) {
        this(cmdSender, actionPlayer, homeName, "", worldGroupName, incorrectNumberOfArguments);
    }

    private HomeAndDelHomeArguments(Player cmdSender, String actionPlayer, String homeName, String groupFlag, String worldGroupName, boolean incorrectNumberOfArguments) {
        this.cmdSender = cmdSender;

        this.actionPlayer = actionPlayer;
        this.homeName = homeName;

        this.cmdSenderUUID = cmdSender.getUniqueId();
        if (!actionPlayer.isEmpty() && Bukkit.getOfflinePlayerIfCached(actionPlayer) != null)
            actionPlayerUUID = Bukkit.getOfflinePlayerIfCached(actionPlayer).getUniqueId();
        else actionPlayerUUID = null;

        this.groupFlagArg = worldGroupName;
        this.groupFlag = groupFlag;

        this.sendersCurrentWorldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(cmdSender.getWorld());
        this.worldGroup = HomeManager.getInstance().getWorldGroupManager().getOrDefault(worldGroupName, sendersCurrentWorldGroup);


        this.incorrectNumberOfArguments = incorrectNumberOfArguments;
    }

    public boolean isValidArguments() {
        return !incorrectNumberOfArguments;
    }

    public boolean isArgPlayerValid() {
        return actionPlayerUUID != null;
    }

    public boolean isSelf() {
        return cmdSenderUUID.equals(actionPlayerUUID);
    }

    public boolean senderHasValidOtherPermission(String commandBasePerm) {
        if (isSelf()) return true;
        return cmdSender.hasPermission(commandBasePerm + ".other." + worldGroup.getName());
    }

    public boolean senderHasValidGroupPermission(String commandBasePerm) {
        if (sendersCurrentWorldGroup.equals(worldGroup)) return true;
        return cmdSender.hasPermission(commandBasePerm + ".group." + worldGroup.getName());
    }

    public boolean isGroupFlagValid() {
        if (groupFlag.isEmpty()) return true;
        return groupFlag.equalsIgnoreCase("-g") || groupFlag.equalsIgnoreCase("--group");
    }

    public boolean isGroupValid() {
        if (groupFlagArg.isEmpty()) return true;
        return worldGroup.getName().equalsIgnoreCase(groupFlagArg);
    }

    public String getHomeName() {
        return homeName;
    }

    public String getActionPlayerName() {
        return actionPlayer;
    }

    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    public WorldGroup getSendersCurrentWorldGroup() {
        return sendersCurrentWorldGroup;
    }

    public String getGroupFlagArg() {
        return groupFlagArg;
    }

    public UUID getActionPlayerUUID() {
        return actionPlayerUUID;
    }

    public String getGroupFlag() {
        return groupFlag;
    }

    public Player getCmdSender() {
        return cmdSender;
    }

    public static HomeAndDelHomeArguments parse(Player cmdSender, String[] args) {
        if (args.length == 0) // /delhome
            return new HomeAndDelHomeArguments(cmdSender, "", "", "", true);
        if (args.length == 1) // /delhome name
            return new HomeAndDelHomeArguments(cmdSender, cmdSender.getName(), args[0], "", false);
        if (args.length == 2) // /delhome (player) name
            return new HomeAndDelHomeArguments(cmdSender, args[0], args[1], "", false);
        if (args.length == 3) // /delhome name (-g group)
            return new HomeAndDelHomeArguments(cmdSender, cmdSender.getName(), args[0], args[1], args[2], false);
        if (args.length == 4)
            return new HomeAndDelHomeArguments(cmdSender, args[0], args[1], args[2], args[3], false);
        return new HomeAndDelHomeArguments(cmdSender, "", "", "", true);
    }


}
