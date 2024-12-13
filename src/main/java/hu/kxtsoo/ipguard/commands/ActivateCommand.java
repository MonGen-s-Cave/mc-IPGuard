package hu.kxtsoo.ipguard.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import hu.kxtsoo.ipguard.database.DatabaseManager;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

@Command(value = "mcipguard", alias = {"ipguard", "mc-ipguard"})
@Permission("ipguard")
public class ActivateCommand extends BaseCommand {

    private final ConfigUtil configUtil;

    public ActivateCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @SubCommand("activate")
    @Permission("ipguard.activate")
    public void activate(CommandSender sender) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(configUtil.getMessage("messages.only-player"));
            return;
        }

        String ipAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();

        try {
            if (DatabaseManager.doesPlayerExist(player.getUniqueId().toString())) {
                DatabaseManager.removePlayer(player.getUniqueId().toString());
                sender.sendMessage(configUtil.getMessage("messages.activate-command.deactivated"));
            } else {
                DatabaseManager.addPlayer(player.getUniqueId().toString(), ipAddress);
                sender.sendMessage(configUtil.getMessage("messages.activate-command.activated").replace("%player%", player.getName()).replace("%ip_address%", ipAddress));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(configUtil.getMessage("messages.database-error"));
        }
    }
}