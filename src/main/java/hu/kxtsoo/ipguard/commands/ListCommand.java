package hu.kxtsoo.ipguard.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import hu.kxtsoo.ipguard.database.DatabaseManager;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Command(value = "mcipguard", alias = {"ipguard", "mc-ipguard"})
@Permission("ipguard.admin")
public class ListCommand extends BaseCommand {

    private final ConfigUtil configUtil;

    public ListCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @SubCommand("list")
    @Permission("ipguard.admin.list")
    public void list(CommandSender sender) {
        try {
            List<String> uuids = DatabaseManager.getDatabasePlayerNames();

            if (uuids.isEmpty()) {
                sender.sendMessage(configUtil.getMessage("messages.list-command.no-players"));
                return;
            }

            StringBuilder playersList = new StringBuilder(configUtil.getMessage("messages.list-command.players-list")).append(" ");

            for (String uuidStr : uuids) {
                UUID uuid = UUID.fromString(uuidStr);
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                String playerName = player.getName() != null ? player.getName() : "Unknown";
                String ipAddress = DatabaseManager.getPlayerIP(uuidStr);

                String formattedPlayer = configUtil.getMessage("messages.list-command.format.player-name")
                        .replace("%player%", playerName);
                String formattedIP = configUtil.getMessage("messages.list-command.format.ip-address")
                        .replace("%ip_address%", ipAddress);

                playersList.append(formattedPlayer).append(" ").append(formattedIP).append(" ");
            }

            sender.sendMessage(playersList.toString().trim());
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(configUtil.getMessage("messages.database-error"));
        }
    }
}