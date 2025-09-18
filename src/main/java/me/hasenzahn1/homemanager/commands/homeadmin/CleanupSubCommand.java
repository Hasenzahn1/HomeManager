package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;

public class CleanupSubCommand implements ISubCommand {


    private final HashMap<CommandSender, Long> executionTimestamps;

    public CleanupSubCommand() {
        executionTimestamps = new HashMap<>();
    }

    @Override
    public void onCommand(CommandSender executor, String[] args) {
        if (executionTimestamps.getOrDefault(executor, 0L) < System.currentTimeMillis() - DefaultConfig.HOME_ADMIN_CONFIRMATION_DURATION * 1000) {
            executionTimestamps.put(executor, System.currentTimeMillis());
            
            DatabaseAccessor accessor = DatabaseAccessor.openSession();
            int count = accessor.cleanupHomesGetAmount(Bukkit.getWorlds());
            accessor.destroy();

            MessageManager.sendMessage(executor, Language.HOME_ADMIN_CLEANUP_MESSAGE, "seconds", String.valueOf(DefaultConfig.HOME_ADMIN_CONFIRMATION_DURATION), "amount", String.valueOf(count));
            return;
        }

        executionTimestamps.remove(executor);

        DatabaseAccessor database = DatabaseAccessor.openSession();
        int rowCount = database.cleanupHomes(Bukkit.getWorlds());
        database.destroy();
        HomeManager.getInstance().getHomesCache().invalidateAll();
        MessageManager.sendMessage(executor, Language.HOME_ADMIN_CLEANUP_SUCCESS, "amount", String.valueOf(rowCount));
        Logger.INFO.log("Cleaned " + rowCount + " homes from the database");
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
