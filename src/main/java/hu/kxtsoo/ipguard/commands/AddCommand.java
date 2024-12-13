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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(value = "mcipguard", alias = {"ipguard", "mc-ipguard"})
@Permission("ipguard.admin")
public class AddCommand extends BaseCommand {

    private final ConfigUtil configUtil;

    public AddCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @SubCommand("add")
    @Permission("ipguard.admin.add")
    public void add(CommandSender sender, String playerName, String ipAddress) {

        if (!isValidIPAddress(ipAddress)) {
            sender.sendMessage(configUtil.getMessage("messages.add-command.invalid-format"));
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        String uuid = player.getUniqueId().toString();

        if (uuid == null || uuid.isEmpty()) {
            sender.sendMessage(configUtil.getMessage("messages.player-not-found").replace("%player%", playerName));
            return;
        }

        try {
            DatabaseManager.addPlayer(uuid, ipAddress);
            sender.sendMessage(configUtil.getMessage("messages.add-command.added-player").replace("%player%", playerName)
                    .replace("%ip_address%", ipAddress));
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(configUtil.getMessage("messages.database-error"));
        }
    }

    private static final String IP_ADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static final String INVALID_IP_PATTERN =
            "^(0\\.0\\.0\\.0|127\\.0\\.0\\.1)$";

    private static final Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
    private static final Pattern invalidIpPattern = Pattern.compile(INVALID_IP_PATTERN);

    private static boolean isValidIPAddress(String ipAddress) {
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches() && !invalidIpPattern.matcher(ipAddress).matches();
    }
}