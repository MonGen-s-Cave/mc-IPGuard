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
public class RemoveCommand extends BaseCommand {

    private final ConfigUtil configUtil;

    public RemoveCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @SubCommand("remove")
    @Permission("ipguard.admin.remove")
    public void remove(CommandSender sender, String playerName) {

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        try {
            if (DatabaseManager.removePlayer(player.getUniqueId().toString())) {
                sender.sendMessage(configUtil.getMessage("messages.remove-command.removed-player")
                        .replace("%player%", Objects.requireNonNull(player.getName())));
            } else {
                sender.sendMessage(configUtil.getMessage("messages.player-not-found"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(configUtil.getMessage("messages.database-error"));
        }
    }
}