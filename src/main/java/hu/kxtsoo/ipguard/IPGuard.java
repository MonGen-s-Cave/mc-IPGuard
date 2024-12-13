package hu.kxtsoo.ipguard;

import hu.kxtsoo.ipguard.database.DatabaseManager;
import hu.kxtsoo.ipguard.events.JoinEvent;
import hu.kxtsoo.ipguard.manager.CommandManager;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public class IPGuard extends JavaPlugin {

    private ConfigUtil configUtil;
    private static IPGuard instance;

    @Override
    public void onEnable() {
        instance = this;

        int pluginId = 21069;
        new Metrics(this, pluginId);

        configUtil = new ConfigUtil(this);
        configUtil.setupConfig();
        reloadConfig();

        try {
            DatabaseManager.initialize(configUtil);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }

        getServer().getPluginManager().registerEvents(new JoinEvent(this, configUtil), this);

//        String authPlugin = configUtil.getConfig().getString("hooks.auth", "").toLowerCase();
//        if (authPlugin.isEmpty()) {
//            getLogger().info("No authentication plugin hook is configured.");
//            return;
//        }
//
//        switch (authPlugin) {
//            case "authme":
//                if (getServer().getPluginManager().getPlugin("AuthMe") != null) {
//                    getLogger().info(ChatColor.AQUA + "Hooking into AuthMe for authentication.");
//                } else {
//                    getLogger().warning("AuthMe is configured but not found. Please install AuthMe or update the configuration.");
//                }
//                break;
//            case "nlogin":
//                if (getServer().getPluginManager().getPlugin("nLogin") != null) {
//                    getLogger().info(ChatColor.AQUA + "Hooking into nLogin for authentication.");
//                } else {
//                    getLogger().warning("nLogin is configured but not found. Please install nLogin or update the configuration.");
//                }
//                break;
//            default:
//                getLogger().warning("Unsupported authentication plugin specified in the configuration.");
//                break;
//        }

        CommandManager commandManager = new CommandManager(this, configUtil);
        commandManager.registerSuggestions();
        commandManager.registerCommands();

        String red = "\u001B[38;2;184;15;37m";
        String reset = "\u001B[0m";
        String yellow = "\u001B[33m";
        String software = getServer().getName();
        String version = getServer().getVersion();

        System.out.println(" ");
        System.out.println(red + "   ___ ____   ____ _   _   _    ____  ____" + reset);
        System.out.println(red + "   |_ _|  _ \\ / ___| | | | / \\  |  _ \\|  _ \\" + reset);
        System.out.println(red + "    | || |_) | |  _| | | |/ _ \\ | |_) | | | |" + reset);
        System.out.println(red + "    | ||  __/| |_| | |_| / ___ \\|  _ <| |_| |" + reset);
        System.out.println(red + "   |___|_|    \\____|\\___/_/   \\_\\_| \\_\\____/ " + reset);
        System.out.println(" ");
        System.out.println(red + "   The plugin successfully started." + reset);
        System.out.println(red + "   mc-IPGuard " + software + " " + version + reset);
        System.out.println(yellow + "   Discord @ dc.mongenscave.com" + reset);
        System.out.println(" ");
    }

    public static IPGuard getInstance() {
        return instance;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }

    @Override
    public void onDisable() {
        try {
            DatabaseManager.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}