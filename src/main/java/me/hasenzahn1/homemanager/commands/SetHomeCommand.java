package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.SetHomeArguments;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.util.PermissionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * Execution Steps:<br>
 * 1) Check if commandSender is a player<br>
 * 2) Get player, his location and the group he is currently in<br>
 * 3) Check if the player has the `homemanager.command.sethome.<group>` permission<br>
 * 4) Check for more than 2 args<br>
 * 5) Gather args. If only one arg is set. This is the home name, so the receiver is the player sending and the homename is arg[0]
 * If there are two args the player is attempting to set a home for another player so arg[0] is the receiver and arg[1] is the homename<br>
 * 6) Convert receiver and giver to uuid and check if they are the same<br>
 * 7) If the receiving players uuid is null (player does not exist) an error is raised<br>
 * 8) If you are not setting the home for yourself, but you don't have the permission `homemanager.command.sethome.other.<group>` an error is raised<br>
 * 9) Get the Receiving players homes from the db, and his maxhomes from the permission<br>
 * 10) If the receiver has reached his maxhomes an error is raised<br>
 * 11) Check if the homename is valid for the db<br>
 * 12) Check if the receiver already has a home with this name<br>
 * 13) Compute if the sender has to pay experience. (He is in survival and executes the command for himself)<br>
 * 14) If he does not have to pay experience, or the group has requiresExperience set to false, => save home to the database<br>
 * 15) Get the Free homes from the database<br>
 * 16) If the player has a free home use it and save the home<br>
 * 17) Calculate the required Experience for this home purchase<br>
 * 18) If the player does not have enough experience send an error<br>
 * 19) Reduce the experience and Save the home<br>
 */
public class SetHomeCommand implements CommandExecutor {

    private final Pattern nameMatcher = Pattern.compile("^[A-Za-z0-9ß#+_-]{1,16}$");

    // /sethome (player) \<name>
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        //Parse Arguments
        SetHomeArguments arguments = SetHomeArguments.parse(((Player) commandSender), args);

        //Get Sender's location data
        Player playerGiveHome = (Player) commandSender;
        Location location = playerGiveHome.getLocation();

        //Check base sethome permission
        if (!commandSender.hasPermission("homemanager.commands.sethome." + arguments.getWorldGroup().getName())) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check Command Arg Range
        if (!arguments.isValidArguments()) {
            Language.sendInvalidArgumentMessage(playerGiveHome, command, true, arguments.getWorldGroup());
            return true;
        }

        //No valid set player
        if (!arguments.argPlayerValid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", arguments.getPlayerArgumentName())));
            return true;
        }

        //Check other Permission (no permission and players differ)
        if (!arguments.senderHasValidOtherPermission()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Access database for homes
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, Location> playerHomes = dbSession.getHomesFromPlayer(arguments.getPlayerReceiveHomeUUID(), arguments.getWorldGroup().getName());
        int maxHomes = PermissionUtils.getMaxHomesFromPermission(commandSender, arguments.getWorldGroup().getName());

        //Check is player has reached his maxHome Limit
        if (arguments.isSelf() && playerHomes.size() >= maxHomes) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_MAX_HOMES, "amount", String.valueOf(maxHomes))));
            dbSession.destroy();
            return true;
        }

        //Check Duplicate Home Name
        if (playerHomes.containsKey(arguments.getHomeName())) {
            sendDuplicateHomesMessage(commandSender, arguments);
            dbSession.destroy();
            return true;
        }

        //Player does not have to pay experience if he is not in survival, or he is setting a home for another player
        //TODO: Gamemode check to config
        boolean hasToPayExperience = arguments.isSelf() && !playerGiveHome.getGameMode().isInvulnerable();

        //Get FreeHomes From db
        int freeHomes = dbSession.getFreeHomes(arguments.getPlayerReceiveHomeUUID(), arguments.getWorldGroup().getName());

        //No Experience Required
        if (!arguments.getWorldGroup().isSetHomeRequiresExperience() || !hasToPayExperience) {
            if (arguments.isSelf())
                dbSession.saveFreeHomes(arguments.getPlayerReceiveHomeUUID(), arguments.getWorldGroup().getName(), Math.max(0, freeHomes - 1));
            saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getPlayerReceiveHomeUUID(), arguments.getHomeName(), location);
            return true;
        }

        //If the player has free homes use it.
        if (freeHomes > 0) {
            dbSession.saveFreeHomes(arguments.getPlayerReceiveHomeUUID(), arguments.getWorldGroup().getName(), freeHomes - 1);
            saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getPlayerReceiveHomeUUID(), arguments.getHomeName(), location);
            return true;
        }

        //Calculate Experience
        int requiredLevels = arguments.getWorldGroup().getRequiredExperience(playerHomes.size());

        //You don't have enough experience, but you have to pay experience
        if (playerGiveHome.getLevel() < requiredLevels) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_NO_EXP, "levels", String.valueOf((int) Math.ceil(requiredLevels)))));
            dbSession.destroy();
            return true;
        }

        //Reduce experience and save to db
        playerGiveHome.setLevel(Math.max(0, playerGiveHome.getLevel() - ((int) Math.ceil(requiredLevels))));
        saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getPlayerReceiveHomeUUID(), arguments.getHomeName(), location);
        return true;
    }

    private void saveHomeToDatabaseAndDestroy(DatabaseAccessor session, CommandSender cmdSender, UUID player, String homeName, Location location) {
        sendSuccessMessage(((Player) cmdSender), player, homeName);
        session.saveHomeToDatabase(player, homeName, location);
        session.destroy();
    }

    private void sendSuccessMessage(Player sender, UUID player, String homeName) {
        if (sender.getUniqueId().equals(player)) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_SUCCESS, "name", homeName)));
        } else {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_SUCCESS_OTHER, "name", homeName, "player", Bukkit.getOfflinePlayer(player).getName())));
        }
    }

    private void sendDuplicateHomesMessage(CommandSender sender, SetHomeArguments arguments) {
        if (arguments.isSelf()) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_DUPLICATE_HOME, "name", arguments.getHomeName())));
        } else {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_DUPLICATE_HOME_OTHER, "name", arguments.getHomeName(), "player", Bukkit.getOfflinePlayer(arguments.getPlayerReceiveHomeUUID()).getName())));
        }
    }
}
