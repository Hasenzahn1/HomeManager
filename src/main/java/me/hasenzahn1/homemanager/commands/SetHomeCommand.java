package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.checks.HomeExperienceCheck;
import me.hasenzahn1.homemanager.commands.system.BaseHomeCommand;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import me.hasenzahn1.homemanager.util.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SetHomeCommand extends BaseHomeCommand {

    private final HomeExperienceCheck homeExperienceCheck;

    public SetHomeCommand(CompletionsHelper completionsHelper) {
        super(completionsHelper);

        homeExperienceCheck = new HomeExperienceCheck() {
            @Override
            public int getRequiredExperience(PlayerNameArguments arguments, int currentHomes, Home requestedHome) {
                return arguments.getWorldGroup().getSettings().getRequiredExperience(currentHomes);
            }
        };
    }

    // /sethome (player) \<name>
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Logger.DEBUG.log(commandSender.getName() + " executed /" + command.getName() + " " + String.join(" ", args));
        //Check player
        if (!(commandSender instanceof Player)) {
            MessageManager.sendMessage(commandSender, Language.NO_PLAYER);
            return true;
        }

        //Parse Arguments
        PlayerNameArguments arguments = PlayerNameArguments.parseArguments(((Player) commandSender), args);

        //Check Permissions
        if (PermissionValidator.checkInvalidPermissions(commandSender, arguments, "homemanager.commands.sethome"))
            return true;

        //Validate Arguments
        if (ArgumentValidator.checkInvalidPlayerArgs(commandSender, arguments, command))
            return true;

        //Access database for homes
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        PlayerHomes playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup());

        //Check Duplicate Home Name
        if (playerHomes.homeExists(arguments.getHomeName())) {
            MessageManager.sendDuplicateHomesMessage(commandSender, arguments);
            dbSession.destroy();
            return true;
        }

        //Check for Invalid Characters in homeName
        if (!arguments.isValidHomeName()) {
            MessageManager.sendMessage(commandSender, Language.SET_HOME_INVALID_NAME, "homename", arguments.getHomeName());
            dbSession.destroy();
            return true;
        }

        //Gather Max Homes from db
        int maxHomes = PermissionUtils.getMaxHomesFromPermission(commandSender, arguments.getWorldGroup().getName());

        //Check if the player has reached his maxHome Limit
        if (arguments.isSelf() && playerHomes.getHomeAmount() >= maxHomes) {
            MessageManager.sendMessage(commandSender, Language.SET_HOME_MAX_HOMES, "amount", String.valueOf(maxHomes));
            dbSession.destroy();
            return true;
        }

        //Create the home
        Home requestedHome = new Home(arguments.getActionPlayerUUID(), arguments.getHomeName(), arguments.getCmdSender().getLocation());

        //Player does not have to pay experience if he is not in survival, or he is setting a home for another player
        //TODO: Gamemode check to config
        boolean hasToPayExperience = homeExperienceCheck.hasToPayExperience(arguments);

        //No Experience Required
        if (!hasToPayExperience || !arguments.getWorldGroup().getSettings().isSetHomeExperienceActive()) {
            if (arguments.isSelf())
                dbSession.decrementFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
            saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getActionPlayerUUID(), requestedHome);
            return true;
        }

        //Get FreeHomes From db
        int freeHomes = dbSession.getFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());

        //If the player has free homes use it.
        if (freeHomes > 0) {
            dbSession.decrementFreeHomes(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
            saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getActionPlayerUUID(), requestedHome);
            return true;
        }

        //You don't have enough experience, but you have to pay experience
        int requiredLevels = homeExperienceCheck.getRequiredExperience(arguments, playerHomes.getHomeAmount(), requestedHome);
        System.out.println(requiredLevels);
        if (homeExperienceCheck.checkForInvalidExperience(arguments, playerHomes.getHomeAmount(), requestedHome)) {
            MessageManager.sendMessage(commandSender, Language.SET_HOME_NO_EXP, "levels", String.valueOf(requiredLevels));
            dbSession.destroy();
            return true;
        }

        //Reduce experience and save to db
        arguments.getCmdSender().setLevel(Math.max(0, arguments.getCmdSender().getLevel() - requiredLevels));
        saveHomeToDatabaseAndDestroy(dbSession, commandSender, arguments.getActionPlayerUUID(), requestedHome);
        return true;
    }

    private void saveHomeToDatabaseAndDestroy(DatabaseAccessor session, CommandSender cmdSender, UUID player, Home home) {
        sendSuccessMessage(((Player) cmdSender), player, home);
        session.saveHomeToDatabase(player, home);
        HomeManager.getInstance().getHomesCache().invalidateCache(player);
        session.destroy();

        Logger.DEBUG.log(cmdSender.getName() + " created home " + home.name() + " at location (" + home.location().getBlockX() + ", " + home.location().getBlockY() + ", " + home.location().getBlockZ() + ")" + " for player " + player);
    }

    private void sendSuccessMessage(Player sender, UUID player, Home home) {
        if (sender.getUniqueId().equals(player)) {
            MessageManager.sendMessage(sender, Language.SET_HOME_SUCCESS, "homename", home.name());
        } else {
            MessageManager.sendMessage(sender, Language.SET_HOME_SUCCESS_OTHER, "homename", home.name(), "player", home.getOwnersName());
        }
    }


    // /sethome (player) <name>
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // No TabCompletions for non player command executors
        if (!(commandSender instanceof Player player)) return List.of();

        //No Completion if command is too long
        if (strings.length >= 3) return List.of();

        // Check Permissions
        boolean playerHasOtherPermission = player.hasPermission("homemanager.commands.sethome.other." + HomeManager.getInstance().getWorldGroupManager().getWorldGroup(player.getWorld()).getName());

        // Define Completions
        List<String> nameArgCompletions = List.of("<homename>");
        List<String> offlinePlayers = completionsHelper.matchAndSort(completionsHelper.getOfflinePlayers(), strings[0]);
        boolean otherPlayerArgMightBeSet = !offlinePlayers.isEmpty();

        //Define Completion Cases
        if (strings[0].isEmpty()) return nameArgCompletions;

        //Define Completions for first argument
        if (strings.length == 1) {
            //Check for other permission: /sethome (player)
            if (playerHasOtherPermission && otherPlayerArgMightBeSet) return offlinePlayers;

            //Display name args
            return nameArgCompletions;
        }

        if (strings.length == 2) {
            //Check for name arg: /sethome (player) name
            if (playerHasOtherPermission && otherPlayerArgMightBeSet) return nameArgCompletions;

            //No other arg needed
            return List.of();
        }
        return List.of();
    }
}
