package me.hasenzahn1.homemanager.commands.args;

import org.bukkit.entity.Player;

/**
 * Represents the arguments related to a player and group in a command context.
 * This class extends the {@link PlayerArguments} class and adds functionality
 * specific to commands that involve one name argument
 * <p>
 * This class is used for the /sethome command
 */
public class PlayerNameArguments extends PlayerArguments {

    private final String homeName;

    public PlayerNameArguments(Player cmdSender, String optionalPlayerArg, String homeName, boolean incorrectNumberOfArguments) {
        super(cmdSender, optionalPlayerArg, incorrectNumberOfArguments);
        this.homeName = homeName;
    }

    /**
     * Returns the home name provided in the arguments.
     *
     * @return the home name
     */
    public String getHomeName() {
        return homeName;
    }

    /**
     * Checks if the home name is valid (1–30 printable ASCII characters).
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidHomeName() {
        return homeName.matches("[!-~]{0,30}");
    }

    /**
     * Parses the arguments for a home-related command into a {@link PlayerNameArguments} object.
     * <p>
     * Supported cases:
     * <ul>
     *     <li><b>0 arguments:</b> Invalid — no homename provided (even for self).</li>
     *     <li><b>1 argument:</b> Treated as homename, applies to sender.</li>
     *     <li><b>2 arguments:</b> First is player name or UUID, second is homename.</li>
     *     <li><b>More than 2 arguments:</b> Invalid — too many inputs.</li>
     * </ul>
     *
     * @param cmdSender the player who issued the command
     * @param args      the command arguments
     * @return parsed {@link PlayerNameArguments} object
     */
    public static PlayerNameArguments parseArguments(Player cmdSender, String[] args) {
        if (args.length == 0)
            return new PlayerNameArguments(cmdSender, cmdSender.getName(), "", true); // invalid: no homename
        if (args.length == 1)
            return new PlayerNameArguments(cmdSender, cmdSender.getName(), args[0], false); // homename only
        if (args.length == 2)
            return new PlayerNameArguments(cmdSender, args[0], args[1], false); // player + homename
        return new PlayerNameArguments(cmdSender, "", "", true); // invalid: too many args
    }
}
