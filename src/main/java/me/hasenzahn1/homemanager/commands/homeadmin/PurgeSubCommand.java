package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import me.hasenzahn1.homemanager.config.DefaultConfig;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.HashMap;
import java.util.List;

public class PurgeSubCommand implements ISubCommand {

    private final HashMap<Player, Long> executionTimestamps;
    private final HashMap<Player, String> executedHomes;

    public PurgeSubCommand() {
        executionTimestamps = new HashMap<>();
        executedHomes = new HashMap<>();
    }

    @Override
    public void onCommand(Player executor, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin " + getName() + " (world)");
            return;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            MessageManager.sendMessage(executor, Language.INVALID_WORLD, "world", worldName);
            return;
        }

        if (executionTimestamps.getOrDefault(executor, 0L) < System.currentTimeMillis() - DefaultConfig.HOME_ADMIN_CONFIRMATION_DURATION * 1000) {
            executionTimestamps.put(executor, System.currentTimeMillis());
            executedHomes.put(executor, worldName);
            MessageManager.sendMessage(executor, Language.HOME_ADMIN_PURGE_MESSAGE, "seconds", String.valueOf(DefaultConfig.HOME_ADMIN_CONFIRMATION_DURATION));
            return;
        }

        if (!worldName.equalsIgnoreCase(executedHomes.get(executor))) {
            executionTimestamps.put(executor, System.currentTimeMillis());
            executedHomes.put(executor, worldName);
            MessageManager.sendMessage(executor, Language.HOME_ADMIN_PURGE_MESSAGE, "seconds", String.valueOf(DefaultConfig.HOME_ADMIN_CONFIRMATION_DURATION));
            return;
        }

        executionTimestamps.remove(executor);
        executedHomes.remove(executor);
        DatabaseAccessor session = DatabaseAccessor.openSession();
        int rowCount = session.purgeHomeInWorld(world);
        session.destroy();
        HomeManager.getInstance().getHomesCache().invalidateAll();
        MessageManager.sendMessage(executor, Language.HOME_ADMIN_PURGE_SUCCESS, "amount", String.valueOf(rowCount));
        Logger.INFO.log("Purged " + rowCount + " homes from the database");
    }

    @Override
    public List<String> onTabComplete(CommandSender executor, String[] args) {
        if (args.length == 1) {
            return Bukkit.getWorlds().stream().map(WorldInfo::getName).filter(n -> n.startsWith(args[0])).sorted().toList();
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "purge";
    }
}
