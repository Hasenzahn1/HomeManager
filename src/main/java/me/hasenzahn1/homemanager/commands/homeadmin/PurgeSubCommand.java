package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.HashMap;
import java.util.List;

public class PurgeSubCommand implements ISubCommand {

    public static final int CONFIRMATION_DURATION = 10;

    private final HashMap<Player, Long> executionTimestamps;

    public PurgeSubCommand() {
        executionTimestamps = new HashMap<>();
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
            MessageManager.sendMessage(executor, Language.HOME_ADMIN_PURGE_INVALID_WORLD, "world", worldName);
            return;
        }

        if (executionTimestamps.getOrDefault(executor, 0L) < System.currentTimeMillis() - CONFIRMATION_DURATION * 1000) {
            executionTimestamps.put(executor, System.currentTimeMillis());
            MessageManager.sendMessage(executor, Language.HOME_ADMIN_PURGE_MESSAGE, "seconds", String.valueOf(CONFIRMATION_DURATION));
            return;
        }

        executionTimestamps.remove(executor);
        DatabaseAccessor session = DatabaseAccessor.openSession();
        int rowCount = session.purgeHomeInWorld(world);
        MessageManager.sendMessage(executor, Language.HOME_ADMIN_PURGE_SUCCESS, "amount", String.valueOf(rowCount));
        session.destroy();
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
