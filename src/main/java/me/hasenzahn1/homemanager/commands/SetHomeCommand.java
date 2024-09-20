package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.util.ExpressionEvaluator;
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
 * Execution Steps:
 * 1) Check if commandSender is a player
 * 2) Get player, his location and the group he is currently in
 * 3) Check if the player has the `homemanager.command.sethome.<group>` permission
 * 4) Check for more than 2 args
 * 5) Gather args. If only one arg is set. This is the home name, so the receiver is the player sending and the homename is arg[0]
 * If there are two args the player is attempting to set a home for another player so arg[0] is the receiver and arg[1] is the homename
 * 6) Convert receiver and giver to uuid and check if they are the same
 * 7) If the receiving players uuid is null (player does not exist) an error is raised
 * 8) If you are not setting the home for yourself, but you don't have the permission `homemanager.command.sethome.other.<group>` an error is raised
 * 9) Get the Receiving players homes from the db, and his maxhomes from the permission
 * 10) If the receiver has reached his maxhomes an error is raised
 * 11) Check if the homename is valid for the db
 * 12) Check if the receiver already has a home with this name
 * 13) Compute if the sender has to pay experience. (He is in survival and executes the command for himself)
 * 14) If he does not have to pay experience, or the group has requiresExperience set to false, => save home to the database
 * 15) Get the Free homes from the database
 * 16) If the player has a free home use it and save the home
 * 17) Calculate the required Experience for this home purchase
 * 18) If the player does not have enough experience send an error
 * 19) Reduce the experience and Save the home
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

        //Get Sender's location data
        Player playerGiveHome = (Player) commandSender;
        Location location = playerGiveHome.getLocation();
        WorldGroup group = HomeManager.getInstance().getWorldGroupManager().groupsByWorld().get(location.getWorld());

        //Check base sethome permission
        if (!commandSender.hasPermission("homemanager.commands.sethome." + group.getName())) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check Command Arg Range
        if (args.length > 2) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/sethome (player) <name>")));
            return true;
        }

        //Gather arguments
        String playerReceiveHome = "";
        String homeName = "";
        if (args.length == 1) {
            playerReceiveHome = commandSender.getName();
            homeName = args[0];
        } else {
            playerReceiveHome = args[0];
            homeName = args[1];
        }

        UUID playerReceiveHomeUUID = Bukkit.getPlayerUniqueId(playerReceiveHome);
        UUID playerSetHomeUUID = ((Player) commandSender).getUniqueId();
        boolean isSelf = playerSetHomeUUID.equals(playerReceiveHomeUUID);

        //No valid set player
        if (playerReceiveHomeUUID == null) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER)));
            return true;
        }

        //Check other Permission (no permission and players differ)
        if (!isSelf && !commandSender.hasPermission("homemanager.commands.sethome.other." + group.getName())) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Access database for homes
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, Location> playerHomes = dbSession.getHomesFromPlayer(playerReceiveHomeUUID, group.getName());
        int maxHomes = PermissionUtils.getMaxHomesFromPermission(Bukkit.getPlayer(playerReceiveHomeUUID), group.getName());

        //Check is player has reached his maxHome Limit
        if (isSelf && playerHomes.size() >= maxHomes) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_MAX_HOMES)));
            dbSession.destroy();
            return true;
        }

        //Check is home name is valid for db
        if (!nameMatcher.matcher(homeName).matches()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_INVALID_HOME_NAME)));
            dbSession.destroy();
            return true;
        }

        //Check Duplicate Home Name
        if (playerHomes.containsKey(homeName)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_DUPLICATE_HOME)));
            dbSession.destroy();
            return true;
        }

        //Player does not have to pay experience if he is not in survival, or he is setting a home for another player
        //TODO: Gamemode check to config
        boolean hasToPayExperience = isSelf && !playerGiveHome.getGameMode().isInvulnerable();

        //No Experience Required
        if (!group.isSetHomeRequiresExperience() || !hasToPayExperience) {
            saveHomeToDatabaseAndDestroy(dbSession, playerReceiveHomeUUID, homeName, location);
            return true;
        }

        //Get FreeHomes From db
        int freeHomes = dbSession.getFreeHomes(playerReceiveHomeUUID, group.getName());

        //If the player has free homes use it.
        if (freeHomes > 0) {
            dbSession.saveFreeHomes(playerReceiveHomeUUID, group.getName(), freeHomes - 1);
            saveHomeToDatabaseAndDestroy(dbSession, playerReceiveHomeUUID, homeName, location);
            return true;
        }

        //Calculate Experience
        double requiredLevels = ExpressionEvaluator.eval(group.getSetHomeExperienceFormula().replace("amount", "" + playerHomes.size()));

        //You don't have enough experience, but you have to pay experience
        if (playerGiveHome.getLevel() < requiredLevels) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_NO_EXP, "levels", String.valueOf((int) Math.ceil(requiredLevels)))));
            dbSession.destroy();
            return true;
        }

        //Reduce experience and save to db
        playerGiveHome.setLevel(Math.max(0, playerGiveHome.getLevel() - ((int) Math.ceil(requiredLevels))));
        saveHomeToDatabaseAndDestroy(dbSession, playerReceiveHomeUUID, homeName, location);
        return true;
    }

    private void saveHomeToDatabaseAndDestroy(DatabaseAccessor session, UUID player, String homeName, Location location) {
        Bukkit.getPlayer(player).sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_SUCCESS)));
        session.saveHomeToDatabase(player, homeName, location);
        session.destroy();
    }
}
