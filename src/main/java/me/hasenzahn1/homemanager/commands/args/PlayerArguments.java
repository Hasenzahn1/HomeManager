package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.util.PlayerNameUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerArguments {

    protected final Player cmdSender;

    private final String optionalPlayerArg;

    private final UUID cmdSenderUUID;
    private final UUID optionalPlayerUUID;

    protected final WorldGroup cmdSenderWorldGroup;

    private final boolean incorrectNumberOfArguments;

    public PlayerArguments(Player cmdSender, String optionalPlayerArg, boolean incorrectNumberOfArguments) {
        this.cmdSender = cmdSender;
        this.optionalPlayerArg = optionalPlayerArg;
        this.incorrectNumberOfArguments = incorrectNumberOfArguments;

        cmdSenderUUID = cmdSender.getUniqueId();
        optionalPlayerUUID = PlayerNameUtils.getUUIDFromString(optionalPlayerArg);

        cmdSenderWorldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(cmdSender.getWorld());
    }

    public String getOptionalPlayerName() {
        return PlayerNameUtils.getPlayerNameFromUUID(optionalPlayerUUID);
    }

    public boolean invalidArguments() {
        return incorrectNumberOfArguments;
    }

    public boolean playerArgInvalid() {
        return optionalPlayerUUID == null;
    }

    public boolean isSelf() {
        return cmdSenderUUID.equals(optionalPlayerUUID);
    }

    public boolean senderHasBasePermission(String commandBasePerm) {
        return cmdSender.hasPermission(commandBasePerm + "." + cmdSenderWorldGroup.getName());
    }

    public boolean senderHasOtherPermission(String commandBasePerm) {
        if (isSelf()) return true;
        return cmdSender.hasPermission(commandBasePerm + ".other." + cmdSenderWorldGroup.getName());
    }

    public String getOptionalPlayerArg() {
        return optionalPlayerArg;
    }

    public WorldGroup getWorldGroup() {
        return cmdSenderWorldGroup;
    }

    public Player getCmdSender() {
        return cmdSender;
    }

    public UUID getActionPlayerUUID() {
        return optionalPlayerUUID;
    }

    public static PlayerArguments parseArguments(Player cmdSender, String[] args) {
        if (args.length == 0)
            return new PlayerArguments(cmdSender, cmdSender.getName(), false);
        if (args.length == 1)
            return new PlayerArguments(cmdSender, args[0], false);
        return new PlayerArguments(cmdSender, "", true);
    }

}
