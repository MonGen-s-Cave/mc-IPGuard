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
import java.util.Objects;

@Command(value = "mcipguard", alias = {"ipguard", "mc-ipguard"})
@Permission("ipguard.admin")
public class CheckCommand extends BaseCommand {

    private final ConfigUtil configUtil;

    public CheckCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @SubCommand("check")
    @Permission("ipguard.admin.check")
    public void check(CommandSender sender, String playerName) {

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        try {
            if (DatabaseManager.doesPlayerExist(player.getUniqueId().toString())) {
                String ipAddress = DatabaseManager.getPlayerIP(player.getUniqueId().toString());
                sender.sendMessage(configUtil.getMessage("messages.check-command.player-exists")
                        .replace("%player%", Objects.requireNonNull(player.getName()))
                        .replace("%ip_address%", ipAddress));
            } else {
                sender.sendMessage(configUtil.getMessage("messages.player-not-found")
                        .replace("%player%", Objects.requireNonNull(player.getName())));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(configUtil.getMessage("messages.database-error"));
        }
    }
}