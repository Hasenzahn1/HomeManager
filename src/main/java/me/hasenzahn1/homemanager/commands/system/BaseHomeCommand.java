package me.hasenzahn1.homemanager.commands.system;

import me.hasenzahn1.homemanager.commands.args.PlayerArguments;
import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import me.hasenzahn1.homemanager.permission.PermissionValidator;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public abstract class BaseHomeCommand implements CommandExecutor, TabCompleter {

    protected final CompletionsHelper completionsHelper;

    public BaseHomeCommand(CompletionsHelper completionsHelper) {
        this.completionsHelper = completionsHelper;
    }

    protected boolean hasBypass(PlayerArguments arguments, String permission) {
        return PermissionValidator.hasBypassPermission(arguments.getCmdSender(), permission, arguments.getWorldGroup());
    }
}