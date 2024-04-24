package hu.kitsoo.gipguard.commands;

import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.List;

public class ReloadCommand implements CommandExecutor, Listener {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;

    public ReloadCommand(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = configUtil.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        String noPermission = configUtil.getMessages().getString("no-permission");
        noPermission = ChatUtil.colorizeHex(noPermission);

        if (!(sender.hasPermission("gipguard.reload") || sender.hasPermission("gipguard.*") || sender.isOp())) {
            sender.sendMessage(prefix + noPermission);
            return true;
        }

        List<String> helpMenuList = configUtil.getMessages().getStringList("help-menu");
        StringBuilder helpMenuBuilder = new StringBuilder();

        for (String line : helpMenuList) {
            helpMenuBuilder.append(line).append("\n");
        }

        String helpMenu = helpMenuBuilder.toString().trim();

        helpMenu = ChatUtil.colorizeHex(helpMenu);

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(helpMenu);
            return false;
        }

        reloadPlugin(sender);

        return true;
    }

    private void reloadPlugin(CommandSender sender) {
        String prefix = configUtil.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        try {
            configUtil.reloadConfig();
            DatabaseManager.initialize(configUtil);

            String reloadSuccess = configUtil.getMessages().getString("messages.reload-success");
            reloadSuccess = ChatUtil.colorizeHex(reloadSuccess);
            sender.sendMessage(prefix + reloadSuccess);
            System.out.println("The plugin successfully reloaded!");

        } catch (Exception e) {
            String databaseError = configUtil.getMessages().getString("messages.database-error");
            databaseError = ChatUtil.colorizeHex(databaseError);
            sender.sendMessage(prefix + databaseError);
            e.printStackTrace();
        }
    }
}
