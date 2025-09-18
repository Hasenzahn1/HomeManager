package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.commands.checks.*;
import me.hasenzahn1.homemanager.commands.system.BaseHomeCommand;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.group.WorldGroupSettings;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.homes.teleportation.TeleportationManager;
import me.hasenzahn1.homemanager.integration.PlotsquaredIntegration;
import me.hasenzahn1.homemanager.integration.WorldGuardIntegration;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
    private WorldGuardRegionCheck worldGuardRegionCheck;
    private PlotsquaredRegionCheck plotsquaredRegionCheck;

    public HomeCommand(CompletionsHelper completionsHelper) {
        super(completionsHelper);

        timeoutCheck = new TimeoutCheck();
        obstructionCheck = new ObstructionCheck();
        homeExperienceCheck = new HomeExperienceCheck() {
            @Override
            public int getRequiredExperience(PlayerNameArguments arguments, int homes, Home requestedHome) {
                return arguments.getWorldGroup().getSettings().getHomeTeleportExperience(arguments.getCmdSender().getLocation(), requestedHome.location());
            }
        };

        if (HomeManager.WORLD_GUARD_API_EXISTS) {
            worldGuardRegionCheck = new WorldGuardRegionCheck(WorldGuardIntegration.homeTeleportFlag);
        }

        if (HomeManager.PLOTSQUARED_API_EXISTS) {
            plotsquaredRegionCheck = new PlotsquaredRegionCheck(PlotsquaredIntegration.TELEPORT_HOMES);
        }
    }

    // /home (player) homename (--group groupname)
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Logger.DEBUG.log(commandSender.getName() + " executed /" + command.getName() + " " + String.join(" ", args));
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
        if (ArgumentValidator.checkInvalidPlayerGroupArgs(commandSender, arguments, command)) return true;

        //Check Worldguard Region
        if (!canUseHomesInWorldGuard(arguments)) {
            MessageManager.sendMessage(commandSender, Language.REGIONS_HOME_TELEPORTATION_DISABLED);
            return true;
        }

        //Validate Plotsquared Plot
        if (!canUseHomesInPlotSquared(arguments)) {
            MessageManager.sendMessage(commandSender, Language.PLOTSQUARED_TELEPORT_HOMES_DISABLED);
            return true;
        }

        //Get Homes from db
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        PlayerHomes playerHomes = dbSession.getHomesFromPlayer(arguments.getActionPlayerUUID(), arguments.getWorldGroup());
        dbSession.destroy();

        //Check if home exists
        if (!playerHomes.homeExists(arguments.getHomeName())) {
            MessageManager.sendUnknownHomeMessage(arguments);
            return true;
        }

        //Get Home
        Home requestedHome = playerHomes.getHome(arguments.getHomeName());
        WorldGroupSettings settings = arguments.getWorldGroup().getSettings();

        //Check if world exists
        if (!requestedHome.location().isWorldLoaded()) {
            sendWorldNotExistsMessage(arguments, requestedHome);
            return true;
        }

        //Check if player is in Timeout
        if (!hasBypass(arguments, PermissionValidator.HOME_TIMEOUT_BYPASS) && isInHomeTimeout(settings, arguments)) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_TIMEOUT, "seconds", String.valueOf(timeoutCheck.getRemainingSeconds(arguments)));
            return true;
        }

        //Check if home is obstructed but command not retried
        if (!hasBypass(arguments, PermissionValidator.HOME_OBSTRUCTED_BYPASS) && shouldCheckObstruction(settings, arguments) && obstructionCheck.checkForObstruction(arguments, requestedHome)) {
            sendHomeObstructionMessage(arguments, settings, requestedHome);
            return true;
        }

        //Check if experience has to be paid but player has not enough experience
        boolean homeTeleportExperienceActive = settings.isHomeTeleportExperienceActive();
        if (!hasBypass(arguments, PermissionValidator.HOME_EXPERIENCE_BYPASS) && homeTeleportExperienceActive && homeExperienceCheck.checkForInvalidExperience(arguments, 0, requestedHome)) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_NO_EXP, "levels", String.valueOf(homeExperienceCheck.getRequiredExperience(arguments, 0, requestedHome)));
            return true;
        }

        final int expToPay = (homeTeleportExperienceActive && homeExperienceCheck.hasToPayExperience(arguments) && !hasBypass(arguments, PermissionValidator.HOME_EXPERIENCE_BYPASS))
                ? homeExperienceCheck.getRequiredExperience(arguments, 0, requestedHome)
                : 0;

        //Check if player is on ground
        if (requiresOnGround(settings, arguments) && !arguments.getCmdSender().isOnGround() && !hasBypass(arguments, PermissionValidator.HOME_GROUND_CHECK_BYPASS)) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_NOT_ON_GROUND);
            return true;
        }

        //Teleportation delay
        int delaySeconds = settings.isDelayActive() && !delayBypassed(settings, arguments) ? settings.getDelayDurationInSeconds() : 0;

        //Teleport without delay
        TeleportationManager.getInstance().createHomeTeleportation(arguments, requestedHome, delaySeconds, expToPay);
        return true;
    }

    private void sendHomeObstructionMessage(PlayerNameGroupArguments arguments, WorldGroupSettings settings, Home home) {
        Component component = Component.text(HomeManager.PREFIX + MessageManager.getPAPILang(arguments.getCmdSender(), Language.WARNING_HOME_OBSTRUCTED,
                        "seconds", String.valueOf(settings.getObstructedHomeCheckRetryDurationInSeconds())
                )).clickEvent(ClickEvent.runCommand(getCommandFromHome(home)))
                .hoverEvent(HoverEvent.showText(Component.text(getCommandFromHome(home))));
        arguments.getCmdSender().sendMessage(component);
    }

    private void sendWorldNotExistsMessage(PlayerNameGroupArguments arguments, Home requestedHome) {
        Component baseText = Component.text(HomeManager.PREFIX + MessageManager.getPAPILang(arguments.getCmdSender(), Language.WARNING_WORLD_NOT_EXISTING_BASE_TEXT) + " ");
        Component deleteText = Component.text(MessageManager.getPAPILang(arguments.getCmdSender(), Language.WARNING_WORLD_NOT_EXISTING_DELETE_TEXT)).clickEvent(ClickEvent.runCommand("/delhome " + arguments.getCmdSender().getName() + " " + requestedHome.name() + " -g " + arguments.getWorldGroup().getName()));
        arguments.getCmdSender().sendMessage(baseText.append(deleteText));
    }

    private boolean canUseHomesInWorldGuard(PlayerNameGroupArguments a) {
        return hasBypass(a, PermissionValidator.HOME_WORLDGUARD_BYPASS) || worldGuardRegionCheck == null || worldGuardRegionCheck.canUseHomes(a.getCmdSender());
    }

    private boolean canUseHomesInPlotSquared(PlayerNameGroupArguments a) {
        return hasBypass(a, PermissionValidator.HOME_PLOTSQUARED_BYPASS) || plotsquaredRegionCheck == null || plotsquaredRegionCheck.canUseHomes(a.getCmdSender());
    }

    private boolean isInHomeTimeout(WorldGroupSettings settings, PlayerNameGroupArguments a) {
        return settings.isTimeoutActive() && timeoutCheck.isInTimeout(a.getCmdSender(), a.getWorldGroup());
    }

    private boolean shouldCheckObstruction(WorldGroupSettings settings, PlayerNameGroupArguments a) {
        if (!settings.isObstructedHomeCheckActive()) return false;
        final boolean creativeBypass = settings.isObstructedHomeCheckDisableInCreative() && a.getCmdSender().getGameMode().isInvulnerable();
        return !creativeBypass;
    }

    private boolean requiresOnGround(WorldGroupSettings settings, PlayerNameGroupArguments a) {
        if (!settings.isHomeTeleportOnGroundCheckActive()) return false;
        if (!a.isSelf()) return false;
        final boolean creativeBypass = settings.isHomeTeleportOnGroundCheckDisableInCreative() && a.getCmdSender().getGameMode().isInvulnerable();
        return !creativeBypass;
    }

    private boolean delayBypassed(WorldGroupSettings settings, PlayerNameGroupArguments a) {
        if (!a.isSelf()) return true;
        if (hasBypass(a, PermissionValidator.HOME_TELEPORTATION_DELAY_BYPASS)) return true;
        if (!settings.isDelayDisableInCreative()) return false;
        return a.getCmdSender().getGameMode().isInvulnerable();
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

    public static String getCommandFromHome(Home home) {
        WorldGroup group = HomeManager.getInstance().getWorldGroupManager().getWorldGroup(home.location().getWorld());
        return "/home " + home.getOwnersName() + " " + home.name() + " -g " + group.getName();
    }
}
