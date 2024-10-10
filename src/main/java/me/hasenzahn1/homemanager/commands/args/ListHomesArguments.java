package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

// /listhomes (player) (-g group)
public class ListHomesArguments {

    private final String playerListHomesHome;

    private final Player cmdSender;
    private final UUID cmdSenderUUID;
    private final UUID playerListHomesUUID;

    private final String groupFlag;
    private final String groupFlagArg;
    private final WorldGroup worldGroup;
    private final WorldGroup sendersCurrentWorldGroup;
    private boolean toManyArguments;


    private ListHomesArguments(Player cmdSender, String playerListHomes, String groupFlag, String worldGroupName, boolean toManyArguments) {
        this.cmdSender = cmdSender;
        this.cmdSenderUUID = cmdSender.getUniqueId();

        this.playerListHomesHome = playerListHomes;
        if (!playerListHomes.isEmpty() && Bukkit.getOfflinePlayerIfCached(playerListHomes) != null)
            playerListHomesUUID = Bukkit.getOfflinePlayerIfCached(playerListHomes).getUniqueId();
        else playerListHomesUUID = null;

        this.groupFlagArg = worldGroupName;
        this.groupFlag = groupFlag;

        this.sendersCurrentWorldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(cmdSender.getWorld());
        this.worldGroup = HomeManager.getInstance().getWorldGroupManager().getOrDefault(worldGroupName, sendersCurrentWorldGroup);

        this.toManyArguments = toManyArguments;
    }

    public boolean isValidArguments() {
        return !toManyArguments;
    }

    public boolean argPlayerValid() {
        return playerListHomesUUID != null;
    }

    public boolean isSelf() {
        return cmdSenderUUID.equals(playerListHomesUUID);
    }

    public boolean senderHasValidOtherPermission() {
        if (isSelf()) return true;
        return cmdSender.hasPermission("homemanager.commands.listhomes.other." + worldGroup.getName());
    }

    public boolean isGroupFlagValid() {
        if (groupFlag.isEmpty()) return true;
        return groupFlag.equalsIgnoreCase("-g") || groupFlag.equalsIgnoreCase("-group");
    }

    public boolean isGroupValid() {
        if (groupFlagArg.isEmpty()) return true;
        return worldGroup.getName().equalsIgnoreCase(groupFlagArg);
    }

    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    public String getPlayerArgumentName() {
        return playerListHomesHome;
    }

    public UUID getPlayerListHomesUUID() {
        return playerListHomesUUID;
    }

    public Player getCmdSender() {
        return cmdSender;
    }

    public String getGroupFlagArg() {
        return groupFlagArg;
    }

    public WorldGroup getSendersCurrentWorldGroup() {
        return sendersCurrentWorldGroup;
    }

    public static ListHomesArguments parse(Player cmdSender, String[] args) {
        if (args.length == 0) return new ListHomesArguments(cmdSender, cmdSender.getName(), "", "", false);
        if (args.length == 1) return new ListHomesArguments(cmdSender, args[0], "", "", false);
        if (args.length == 2) return new ListHomesArguments(cmdSender, cmdSender.getName(), args[0], args[1], false);
        if (args.length == 3) return new ListHomesArguments(cmdSender, args[0], args[1], args[2], false);
        return new ListHomesArguments(cmdSender, cmdSender.getName(), "", "", true);
    }
}
