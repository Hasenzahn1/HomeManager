package me.hasenzahn1.homemanager.commands.homeadmin;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.Logger;
import me.hasenzahn1.homemanager.MessageManager;
import me.hasenzahn1.homemanager.commands.system.ISubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

public class ReloadSubCommand implements ISubCommand {
    @Override
    public void onCommand(Player executor, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(executor, Language.INVALID_COMMAND, "command", "/homeadmin " + getName() + " <config/groups/lang>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "config":
                HomeManager.getInstance().reloadConfig();
                Logger.DEBUG.log("Reloaded config.yml from disk");
                break;
            case "groups":
                HomeManager.getInstance().getHomesCache().invalidateAll();
                HomeManager.getInstance().getWorldGroupManager().reloadFromDisk();
                Logger.DEBUG.log("Reloaded groups.yml from disk");
                break;
            case "lang":
                Language.reload();
                Logger.DEBUG.log("Reloaded lang.yml from disk");
                break;
        }

        MessageManager.sendMessage(executor, Language.HOME_ADMIN_RELOAD_SUCCESS, "config", args[0].toLowerCase());
    }

    @Override
    public List<String> onTabComplete(CommandSender executor, String[] args) {
        if (args.length == 1) {
            return Stream.of("config", "groups", "lang").filter(s -> s.startsWith(args[0])).sorted().toList();
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "reload";
    }
}
