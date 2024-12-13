package hu.kxtsoo.ipguard.listeners;

import hu.kxtsoo.ipguard.IPGuard;
import hu.kxtsoo.ipguard.database.DatabaseManager;
import hu.kxtsoo.ipguard.hooks.HookManager;
import hu.kxtsoo.ipguard.manager.SchedulerManager;
import hu.kxtsoo.ipguard.util.ConfigUtil;
import okhttp3.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class PlayerJoinListener implements Listener {

    private final IPGuard plugin;
    private final ConfigUtil configUtil;
    private final HookManager hookManager;

    public PlayerJoinListener(IPGuard plugin, ConfigUtil configUtil, HookManager hookManager) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.hookManager = hookManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        String playerName = event.getPlayer().getName();
        String playerCurrentIP = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().getHostAddress();

        SchedulerManager.runAsync(() -> {
            try {
                String playerIP = DatabaseManager.getPlayerIP(uuid);

                if (!"N/A".equals(playerIP) && !playerCurrentIP.equals(playerIP)) {
                    if (configUtil.getConfig().getBoolean("discord.enabled")) {
                        sendWebhook(playerName, playerCurrentIP);
                    }

                    String kickReason = configUtil.getMessage("messages.player-kick");
                    if (kickReason.isEmpty()) {
                        kickReason = "This user account is IP address protected!";
                    }

                    String finalKickReason = kickReason;
                    SchedulerManager.run(() -> event.getPlayer().kickPlayer(finalKickReason));
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to check player IP from the database: " + e.getMessage());
            }
        });
    }

    private void sendWebhook(String playerName, String playerCurrentIP) {
        String webhookUrl = configUtil.getConfig().getString("discord.webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        List<String> previousUsers = hookManager.getAuthHook() != null
                ? hookManager.getAuthHook().getPlayersByIP(playerCurrentIP)
                : List.of("Unknown plugin configuration");

        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        String usersListDescription = previousUsers.isEmpty() ? "Please configure 'hooks.auth' in the config.yml to display users." : String.join(", ", previousUsers);
        String descriptionTemplate = configUtil.getConfig().getString("discord.embed.description", "**The user details:**\n> Username: **%player%** \n> IP-Address: **%ip_address%** \n> Exact Date: **%date%**\n> Previous users on this IP: **%previous_users%**");
        String description = descriptionTemplate.replace("%player%", playerName)
                .replace("%ip_address%", playerCurrentIP)
                .replace("%date%", date)
                .replace("%previous_users%", usersListDescription);
        int color = Integer.decode(configUtil.getConfig().getString("discord.embed.color", "#36393F").replace("#", "0x"));

        JSONObject embed = new JSONObject();
        embed.put("title", configUtil.getConfig().getString("discord.embed.title", ":warning: A player tried to access a protected account."));
        embed.put("description", description);
        embed.put("color", color);
        embed.put("footer", new JSONObject().put("text", plugin.getConfig().getString("discord.embed.footer", "MonGen's Cave - mc-IPGuard Logs")));
        embed.put("timestamp", Instant.now().toString());

        JSONArray embeds = new JSONArray();
        embeds.add(embed);

        JSONObject payload = new JSONObject();
        payload.put("embeds", embeds);

        RequestBody body = RequestBody.create(payload.toJSONString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(webhookUrl).post(body).build();

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 429) {
                    long retryAfter = Long.parseLong(Objects.requireNonNull(response.header("Retry-After", "60")));
                    plugin.getLogger().info("Discord Rate Limit reached! Try again after " + retryAfter + " seconds.");
                } else if (!response.isSuccessful()) {
                    plugin.getLogger().warning("Failed to send Discord webhook: " + response);
                } else {
                    plugin.getLogger().info("Discord webhook sent successfully.");
                }
                response.close();
            }
        });
    }
}