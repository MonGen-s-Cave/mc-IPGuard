package hu.kitsoo.gipguard.events;

import com.nickuc.login.api.nLoginAPI;
import com.nickuc.login.api.types.AccountData;
import fr.xephi.authme.api.v3.AuthMeApi;
import hu.kitsoo.gipguard.GIPGuard;
import hu.kitsoo.gipguard.database.DatabaseManager;
import hu.kitsoo.gipguard.util.ChatUtil;
import hu.kitsoo.gipguard.util.ConfigUtil;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JoinEvent implements Listener {

    private final GIPGuard plugin;
    private final ConfigUtil configUtil;

    public JoinEvent(GIPGuard plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String playerName = event.getPlayer().getName();
                String playerCurrentIP = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().getHostAddress();

                try {
                    String playerIP = DatabaseManager.getPlayerIP(playerName);
                    if (!playerIP.equals("N/A") && !playerCurrentIP.equals(playerIP)) {

                        if (configUtil.getConfig().getBoolean("discord.enabled")) {
                            sendWebhook(playerName, playerCurrentIP);
                        }

                        List<String> kickReasons = configUtil.getMessages().getStringList("messages.player-kick");
                        if (kickReasons.isEmpty()) {
                            kickReasons = List.of("This user account is IP address protected!");
                        }

                        String kickReason = String.join("\n", kickReasons.stream().map(ChatUtil::colorizeHex).toList());
                        Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().kickPlayer(kickReason));
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to check player IP from the database: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void sendWebhook(String playerName, String playerCurrentIP) {
        String webhookUrl = configUtil.getConfig().getString("discord.webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        String configuredAuthPlugin = configUtil.getConfig().getString("hooks.auth", "").toLowerCase();
        List<String> previousUsers = new ArrayList<>();

        try {
            if (configuredAuthPlugin.equals("authme")) {
                AuthMeApi authMeApi = AuthMeApi.getInstance();
                previousUsers = authMeApi.getNamesByIp(playerCurrentIP);
            } else if (configuredAuthPlugin.equals("nlogin")) {
                List<AccountData> accounts = nLoginAPI.getApi().getAccountsByIp(playerCurrentIP);
                previousUsers = accounts.stream().map(AccountData::getLastName).toList();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to retrieve account data: " + e.getMessage());
            return;
        }

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
        embed.put("footer", new JSONObject().put("text", plugin.getConfig().getString("discord.embed.footer", "Glowing Studios - gIPGuard Logs")));
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