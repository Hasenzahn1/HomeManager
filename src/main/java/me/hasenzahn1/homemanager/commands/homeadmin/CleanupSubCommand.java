package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class CleanupSubCommand implements ISubCommand {

    public static final int CONFIRMATION_DURATION = 10;

    private final HashMap<Player, Long> executionTimestamps;

    public CleanupSubCommand() {
        executionTimestamps = new HashMap<>();
    }

    @Override
    public void onCommand(Player executor, String[] args) {
        if (executionTimestamps.getOrDefault(executor, 0L) < System.currentTimeMillis() - CONFIRMATION_DURATION * 1000) {
            executionTimestamps.put(executor, System.currentTimeMillis());
            MessageManager.sendMessage(executor, Language.HOME_ADMIN_CLEANUP_MESSAGE, "seconds", String.valueOf(CONFIRMATION_DURATION));
            return;
        }

        executionTimestamps.remove(executor);

        DatabaseAccessor database = DatabaseAccessor.openSession();
        int rowCount = database.cleanupHomes(Bukkit.getWorlds());
        database.destroy();
        MessageManager.sendMessage(executor, Language.HOME_ADMIN_CLEANUP_SUCCESS, "amount", String.valueOf(rowCount));
    }

    @Override
    public List<String> onTabComplete(CommandSender executor, String[] args) {
        return List.of();
    }

    @Override
    public String getName() {
        return "cleanup";
    }
}
