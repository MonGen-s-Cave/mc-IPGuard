package hu.kxtsoo.ipguard.hooks;

import hu.kxtsoo.ipguard.IPGuard;
import hu.kxtsoo.ipguard.hooks.impl.AuthMeHook;
import hu.kxtsoo.ipguard.hooks.impl.nLoginHook;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import org.bukkit.Bukkit;

public class HookManager {

    private final IPGuard plugin;
    private final ConfigUtil configUtil;
    private AuthHook authHook;

    public HookManager(IPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    public void registerHooks() {
        String selectedAuthPlugin = configUtil.getConfig().getString("hooks.settings.auth-plugin", "").toLowerCase();

        if (selectedAuthPlugin.equals("authme") &&
                configUtil.getHooks().getBoolean("hooks.register.AuthMe", true) &&
                Bukkit.getPluginManager().getPlugin("AuthMe") != null &&
                Bukkit.getPluginManager().getPlugin("AuthMe").isEnabled()) {

            authHook = new AuthMeHook();
            plugin.getLogger().info("\u001B[32m[Hook] AuthMe successfully enabled.\u001B[0m");

        } else if (selectedAuthPlugin.equals("nlogin") &&
                configUtil.getHooks().getBoolean("hooks.register.nLogin", true) &&
                Bukkit.getPluginManager().getPlugin("nLogin") != null &&
                Bukkit.getPluginManager().getPlugin("nLogin").isEnabled()) {

            authHook = new nLoginHook();
            plugin.getLogger().info("\u001B[32m[Hook] nLogin successfully enabled.\u001B[0m");

        } else {
            plugin.getLogger().warning("No valid authentication plugin selected or the selected plugin is not enabled.");
        }
    }

    public AuthHook getAuthHook() {
        return authHook;
    }
}