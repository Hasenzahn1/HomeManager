package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.util.PlayerNameUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents the arguments related to a player in a command context.
 * This class encapsulates the sender's information, such as their UUID, world group,
 * and an optional player argument (if provided in the command).
 * It also provides methods to check the validity of the arguments and perform related operations.
 */
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

    /**
     * Returns the name of the optional player argument.
     * <p>
     * This method retrieves the player name associated with the optional player UUID.
     *
     * @return the name of the optional player
     */
    public String getOptionalPlayerName() {
        return PlayerNameUtils.getPlayerNameFromUUID(optionalPlayerUUID);
    }

    /**
     * Checks if the number of arguments is invalid.
     * <p>
     * This method checks whether the command arguments are incorrect, usually due to missing or extra arguments.
     *
     * @return true if the number of arguments is invalid, false otherwise
     */
    public boolean invalidArguments() {
        return incorrectNumberOfArguments;
    }

    /**
     * Checks if the optional player argument is invalid.
     * <p>
     * This method checks whether the optional player argument is null or invalid.
     *
     * @return true if the optional player argument is invalid, false otherwise
     */
    public boolean playerArgInvalid() {
        return optionalPlayerUUID == null;
    }

    /**
     * Checks if the sender is the same as the optional player argument.
     * <p>
     * This method checks if the sender's UUID matches the optional player UUID, indicating that the sender is interacting with themselves.
     *
     * @return true if the sender is interacting with themselves, false otherwise
     */
    public boolean isSelf() {
        return cmdSenderUUID.equals(optionalPlayerUUID);
    }

    /**
     * Checks if the sender has the specified base permission for the world group.
     * <p>
     * This method checks if the sender has the permission for the command, based on their world group.
     *
     * @param commandBasePerm the base permission to check
     * @return true if the sender has the permission, false otherwise
     */
    public boolean senderHasBasePermission(String commandBasePerm) {
        return cmdSender.hasPermission(commandBasePerm + "." + cmdSenderWorldGroup.getName());
    }

    /**
     * Checks if the sender has the "other" permission for the specified world group.
     * <p>
     * This method checks if the sender has the permission to interact with other players' homes in the specified world group.
     *
     * @param commandBasePerm the base permission to check
     * @return true if the sender has the "other" permission, false otherwise
     */
    public boolean senderHasOtherPermission(String commandBasePerm) {
        if (isSelf()) return true;
        return cmdSender.hasPermission(commandBasePerm + ".other." + cmdSenderWorldGroup.getName());
    }

    /**
     * Returns the optional player argument passed in the command.
     * <p>
     * This method retrieves the string value of the optional player argument.
     *
     * @return the optional player argument
     */
    public String getOptionalPlayerArg() {
        return optionalPlayerArg;
    }

    /**
     * Returns the world group of the command sender.
     * <p>
     * This method retrieves the world group associated with the sender's current world.
     *
     * @return the world group of the sender
     */
    public WorldGroup getWorldGroup() {
        return cmdSenderWorldGroup;
    }

    /**
     * Returns the command sender player.
     * <p>
     * This method retrieves the player object representing the sender.
     *
     * @return the player who issued the command
     */
    public Player getCmdSender() {
        return cmdSender;
    }

    /**
     * Returns the UUID of the player associated with the optional player argument.
     * <p>
     * This method retrieves the UUID of the player associated with the optional player argument, or null if invalid.
     *
     * @return the UUID of the optional player, or null if invalid
     */
    public UUID getActionPlayerUUID() {
        return optionalPlayerUUID;
    }

    /**
     * Parses the arguments for a player command.
     * <p>
     * This method parses the arguments passed to the command and returns a PlayerArguments object
     * based on the number of arguments provided.
     *
     * @param cmdSender the player who issued the command
     * @param args      the arguments passed to the command
     * @return a PlayerArguments object containing the parsed arguments
     */
    public static PlayerArguments parseArguments(Player cmdSender, String[] args) {
        if (args.length == 0)
            return new PlayerArguments(cmdSender, cmdSender.getName(), false);
        if (args.length == 1)
            return new PlayerArguments(cmdSender, args[0], false);
        return new PlayerArguments(cmdSender, "", true);
    }

}
