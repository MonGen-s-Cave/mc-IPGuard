package hu.kitsoo.gipguard.commands;

import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class CheckCommand implements CommandExecutor {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;

    public CheckCommand(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = configUtil.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        String noPermission = configUtil.getMessages().getString("messages.no-permission");
        noPermission = ChatUtil.colorizeHex(noPermission);

        if (!(sender.hasPermission("gipguard.check") || sender.hasPermission("gipguard.*") || sender.isOp())) {
            sender.sendMessage(prefix + noPermission);
            return true;
        }

        if (args.length != 2) {
            String usageMessage = configUtil.getMessages().getString("messages.check-usage");
            usageMessage = ChatUtil.colorizeHex(usageMessage);
            sender.sendMessage(prefix + usageMessage);
            return true;
        }

        String playerName = args[1];

        try {
            if (DatabaseManager.doesPlayerExist(playerName)) {
                String ipAddress = DatabaseManager.getPlayerIP(playerName);
                String playerExistsMessage = configUtil.getMessages().getString("messages.player-exists");
                playerExistsMessage = playerExistsMessage.replace("%player%", playerName).replace("%ip_address%", ipAddress);
                playerExistsMessage = ChatUtil.colorizeHex(playerExistsMessage);
                sender.sendMessage(prefix + playerExistsMessage);
            } else {
                String playerNotExistsMessage = configUtil.getMessages().getString("messages.player-not-exists");
                playerNotExistsMessage = playerNotExistsMessage.replace("%player%", playerName);
                playerNotExistsMessage = ChatUtil.colorizeHex(playerNotExistsMessage);
                sender.sendMessage(prefix + playerNotExistsMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();

            String databaseError = configUtil.getMessages().getString("messages.database-error");
            databaseError = ChatUtil.colorizeHex(databaseError);
            sender.sendMessage(prefix + databaseError);
        }

        return true;
    }
}
