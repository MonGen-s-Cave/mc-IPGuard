package hu.kitsoo.gipguard.util;

import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {

    private final GIPGuard plugin;

    public TabComplete (GIPGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("activate");
            completions.add("reload");
            completions.add("add");
            completions.add("remove");
            completions.add("list");
            completions.add("check");
        } else if (args.length == 2 && "check".equalsIgnoreCase(args[0])) {
            completions.add("<player>");
        } else if (args.length == 2 && "add".equalsIgnoreCase(args[0])) {
            completions.addAll(getOnlinePlayerNames());
        } else if (args.length == 2 && "remove".equalsIgnoreCase(args[0])) {
            try {
                completions.addAll(DatabaseManager.getDatabasePlayerNames());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return completions;
    }
    private List<String> getOnlinePlayerNames() {
        List<String> onlinePlayerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayerNames.add(player.getName());
        }
        return onlinePlayerNames;
    }

}
