package hu.kitsoo.gipguard;

import hu.kitsoo.gipguard.commands.*;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GIPGuardCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ConfigUtil configUtil;

    public GIPGuardCommand(JavaPlugin plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = configUtil.getMessages().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        List<String> helpMenuList = configUtil.getMessages().getStringList("help-menu");
        StringBuilder helpMenuBuilder = new StringBuilder();

        for (String line : helpMenuList) {
            helpMenuBuilder.append(line).append("\n");
        }

        String helpMenu = helpMenuBuilder.toString().trim();
        helpMenu = ChatUtil.colorizeHex(helpMenu);

        if (args.length == 0 || args.length < 1) {
            sender.sendMessage(helpMenu);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                ReloadCommand reloadCommand = new ReloadCommand((GIPGuard) plugin, configUtil);
                return reloadCommand.onCommand(sender, command, label, args);
            case "remove":
                RemoveCommand removeCommand = new RemoveCommand((GIPGuard) plugin, configUtil);
                return removeCommand.onCommand(sender, command, label, args);
            case "add":
                AddCommand addCommand = new AddCommand((GIPGuard) plugin, configUtil);
                return addCommand.onCommand(sender, command, label, args);
            case "list":
                ListCommand listCommand = new ListCommand((GIPGuard) plugin, configUtil);
                return listCommand.onCommand(sender, command, label, args);
            case "check":
                CheckCommand checkCommand = new CheckCommand((GIPGuard) plugin, configUtil);
                return checkCommand.onCommand(sender, command, label, args);
            case "activate":
                ActivateCommand activateCommand = new ActivateCommand((GIPGuard) plugin, configUtil);
                activateCommand.onCommand(sender, command, label, args);
                return true;
            default:
                sender.sendMessage(helpMenu);
                return true;
        }
    }
}