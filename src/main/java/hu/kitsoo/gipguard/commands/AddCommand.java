package hu.kitsoo.gipguard.commands;

import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddCommand implements CommandExecutor {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;

    public AddCommand(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = configUtil.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        String noPermission = configUtil.getMessages().getString("messages.no-permission");
        noPermission = ChatUtil.colorizeHex(noPermission);

        String badUsage = configUtil.getMessages().getString("messages.too-many-args");
        badUsage = ChatUtil.colorizeHex(badUsage);

        if (!(sender.hasPermission("gipguard.add") || sender.hasPermission("gipguard.*") || sender.isOp())) {
            sender.sendMessage(prefix + noPermission);
            return true;
        }

        if (args.length != 2 && args.length != 3) {
            sender.sendMessage(prefix + badUsage);
            return true;
        }

        String playerName;
        String ipAddress;

        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + badUsage);
                return true;
            }

            playerName = ((Player) sender).getName();
            ipAddress = args[1];
        } else {
            playerName = args[1];
            ipAddress = args[2];
        }

        if (!isValidIPAddress(ipAddress)) {
            String invalidFormat = configUtil.getMessages().getString("messages.invalid-format");
            invalidFormat = ChatUtil.colorizeHex(invalidFormat);

            sender.sendMessage(prefix + invalidFormat);
            return true;
        }

        try {
            DatabaseManager.addPlayer(playerName, ipAddress);

            String successAdded = configUtil.getMessages().getString("messages.added-player");
            successAdded = ChatUtil.colorizeHex(successAdded);
            successAdded = successAdded.replace("%player%", playerName);
            successAdded = successAdded.replace("%ip_address%", ipAddress);

            sender.sendMessage(prefix + successAdded);
        } catch (SQLException e) {
            e.printStackTrace();

            String databaseError = configUtil.getMessages().getString("messages.database-error");
            databaseError = ChatUtil.colorizeHex(databaseError);
            sender.sendMessage(prefix + databaseError);
        }

        return true;
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

    public static boolean isValidIPAddress(String ipAddress) {
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches() && !invalidIpPattern.matcher(ipAddress).matches();
    }
}