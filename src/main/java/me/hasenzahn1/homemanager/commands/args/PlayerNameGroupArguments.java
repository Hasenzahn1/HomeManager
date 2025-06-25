package me.hasenzahn1.homemanager.commands.args;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.entity.Player;

/**
 * Represents the arguments related to a player and group in a command context.
 * This class extends the {@link PlayerNameArguments} class and adds functionality
 * specific to commands that involve a name a player or a group
 * <p>
 * This class is used for the /delhome oder /home command
 */
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

    /**
     * Checks whether the command sender has permission to act within the target group.
     * <p>
     * If the sender's current world group is the same as the target group, permission is granted automatically.
     * Otherwise, a specific permission node is checked, such as:
     * <pre>
     *     homemanager.commands.[base].group.[groupName]
     * </pre>
     *
     * @param commandBasePerm the base permission string (e.g., "homemanager.commands.sethome")
     * @return {@code true} if permission is granted, {@code false} otherwise
     */
    public boolean senderHasGroupPermission(String commandBasePerm) {
        if (cmdSenderWorldGroup.equals(worldGroup)) return true;
        return cmdSender.hasPermission(commandBasePerm + ".group." + worldGroup.getName());
    }

    /**
     * Checks whether the provided group flag is invalid.
     * <p>
     * Valid flags are {@code -g} or {@code -group}, case-insensitive.
     * If no flag is provided (empty), this is considered valid.
     *
     * @return {@code true} if the flag is invalid, {@code false} otherwise
     */
    public boolean groupFlagInvalid() {
        if (groupFlagArg.isEmpty()) return false;
        return !groupFlagArg.equalsIgnoreCase("-g") && !groupFlagArg.equalsIgnoreCase("-group");
    }

    /**
     * Checks whether the specified group name is invalid.
     * <p>
     * A group name is considered invalid if it's not empty and doesn't match the resolved {@code worldGroup}'s name.
     *
     * @return {@code true} if the group name is invalid, {@code false} otherwise
     */
    public boolean groupInvalid() {
        if (groupName.isEmpty()) return false;
        return !worldGroup.getName().equalsIgnoreCase(groupName);
    }

    /**
     * Returns the resolved world group used for this argument context.
     *
     * @return the {@link WorldGroup} object used for this command context
     */
    @Override
    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    /**
     * Gets the raw group name string provided in the arguments.
     *
     * @return the specified group name, or empty if none was provided
     */
    public String getGroupName() {
        return groupName;
    }


    /**
     * Parses command arguments into a {@link PlayerNameGroupArguments} object.
     * <p>
     * Supported argument combinations:
     * <ul>
     *     <li><b>0 arguments:</b> Invalid – no home name provided.</li>
     *     <li><b>1 argument:</b> Treated as home name; player is the sender.</li>
     *     <li><b>2 arguments:</b> First is player name, second is home name.</li>
     *     <li><b>3 arguments:</b> First is home name, second is group flag (e.g., {@code -g}), third is group name; player is the sender.</li>
     *     <li><b>4 arguments:</b> First is player name, second is home name, third is group flag, fourth is group name.</li>
     *     <li><b>More than 4 arguments:</b> Treated as invalid input.</li>
     * </ul>
     *
     * @param cmdSender the player who issued the command
     * @param args      the string arguments passed with the command
     * @return a new {@code PlayerNameGroupArguments} object representing the parsed input
     */
    public static PlayerNameGroupArguments parseArguments(Player cmdSender, String[] args) {
        if (args.length == 0)
            return new PlayerNameGroupArguments(cmdSender, cmdSender.getName(), "", "", "", true); // invalid
        if (args.length == 1)
            return new PlayerNameGroupArguments(cmdSender, cmdSender.getName(), args[0], "", "", false); // homename only
        if (args.length == 2)
            return new PlayerNameGroupArguments(cmdSender, args[0], args[1], "", "", false); // player + homename
        if (args.length == 3)
            return new PlayerNameGroupArguments(cmdSender, cmdSender.getName(), args[0], args[1], args[2], false); // homename + -g + group
        if (args.length == 4)
            return new PlayerNameGroupArguments(cmdSender, args[0], args[1], args[2], args[3], false); // player + homename + -g + group
        return new PlayerNameGroupArguments(cmdSender, "", "", "", "", true); // too many args
    }
}

