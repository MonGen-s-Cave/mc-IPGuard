package hu.kitsoo.gipguard.commands;

import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;

public class ActivateCommand implements CommandExecutor {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;

    public ActivateCommand(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        String prefix = configUtil.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        String noPermission = configUtil.getMessages().getString("messages.no-permission");
        noPermission = ChatUtil.colorizeHex(noPermission);

        if (!(sender.hasPermission("gipguard.activate") || sender.hasPermission("gipguard.*") || sender.isOp())) {
            sender.sendMessage(prefix + noPermission);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + "Only players can execute this command.");
            return true;
        }

        String playerName = player.getName();
        String ipAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();

        try {
            if (DatabaseManager.doesPlayerExist(playerName)) {
                DatabaseManager.removePlayer(playerName);
                String deactivateMessage = configUtil.getMessages().getString("messages.deactivation-successful", "&x&2&b&f&b&0&0You have successfully disabled IP Protection on the account!");
                deactivateMessage = ChatUtil.colorizeHex(deactivateMessage);
                sender.sendMessage(prefix + deactivateMessage);
            } else {
                DatabaseManager.addPlayer(playerName, ipAddress);
                String activateMessage = configUtil.getMessages().getString("messages.activation-successful", "Y&x&2&b&f&b&0&0ou have activated IP Protection for the account %player%. The IP address is %ip_address%");
                activateMessage = activateMessage.replace("%player%", playerName).replace("%ip_address%", ipAddress);
                activateMessage = ChatUtil.colorizeHex(activateMessage);
                sender.sendMessage(prefix + activateMessage);
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