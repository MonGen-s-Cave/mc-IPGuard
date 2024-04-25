package hu.kitsoo.gipguard;

import hu.kitsoo.gipguard.commands.*;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GIPGuardCommand implements CommandExecutor {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;
    private Map<String, CommandExecutor> commands = new HashMap<>();

    public GIPGuardCommand(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        initializeCommands();
    }

    private void initializeCommands() {
        commands.put("reload", new ReloadCommand(plugin, configUtil));
        commands.put("remove", new RemoveCommand(plugin, configUtil));
        commands.put("add", new AddCommand(plugin, configUtil));
        commands.put("list", new ListCommand(plugin, configUtil));
        commands.put("check", new CheckCommand(plugin, configUtil));
        commands.put("activate", new ActivateCommand(plugin, configUtil));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        List<String> helpMenuList = configUtil.getMessages().getStringList("help-menu");
        StringBuilder helpMenuBuilder = new StringBuilder();

        for (String line : helpMenuList) {
            helpMenuBuilder.append(line).append("\n");
        }

        String helpMenu = helpMenuBuilder.toString().trim();
        helpMenu = ChatUtil.colorizeHex(helpMenu);

        if (args.length == 0) {
            sender.sendMessage(helpMenu);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        CommandExecutor executor = commands.get(subCommand);

        if (executor != null) {
            return executor.onCommand(sender, command, label, args);
        } else {
            sender.sendMessage(helpMenu);
            return true;
        }
    }
}
