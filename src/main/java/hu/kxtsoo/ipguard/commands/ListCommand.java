package hu.kxtsoo.ipguard.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import hu.kxtsoo.ipguard.util.ConfigUtil;

@Command(value = "mcipguard", alias = {"ipguard", "mc-ipguard"})
@Permission("ipguard.admin")
public class ListCommand extends BaseCommand {

    private final ConfigUtil configUtil;

    public ListCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

//    @SubCommand("list")
//    @Permission("ipguard.list")
//    public void list(CommandSender sender) {
//        try {
//            List<String> playerNames = DatabaseManager.getDatabasePlayerNames();
//            StringBuilder messageBuilder = new StringBuilder();
//
//            if (playerNames.isEmpty()) {
//                String noPlayerInDatabase = ChatUtil.colorizeHex(configUtil.getMessage("messages.no-players-list"));
//                messageBuilder.append(prefix).append(noPlayerInDatabase);
//            } else {
//                String playersInDatabase = ChatUtil.colorizeHex(configUtil.getMessage("messages.players-list"));
//                messageBuilder.append(prefix).append(playersInDatabase).append("\n");
//
//                String playerNameFormat = ChatUtil.colorizeHex(configUtil.getMessage("messages.player-name-format"));
//                String ipAddressFormat = ChatUtil.colorizeHex(configUtil.getMessage("messages.ip-address-format"));
//
//                for (String playerName : playerNames) {
//                    String formattedName = playerNameFormat.replace("%player%", playerName);
//                    String ipAddress = DatabaseManager.getPlayerIP(playerName);
//                    String formattedIPAddress = ipAddressFormat.replace("%ip_address%", ipAddress);
//                    messageBuilder.append(formattedName).append(" ").append(formattedIPAddress).append("\n");
//                }
//            }
//
//            sender.sendMessage(messageBuilder.toString().trim());
//        } catch (SQLException e) {
//            e.printStackTrace();
//            String databaseError = ChatUtil.colorizeHex(configUtil.getMessage("messages.database-error"));
//            sender.sendMessage(prefix + databaseError);
//        }
//    }
}