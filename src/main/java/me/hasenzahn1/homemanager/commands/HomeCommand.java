package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.commands.checks.HomeExperienceCheck;
import me.hasenzahn1.homemanager.commands.checks.ObstructionCheck;
import me.hasenzahn1.homemanager.commands.checks.TimeoutCheck;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HomeCommand extends BaseHomeCommand {

    private final TimeoutCheck timeoutCheck;
    private final ObstructionCheck obstructionCheck;
    private final HomeExperienceCheck homeExperienceCheck;

    public HomeCommand(CompletionsHelper completionsHelper) {
        super(completionsHelper);

        timeoutCheck = new TimeoutCheck();
        obstructionCheck = new ObstructionCheck();
        homeExperienceCheck = new HomeExperienceCheck() {
            @Override
            public int getRequiredExperience(PlayerNameArguments arguments, int homes) {
                return arguments.getWorldGroup().getSettings().getHomeTeleportExperienceAmount();
            }
        };
    }

    // /home (player) homename (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Check player
        if (!(commandSender instanceof Player)) {
            MessageManager.sendMessage(commandSender, Language.NO_PLAYER);
            return true;
        }

        //Parse Arguments
        PlayerNameGroupArguments arguments = PlayerNameGroupArguments.parseArguments(((Player) commandSender), args);

        //Check Permissions
        if (PermissionValidator.checkInvalidPermissionsWithGroup(commandSender, arguments, "homemanager.commands.home"))
            return true;

        //Check Args
        if (ArgumentValidator.checkInvalidPlayerGroupArgs(commandSender, arguments, command))
            return true;

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        PlayerHomes playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup().getName());
        dbSession.destroy();

        //Check if home exists
        if (!playerHomes.homeExists(arguments.getHomeName())) {
            MessageManager.sendUnknownHomeMessage(arguments);
            return true;
        }

        //Get Home
        Home requestedHome = playerHomes.getHome(arguments.getHomeName());

        //Check if world exists
        if (!requestedHome.location().isWorldLoaded()) {
            Component baseText = Component.text(HomeManager.PREFIX + Language.getLang(Language.WARNING_WORLD_NOT_EXISTING_BASE_TEXT) + " ");
            Component deleteText = Component.text(Language.getLang(Language.WARNING_WORLD_NOT_EXISTING_DELETE_TEXT)).clickEvent(ClickEvent.runCommand("/delhome " + arguments.getCmdSender().getName() + " " + requestedHome.name() + " -g " + arguments.getWorldGroup().getName()));
            arguments.getCmdSender().sendMessage(baseText.append(deleteText));
            return true;
        }

        //Check if player is in Timeout
        boolean timeoutActive = arguments.getWorldGroup().getSettings().isTimeoutActive();
        if (timeoutActive && timeoutCheck.isInTimeout(arguments.getCmdSender(), arguments.getWorldGroup())) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_TIMEOUT, "seconds", String.valueOf(timeoutCheck.getRemainingSeconds(arguments)));
            return true;
        }

        //Check if home is obstructed but command not retried
        boolean homeObstructionCheck = arguments.getWorldGroup().getSettings().isHomeTeleportObstructedHomeCheck();
        if (homeObstructionCheck && obstructionCheck.checkForObstruction(arguments, requestedHome)) {
            Component component = Component.text(HomeManager.PREFIX + Language.getLang(Language.WARNING_HOME_OBSTRUCTED, "seconds", String.valueOf(arguments.getWorldGroup().getSettings().getHomeTeleportObstructedHomeRetryDuration()))).clickEvent(ClickEvent.runCommand("/home " + arguments.getCmdSender().getName() + " " + requestedHome.name() + " -g " + arguments.getWorldGroup().getName()));
            arguments.getCmdSender().sendMessage(component);
            return true;
        }

        //Check if experience has to be paid but player has not enough experience
        boolean homeTeleportExperienceActive = arguments.getWorldGroup().getSettings().isHomeTeleportExperienceActive();
        if (homeTeleportExperienceActive && homeExperienceCheck.checkForInvalidExperience(arguments, 0)) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_NO_EXP, "levels", String.valueOf(homeExperienceCheck.getRequiredExperience(arguments, 0)));
            return true;
        }

        //Check if player is on ground
        boolean playerHasToBeOnGround = arguments.getWorldGroup().getSettings().isHomeTeleportGroundCheck();
        boolean hasGroundBypass = arguments.isSelf() || arguments.getCmdSender().getGameMode().isInvulnerable();
        if (playerHasToBeOnGround && !arguments.getCmdSender().isOnGround() && !hasGroundBypass) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_NOT_ON_GROUND);
            return true;
        }

        //Pay Experience
        int experienceToBePaid = 0;
        if (homeTeleportExperienceActive && homeExperienceCheck.hasToPayExperience(arguments))
            experienceToBePaid = homeExperienceCheck.getRequiredExperience(arguments, 0);

        //Start Teleportation
        if (arguments.getWorldGroup().getSettings().isDelayActive() && arguments.isSelf()) {
            HomeManager.getInstance().createHomeTeleportation(arguments, requestedHome, arguments.getWorldGroup().getSettings().getDelayDurationInSeconds(), experienceToBePaid);
        } else {
            HomeManager.getInstance().createHomeTeleportation(arguments, requestedHome, 0, experienceToBePaid);
        }
        return true;
    }

    // /home (player) homename (--group groupname)
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        //No TabCompletions for non player command executors
        if (!(commandSender instanceof Player player)) return List.of();

        //No Completion if command is too long
        if (strings.length >= 5) return List.of();

        //Get World group
        WorldGroup worldGroup = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(player.getWorld());

        //Check permissions
        boolean hasOtherPermission = commandSender.hasPermission("homemanager.commands.home.other." + worldGroup.getName());
        boolean hasGroupPermission = commandSender.hasPermission("homemanager.commands.home.group." + worldGroup.getName());

        //Define Completions
        List<String> offlinePlayers = completionsHelper.matchAndSort(completionsHelper.getOfflinePlayers(), strings[0]);
        List<String> playersHomes = completionsHelper.getHomeSuggestions(player, player.getName());
        List<String> groupPrefix = List.of("-g", "-group");
        List<String> groups = completionsHelper.getWorldGroups(commandSender, "homemanager.commands.home.groups");
        boolean otherPlayerArgMightBeSet = !offlinePlayers.isEmpty();

        //Define completions for first arg
        if (strings.length == 1) {
            // Check Homes Arg: /home <home>
            List<String> matchedHomeNames = completionsHelper.matchAndSort(playersHomes, strings[0]);
            if (!matchedHomeNames.isEmpty()) return matchedHomeNames;

            //Check OfflinePlayer arg: /home (player)
            if (hasOtherPermission) return offlinePlayers;

            //No permission For other player and wrong homename
            return List.of();
        }

        //Define completions for second arg
        if (strings.length == 2) {
            //Check Home Arg: /home <player> <home>
            if (hasOtherPermission && otherPlayerArgMightBeSet) {
                List<String> otherPlayersHomes = completionsHelper.getHomeSuggestions(player, strings[0]);
                return completionsHelper.matchAndSort(otherPlayersHomes, strings[1]);
            }

            // Complete for group Flag: /home <home> -g
            if (hasGroupPermission) {
                return completionsHelper.matchAndSort(groupPrefix, strings[1]);
            }
            return List.of();
        }

        //Define completions for third arg
        if (strings.length == 3) {
            //Check for /home <player> <home> -g
            if (hasOtherPermission && otherPlayerArgMightBeSet) {
                return completionsHelper.matchAndSort(groupPrefix, strings[2]);
            }

            //Check for /home <home> -g <group>
            List<String> groupPrefixMatches = completionsHelper.matchAndSort(groupPrefix, strings[1]);
            if (hasGroupPermission && !groupPrefixMatches.isEmpty()) {
                return completionsHelper.matchAndSort(groups, strings[2]);
            }

            //All other cases
            return List.of();
        }

        //Define completions for fourth arg
        if (strings.length == 4) {
            if (!hasOtherPermission || !hasGroupPermission) return List.of();
            if (!otherPlayerArgMightBeSet) return List.of();

            //Check for /home <player> <home> -g <group>
            List<String> groupPrefixMatches = completionsHelper.matchAndSort(groupPrefix, strings[2]);
            if (!groupPrefixMatches.isEmpty()) return completionsHelper.matchAndSort(groups, strings[3]);

            return List.of();
        }

        return List.of();
    }
}
