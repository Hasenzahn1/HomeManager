package me.hasenzahn1.homemanager.homes.teleportation;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.homes.Home;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the teleportation process of a player to their home.
 * Handles the teleportation delay, experience payment, and sending appropriate messages to the player.
 */
public class PlayerTeleportation {

    private final PlayerNameArguments arguments;
    private final Player player;
    private final Home home;
    private final int experienceToBePaid;

    private BukkitTask timerTask;

    /**
     * Constructs a PlayerTeleportation object.
     * Initializes the teleportation parameters.
     *
     * @param arguments          The arguments provided by the player initiating the teleportation.
     * @param home               The home the player is teleporting to.
     * @param experienceToBePaid The experience cost for the teleportation.
     */
    public PlayerTeleportation(PlayerNameArguments arguments, Home home, int experienceToBePaid) {
        this.arguments = arguments;
        this.player = arguments.getCmdSender();
        this.home = home;
        this.experienceToBePaid = experienceToBePaid;
    }

    /**
     * Starts the teleportation process with an optional delay.
     * If delay is 0, the teleportation happens immediately.
     *
     * @param delay The delay in seconds before teleportation.
     */
    public void startTeleportation(int delay) {
        if (delay == 0) {
            teleport();
            return;
        }

        AtomicInteger counter = new AtomicInteger(delay);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                int currentSeconds = counter.decrementAndGet();
                if (currentSeconds == 0) {
                    teleport();
                    cancel();
                    return;
                }

                // Send Message
                MessageManager.sendMessage(player, Language.TELEPORTATION_DELAY_MESSAGE, "seconds", String.valueOf(currentSeconds));
            }
        }.runTaskTimer(HomeManager.getInstance(), 0, 20);
    }

    /**
     * Teleports the player to their home and deducts the required experience.
     * Sends a success message and logs the teleportation.
     */
    private void teleport() {
        home.teleport(player);
        player.setLevel(Math.max(player.getLevel() - experienceToBePaid, 0));
        sendSuccessMessage();
        TeleportationManager.getInstance().removeTeleportation(player);
        Logger.DEBUG.log("Teleported player " + player.getName() + " to home " + home.name() + " at location (" + home.location().getBlockX() + ", " + home.location().getBlockY() + ", " + home.location().getBlockZ() + ")");
    }

    /**
     * Cancels the teleportation process.
     * Removes any active teleportation and sends a cancellation message.
     */
    public void cancel() {
        arguments.getCmdSender().sendActionBar(Component.text(" "));
        MessageManager.sendMessage(arguments.getCmdSender(), Language.TELEPORTATION_CANCELLED);
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        TeleportationManager.getInstance().removeTeleportation(player);
        Logger.DEBUG.log("Cancelled teleportation of player " + player.getName() + " to home " + home.name());
    }

    /**
     * Sends a success message to the player after a successful teleportation.
     * The message varies depending on whether the teleportation was for the player themselves or another player.
     */
    private void sendSuccessMessage() {
        arguments.getCmdSender().sendActionBar(Component.text(" "));
        if (arguments.isSelf()) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.TELEPORTATION_SUCCESS, "homename", home.name());
        } else {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.TELEPORTATION_SUCCESS_OTHER, "homename", home.name(), "player", arguments.getOptionalPlayerName());
        }
    }

}

