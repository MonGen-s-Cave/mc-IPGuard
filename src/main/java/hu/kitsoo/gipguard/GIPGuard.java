package hu.kitsoo.gipguard;

import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.events.JoinEvent;
import hu.kitsoo.gipguard.util.ConfigUtil;
import hu.kitsoo.gipguard.util.TabComplete;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public class GIPGuard extends JavaPlugin {

    private ConfigUtil configUtil;

    @Override
    public void onEnable() {
        configUtil = new ConfigUtil(this);
        configUtil.setupConfig();

        int pluginId = 21069;
        new Metrics(this, pluginId);

        getLogger().info("Plugin developed by Glowing Studios. https://discord.gg/esxwNC4DmZ");

        try {
            DatabaseManager.initialize(configUtil);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }

        getServer().getPluginManager().registerEvents(new JoinEvent(this, configUtil), this);
        getCommand("gipguard").setTabCompleter(new TabComplete(this));
        getCommand("gipguard").setExecutor(new GIPGuardCommand(this, configUtil));

        getLogger().info(ChatColor.YELLOW + "Selected database type: " + DatabaseManager.getDatabaseType());

        String authPlugin = configUtil.getConfig().getString("hooks.auth", "").toLowerCase();
        if (authPlugin.isEmpty()) {
            getLogger().info("No authentication plugin hook is configured.");
            return;
        }

        switch (authPlugin) {
            case "authme":
                if (getServer().getPluginManager().getPlugin("AuthMe") != null) {
                    getLogger().info(ChatColor.AQUA + "Hooking into AuthMe for authentication.");
                } else {
                    getLogger().warning("AuthMe is configured but not found. Please install AuthMe or update the configuration.");
                }
                break;
            case "nlogin":
                if (getServer().getPluginManager().getPlugin("nLogin") != null) {
                    getLogger().info(ChatColor.AQUA + "Hooking into nLogin for authentication.");
                } else {
                    getLogger().warning("nLogin is configured but not found. Please install nLogin or update the configuration.");
                }
                break;
            default:
                getLogger().warning("Unsupported authentication plugin specified in the configuration.");
                break;
        }
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
