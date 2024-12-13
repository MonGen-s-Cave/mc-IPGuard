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
public class ReloadCommand extends BaseCommand {

    private final ConfigUtil configUtil;

    public ReloadCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @SubCommand("reload")
    @Permission("ipguard.admin.reload")
    public boolean reload(CommandSender sender) {

        sender.sendMessage(configUtil.getMessage("messages.reload-command.success"));
        configUtil.reloadConfig();

        try {
            DatabaseManager.initialize(configUtil);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
