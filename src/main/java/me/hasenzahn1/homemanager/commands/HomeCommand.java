package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.ArgumentValidator;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.homes.PlayerHomes;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HomeCommand extends BaseHomeCommand {

    private static final int OBSTRUCTED_HOME_RETRY_DURATION = 5000;

    private final HashMap<UUID, String> lastHomes;
    private final HashMap<UUID, Long> obstructedTimestamps;


    public HomeCommand(CompletionsHelper completionsHelper) {
        super(completionsHelper);

        lastHomes = new HashMap<>();
        obstructedTimestamps = new HashMap<>();
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
        if (arguments.getWorldGroup().getSettings().isTimeoutActive() && System.currentTimeMillis() - HomeManager.getInstance().getTimeoutListener().getDamageTimestamps().getOrDefault(arguments.getCmdSender().getUniqueId(), 0L) < arguments.getWorldGroup().getSettings().getTimeoutDurationInSeconds() * 1000) {
            double durationInMillis = arguments.getWorldGroup().getSettings().getTimeoutDurationInSeconds() * 1000 - (System.currentTimeMillis() - HomeManager.getInstance().getTimeoutListener().getDamageTimestamps().get(arguments.getCmdSender().getUniqueId()));
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_TIMEOUT, "seconds", String.valueOf(Math.round(durationInMillis / 1000.0)));
            return true;
        }

        //Check if the command is retried
        boolean sameHome = lastHomes.getOrDefault(arguments.getCmdSender().getUniqueId(), "").equalsIgnoreCase(requestedHome.name());
        boolean within5Seconds = System.currentTimeMillis() - obstructedTimestamps.getOrDefault(arguments.getCmdSender().getUniqueId(), 0L) <= OBSTRUCTED_HOME_RETRY_DURATION;
        boolean retried = sameHome && within5Seconds;

        //Check for home obstruction
        boolean checkForObstructionActive = arguments.getWorldGroup().getSettings().isHomeTeleportObstructedHomeCheck();
        if (checkForObstructionActive && !retried && homeIsObstructed(requestedHome, arguments.getCmdSender().getEyeHeight())) {
            Component component = Component.text(HomeManager.PREFIX + Language.getLang(Language.WARNING_HOME_OBSTRUCTED)).clickEvent(ClickEvent.runCommand("/home " + arguments.getCmdSender().getName() + " " + requestedHome.name() + " -g " + arguments.getWorldGroup().getName()));
            arguments.getCmdSender().sendMessage(component);

            //Save Data for the retry command
            obstructedTimestamps.put(arguments.getCmdSender().getUniqueId(), System.currentTimeMillis());
            lastHomes.put(arguments.getCmdSender().getUniqueId(), requestedHome.name());
            return true;
        }

        //Check if experience has to be paid
        boolean homeTeleportExperienceActive = arguments.getWorldGroup().getSettings().isHomeTeleportExperienceActive();
        int requiredLevels = arguments.getWorldGroup().getSettings().getHomeTeleportExperienceAmount();
        boolean hasToPayExperience = arguments.isSelf() && !arguments.getCmdSender().getGameMode().isInvulnerable();

        //Check if experience has to be paid but player has not enough experience
        if (homeTeleportExperienceActive && hasToPayExperience && arguments.getCmdSender().getLevel() < requiredLevels) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_NO_EXP, "levels", String.valueOf(requiredLevels));
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
        if (homeTeleportExperienceActive && hasToPayExperience)
            arguments.getCmdSender().setLevel(arguments.getCmdSender().getLevel() - requiredLevels);

        //Teleport to home
        requestedHome.teleport(arguments.getCmdSender());

        //Send Success Message
        sendSuccessMessage(arguments, requestedHome.name());
        return true;
    }

    private boolean homeIsObstructed(Home home, double eyeLocation) {
        if (!home.location().isChunkLoaded()) home.location().getChunk().load();

        if (!home.location().getBlock().isPassable()) return true;
        if (!home.location().clone().add(0, 1, 0).getBlock().isPassable()) return true;
        if (!home.location().clone().add(0, eyeLocation, 0).getBlock().isPassable()) return true;

        return false;
    }

    private void sendSuccessMessage(PlayerNameGroupArguments arguments, String homeName) {
        if (arguments.isSelf()) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_SUCCESS, "name", homeName);
        } else {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.HOME_SUCCESS_OTHER, "name", homeName, "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName());
        }
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
