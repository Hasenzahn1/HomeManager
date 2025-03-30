package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
        optionalPlayerUUID = getOptionalPlayerUUIDFromString(optionalPlayerArg);

        cmdSenderWorldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(cmdSender.getWorld());
    }

    private UUID getOptionalPlayerUUIDFromString(String arg) {
        if (arg.isEmpty()) return null;

        UUID fromUUID = tryParseUUID(optionalPlayerArg);
        if (fromUUID != null) return fromUUID;

        OfflinePlayer fromName = Bukkit.getOfflinePlayerIfCached(optionalPlayerArg);
        if (fromName != null) return fromName.getUniqueId();

        return null;
    }

    public String getOptionalPlayerName() {
        OfflinePlayer player = Bukkit.getOfflinePlayer(optionalPlayerUUID);
        return player.getName() == null ? optionalPlayerUUID.toString() : player.getName();
    }

    private UUID tryParseUUID(String arg) {
        try {
            return UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
