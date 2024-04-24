package hu.kitsoo.gipguard.commands;

import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.sql.SQLException;

public class RemoveCommand implements CommandExecutor, Listener {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;

    public RemoveCommand(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = configUtil.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        String noPermission = configUtil.getMessages().getString("messages.no-permission");
        noPermission = ChatUtil.colorizeHex(noPermission);

        String notFound = configUtil.getMessages().getString("messages.player-not-found");
        notFound = ChatUtil.colorizeHex(notFound);

        if (!(sender.hasPermission("gipguard.remove") || sender.hasPermission("gipguard.*") || sender.isOp())) {
            sender.sendMessage(prefix + noPermission);
            return true;
        }

        if (args.length != 2) {
            String badUsage = configUtil.getMessages().getString("messages.too-many-args");
            badUsage = ChatUtil.colorizeHex(badUsage);

            sender.sendMessage(prefix + badUsage);
            return true;
        }

        String playerName = args[1];

        try {
            if (DatabaseManager.removePlayer(playerName)) {
                String successRemoved = configUtil.getMessages().getString("messages.removed-player");
                successRemoved = ChatUtil.colorizeHex(successRemoved);
                successRemoved = successRemoved.replace("%player%", playerName);

                sender.sendMessage(prefix + successRemoved);
            } else {
                sender.sendMessage(prefix + notFound);
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