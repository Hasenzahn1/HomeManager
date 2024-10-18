package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SetHomeArguments {

    private final String playerReceiveHome;
    private final String homeName;

    private final Player cmdSender;
    private final UUID cmdSenderUUID;
    private final UUID playerReceiveHomeUUID;

    private final WorldGroup worldGroup;
    private boolean toManyArguments;


    private SetHomeArguments(Player cmdSender, String playerReceiveHome, String homeName, boolean toManyArguments) {
        this.cmdSender = cmdSender;
        this.cmdSenderUUID = cmdSender.getUniqueId();

        this.playerReceiveHome = playerReceiveHome;
        this.homeName = homeName;

        OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(playerReceiveHome);
        if (receiver == null) playerReceiveHomeUUID = null;
        else playerReceiveHomeUUID = receiver.getUniqueId();

        worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(cmdSender.getWorld());

        this.toManyArguments = toManyArguments;
    }

    public boolean isValidArguments() {
        if (homeName.isEmpty()) return false;
        if (toManyArguments) return false;
        return true;
    }

    public boolean argPlayerValid() {
        return playerReceiveHomeUUID != null;
    }

    public boolean isSelf() {
        return cmdSenderUUID.equals(playerReceiveHomeUUID);
    }

    public boolean senderHasValidOtherPermission() {
        if (isSelf()) return true;
        return cmdSender.hasPermission("homemanager.commands.sethome.other." + worldGroup.getName());
    }

    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    public String getPlayerArgumentName() {
        return playerReceiveHome;
    }

    public UUID getPlayerReceiveHomeUUID() {
        return playerReceiveHomeUUID;
    }

    public String getHomeName() {
        return homeName;
    }

    public static SetHomeArguments parse(Player cmdSender, String[] args) {
        if (args.length == 0) return new SetHomeArguments(cmdSender, cmdSender.getName(), "", false);
        if (args.length == 1) return new SetHomeArguments(cmdSender, cmdSender.getName(), args[0], false);
        if (args.length == 2) return new SetHomeArguments(cmdSender, args[0], args[1], false);
        return new SetHomeArguments(cmdSender, args[0], args[1], true);
    }
}
