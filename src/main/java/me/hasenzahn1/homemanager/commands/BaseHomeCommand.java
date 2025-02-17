package me.hasenzahn1.homemanager.commands;

import me.hasenzahn1.homemanager.commands.tabcompletion.CompletionsHelper;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public abstract class BaseHomeCommand implements CommandExecutor, TabCompleter {

    protected final CompletionsHelper completionsHelper;

    public BaseHomeCommand(CompletionsHelper completionsHelper) {
        this.completionsHelper = completionsHelper;
    }
}