package me.hasenzahn1.homemanager.homes;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerTeleportation {

    private final PlayerNameArguments arguments;
    private final Player player;
    private final Home home;
    private final int experienceToBePaid;

    private BukkitTask timerTask;

    public PlayerTeleportation(PlayerNameArguments arguments, Home home, int experienceToBePaid) {
        this.arguments = arguments;
        this.player = arguments.getCmdSender();
        this.home = home;
        this.experienceToBePaid = experienceToBePaid;
    }

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

                //Send Message
                MessageManager.sendMessage(player, Language.TELEPORTATION_DELAY_MESSAGE, "seconds", String.valueOf(currentSeconds));
            }
        }.runTaskTimer(HomeManager.getInstance(), 0, 20);
    }

    private void teleport() {
        home.teleport(player);
        player.setLevel(Math.max(player.getLevel() - experienceToBePaid, 0));
        sendSuccessMessage();
        HomeManager.getInstance().removeTeleportation(player);
    }

    public void cancel() {
        arguments.getCmdSender().sendActionBar(Component.text(" "));
        MessageManager.sendMessage(arguments.getCmdSender(), Language.TELEPORTATION_CANCELLED);
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        HomeManager.getInstance().removeTeleportation(player);
    }

    private void sendSuccessMessage() {
        arguments.getCmdSender().sendActionBar(Component.text(" "));
        if (arguments.isSelf()) {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.TELEPORTATION_SUCCESS, "name", home.name());
        } else {
            MessageManager.sendMessage(arguments.getCmdSender(), Language.TELEPORTATION_SUCCESS_OTHER, "name", home.name(), "player", Bukkit.getOfflinePlayer(arguments.getActionPlayerUUID()).getName());
        }
    }

}
