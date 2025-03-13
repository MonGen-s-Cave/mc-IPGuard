package hu.kxtsoo.ipguard;

import hu.kxtsoo.ipguard.database.DatabaseManager;
import hu.kxtsoo.ipguard.hooks.HookManager;
import hu.kxtsoo.ipguard.listeners.PlayerJoinListener;
import hu.kxtsoo.ipguard.manager.CommandManager;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import hu.kxtsoo.ipguard.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public class IPGuard extends JavaPlugin {

    private ConfigUtil configUtil;
    private static IPGuard instance;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        instance = this;

        int pluginId = 21069;
        new Metrics(this, pluginId);

        configUtil = new ConfigUtil(this);
        configUtil.setupConfig();
        reloadConfig();

        try {
            DatabaseManager.initialize(configUtil, this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }

        hookManager = new HookManager(this, configUtil);
        hookManager.registerHooks();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, configUtil, hookManager), this);

        CommandManager commandManager = new CommandManager(this, configUtil);
        commandManager.registerSuggestions();
        commandManager.registerCommands();

        String red = "\u001B[38;2;184;15;37m";
        String reset = "\u001B[0m";
        String yellow = "\u001B[33m";
        String software = getServer().getName();
        String version = getServer().getVersion();

        getLogger().info(" ");
        getLogger().info(red + "   ___ ____   ____ _   _   _    ____  ____" + reset);
        getLogger().info(red + "   |_ _|  _ \\ / ___| | | | / \\  |  _ \\|  _ \\" + reset);
        getLogger().info(red + "    | || |_) | |  _| | | |/ _ \\ | |_) | | | |" + reset);
        getLogger().info(red + "    | ||  __/| |_| | |_| / ___ \\|  _ <| |_| |" + reset);
        getLogger().info(red + "   |___|_|    \\____|\\___/_/   \\_\\_| \\_\\____/ " + reset);
        getLogger().info(" ");
        getLogger().info(red + "   The plugin successfully started." + reset);
        getLogger().info(red + "   mc-IPGuard " + software + " " + version + reset);
        getLogger().info(yellow + "   Discord @ dc.mongenscave.com" + reset);
        getLogger().info(" ");

        if (configUtil.getConfig().getBoolean("update-checker.enabled", true)) {
            new UpdateChecker(this, configUtil, 5389);
        }
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