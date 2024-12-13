package hu.kxtsoo.ipguard.manager;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import hu.kxtsoo.ipguard.IPGuard;
import hu.kxtsoo.ipguard.commands.*;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.command.CommandSender;

public class CommandManager {
    private final BukkitCommandManager<CommandSender> commandManager;
    private ConfigUtil configUtil;
    private IPGuard plugin;

    public CommandManager(IPGuard plugin, ConfigUtil configUtil) {
        this.commandManager = BukkitCommandManager.create(plugin);
        this.configUtil = configUtil;
        this.plugin = plugin;
    }

    public void registerSuggestions() {}

    public void registerCommands() {
        commandManager.registerCommand(new ReloadCommand(configUtil, plugin));
        commandManager.registerCommand(new AddCommand(configUtil));
        commandManager.registerCommand(new ActivateCommand(configUtil));
        commandManager.registerCommand(new RemoveCommand(configUtil));
        commandManager.registerCommand(new CheckCommand(configUtil));
        commandManager.registerCommand(new ListCommand(configUtil));
    }
}