package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.Language;
import me.hasenzahn1.homemanager.db.DatabaseAccessor;
import me.hasenzahn1.homemanager.group.WorldGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class SetHomeCommand implements CommandExecutor {

    private final Pattern nameMatcher = Pattern.compile("^[A-Za-z0-9ß#+_-]{1,16}$");

    // /sethome (player) \<name>
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        //Check player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PLAYER)));
            return true;
        }

        //Get Sender's location data
        Player player = (Player) commandSender;
        Location location = player.getLocation();
        WorldGroup group = HomeManager.getInstance().getWorldGroupManager().groupsByWorld().get(location.getWorld());

        //Check base sethome permission
        if (!commandSender.hasPermission("homemanager.commands.sethome." + group.getName())) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION)));
            return true;
        }

        //Check Command Arg Range
        if (args.length > 2) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.INVALID_COMMAND, "command", "/sethome (player) <name>")));
            return true;
        }

        //Gather arguments
        String playerToSetHomeTo = "";
        String homeName = "";
        if (args.length == 1) {
            playerToSetHomeTo = commandSender.getName();
            homeName = args[0];
        } else {
            playerToSetHomeTo = args[0];
            homeName = args[1];
        }

        //No valid set player
        if (Bukkit.getPlayer(playerToSetHomeTo) == null) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.UNKNOWN_PLAYER)));
            return true;
        }

        //Check other Permission
        if (!commandSender.hasPermission("homemanager.commands.sethome.other." + group.getName())) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.NO_PERMISSION_OTHER)));
            return true;
        }

        //Check home name
        if (!nameMatcher.matcher(homeName).matches()) {
            commandSender.sendMessage(Component.text(HomeManager.PREFIX + Language.getLang(Language.SET_HOME_INVALID_HOME_NAME)));
            return true;
        }

        //Save home to database
        DatabaseAccessor dbSession = DatabaseAccessor.openSession();
        dbSession.saveHomeToDatabase(Bukkit.getPlayerUniqueId(playerToSetHomeTo), homeName, location);
        dbSession.destroy();
        return true;
    }
}
