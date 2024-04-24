package hu.kitsoo.gipguard.commands;

import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;

public class ListCommand implements CommandExecutor {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;

    public ListCommand(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = configUtil.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        String noPermission = configUtil.getMessages().getString("messages.no-permission");
        noPermission = ChatUtil.colorizeHex(noPermission);

        if (!(sender.hasPermission("gipguard.list") || sender.hasPermission("gipguard.*") || sender.isOp())) {
            sender.sendMessage(prefix + noPermission);
            return true;
        }

        try {
            List<String> playerNames = DatabaseManager.getDatabasePlayerNames();
            StringBuilder messageBuilder = new StringBuilder();
            if (playerNames.isEmpty()) {
                String noPlayerInDatabase = configUtil.getMessages().getString("messages.no-players-list");
                noPlayerInDatabase = ChatUtil.colorizeHex(noPlayerInDatabase);

                messageBuilder.append(prefix).append(noPlayerInDatabase);
            } else {
                String PlayersInDatabase = configUtil.getMessages().getString("messages.players-list");
                PlayersInDatabase = ChatUtil.colorizeHex(PlayersInDatabase);
                messageBuilder.append(prefix).append(PlayersInDatabase);
                String playerNameFormat = configUtil.getMessages().getString("messages.player-name-format");
                playerNameFormat = ChatUtil.colorizeHex(playerNameFormat);
                playerNameFormat = playerNameFormat.replace("%player%", "{player}");
                String ipAddressFormat = configUtil.getMessages().getString("messages.ip-address-format");
                ipAddressFormat = ChatUtil.colorizeHex(ipAddressFormat);
                ipAddressFormat = ipAddressFormat.replace("%ip_address%", "{ip}");
                for (String playerName : playerNames) {
                    String formattedName = playerNameFormat.replace("{player}", playerName);
                    String ipAddress = DatabaseManager.getPlayerIP(playerName);
                    String formattedIPAddress = ipAddressFormat.replace("{ip}", ipAddress);
                    messageBuilder.append(formattedName).append(" ").append(formattedIPAddress).append(", ");
                }
                messageBuilder.setLength(messageBuilder.length() - 2);
            }
            String message = messageBuilder.toString();
            sender.sendMessage(message);
        } catch (SQLException e) {
            e.printStackTrace();

            String databaseError = configUtil.getMessages().getString("messages.database-error");
            databaseError = ChatUtil.colorizeHex(databaseError);
            sender.sendMessage(prefix + databaseError);
        }

        return true;
    }
}
