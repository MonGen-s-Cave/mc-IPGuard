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

        if (getConfig().getBoolean("update-checker.enabled", true)) {
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