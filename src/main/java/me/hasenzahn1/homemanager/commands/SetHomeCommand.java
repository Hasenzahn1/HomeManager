package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.homes.PlayerHome;
import me.hasenzahn1.homemanager.util.PermissionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

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
        PlayerNameArguments arguments = PlayerNameArguments.parseArguments(((Player) commandSender), args);

        //Check base sethome permission
        if (!arguments.senderHasValidCommandPermission("homemanager.commands.sethome")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check other Permission (no permission and players differ)
        if (!arguments.senderHasValidOtherPermission("homemanager.commands.sethome")) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Check Command Arg Range
        if (arguments.invalidArguments()) {
            Language.sendInvalidArgumentMessage(arguments.getCmdSender(), command, true, arguments.getWorldGroup());
            return true;
        }

        //No valid set player
        if (arguments.playerArgInvalid()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER, "name", arguments.getOptionalPlayerArg())));
            return true;
        }

        //Access database for homes
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        HashMap<String, PlayerHome> playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        PlayerHome requestedHome = playerHomes.get(arguments.getHomeName().toLowerCase());

        //Gather Max Homes from db
        int maxHomes = PermissionUtils.getMaxHomesFromPermission(commandSender, arguments.getWorldGroup().getName());

        //Check is player has reached his maxHome Limit
        if (arguments.isSelf() && playerHomes.size() >= maxHomes) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_MAX_HOMES, "amount", String.valueOf(maxHomes))));
            dbSession.destroy();
            return true;
        }

        //Check Duplicate Home Name
        if (requestedHome != null) {
            sendDuplicateHomesMessage(commandSender, arguments);
            dbSession.destroy();
            return true;
        }
        requestedHome = new PlayerHome(arguments.getHomeName(), arguments.getCmdSender().getLocation());

        //Player does not have to pay experience if he is not in survival, or he is setting a home for another player
        //TODO: Gamemode check to config
        boolean hasToPayExperience = arguments.isSelf() && !arguments.getCmdSender().getGameMode().isInvulnerable();

        //Get FreeHomes From db
        int freeHomes = dbSession.getFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());

        //No Experience Required
        if (!arguments.getWorldGroup().isSetHomeRequiresExperience() || !hasToPayExperience) {
            if (arguments.isSelf())
                dbSession.saveFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName(), Math.max(0, freeHomes - 1));
            saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getActionPlayerUUID(), requestedHome);
            return true;
        }

        //If the player has free homes use it.
        if (freeHomes > 0) {
            dbSession.saveFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName(), freeHomes - 1);
            saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getActionPlayerUUID(), requestedHome);
            return true;
        }

        //Calculate Experience
        int requiredLevels = arguments.getWorldGroup().getRequiredExperience(playerHomes.size());

        //You don't have enough experience, but you have to pay experience
        if (arguments.getCmdSender().getLevel() < requiredLevels) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_NO_EXP, "levels", String.valueOf((int) Math.ceil(requiredLevels)))));
            dbSession.destroy();
            return true;
        }

        //Reduce experience and save to db
        arguments.getCmdSender().setLevel(Math.max(0, arguments.getCmdSender().getLevel() - ((int) Math.ceil(requiredLevels))));
        saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getActionPlayerUUID(), requestedHome);
        return true;
    }

    private void saveHomeToDatabaseAndDestroy(DatabaseAccessor session, CommandSender cmdSender, UUID player, PlayerHome home) {
        sendSuccessMessage(((Player) cmdSender), player, home.getName());
        session.saveHomeToDatabase(player, home);
        session.destroy();
    }

    private void sendSuccessMessage(Player sender, UUID player, String homeName) {
        if (sender.getUniqueId().equals(player)) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_SUCCESS, "name", homeName)));
        } else {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_SUCCESS_OTHER, "name", homeName, "player", Bukkit.getOfflinePlayer(player).getName())));
        }
    }

    private void sendDuplicateHomesMessage(CommandSender sender, PlayerNameArguments arguments) {
        if (arguments.isSelf()) {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_DUPLICATE_HOME, "name", arguments.getHomeName())));
        } else {
            sender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_DUPLICATE_HOME_OTHER, "name", arguments.getHomeName(), "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName())));
        }
    }
}
