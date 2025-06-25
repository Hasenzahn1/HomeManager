package me.hasenzahn1.homemanager.commands.args;

import org.bukkit.entity.Player;

/**
 * Represents the arguments related to a player and group in a command context.
 * This class extends the {@link PlayerNameGroupArguments} class and adds functionality
 * specific to commands that involve groups, including handling optional player arguments
 * and group flags.
 * <p>
 * This class is used for the /homes command
 */
public class PlayerGroupArguments extends PlayerNameGroupArguments {

    public PlayerGroupArguments(Player cmdSender, String optionalPlayerArg, String groupFlagArg, String groupName, boolean incorrectNumberOfArguments) {
        super(cmdSender, optionalPlayerArg, "", groupFlagArg, groupName, incorrectNumberOfArguments);
    }

    /**
     * Parses command arguments into a {@link PlayerGroupArguments} object.
     * <p>
     * Supported argument combinations:
     * <ul>
     *     <li><b>0 arguments:</b> The sender is used as the player; no group specified.</li>
     *     <li><b>1 argument:</b> Treated as a player name; no group specified.</li>
     *     <li><b>2 arguments:</b> Treated as a group specification for the sender (e.g., {@code -g survival}).</li>
     *     <li><b>3 arguments:</b> First is player name, second is group flag (e.g., {@code -g}), third is group name.</li>
     *     <li><b>More than 3 arguments:</b> Treated as invalid input.</li>
     * </ul>
     *
     * @param cmdSender the player who issued the command
     * @param args      the string arguments passed with the command
     * @return a new {@code PlayerGroupArguments} object representing the parsed input
     */
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
