package hu.kxtsoo.ipguard.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import hu.kxtsoo.ipguard.database.DatabaseManager;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

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
        try {
            if (DatabaseManager.doesPlayerExist(playerName)) {
                String ipAddress = DatabaseManager.getPlayerIP(playerName);
                sender.sendMessage(configUtil.getMessage("messages.check-command.player-exists")
                        .replace("%player%", playerName)
                        .replace("%ip_address%", ipAddress));
            } else {
                sender.sendMessage(configUtil.getMessage("messages.player-not-found")
                        .replace("%player%", playerName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(configUtil.getMessage("messages.database-error"));
        }
    }
}