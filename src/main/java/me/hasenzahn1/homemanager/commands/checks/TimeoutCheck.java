package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.commands.args.PlayerNameGroupArguments;
import me.hasenzahn1.homemanager.group.WorldGroup;
import org.bukkit.entity.Player;

public class TimeoutCheck {

    public boolean isInTimeout(Player player, WorldGroup worldGroup) {
        return System.currentTimeMillis() - HomeManager.getInstance().getTimeoutListener().getLastTimestamp(player) < worldGroup.getSettings().getTimeoutDurationInSeconds() * 1000;
    }

    public int getRemainingSeconds(PlayerNameGroupArguments arguments) {
        double durationInMillis = arguments.getWorldGroup().getSettings().getTimeoutDurationInSeconds() * 1000 - (System.currentTimeMillis() - HomeManager.getInstance().getTimeoutListener().getLastTimestamp(arguments.getCmdSender()));
        return (int) Math.round(durationInMillis / 1000.0);
    }

}
