package dev.luckynetwork.cyclize.calm;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.text.DecimalFormat;
import java.util.HashMap;

@Plugin(
        id = "calm",
        name = "Calm",
        version = BuildConstants.VERSION,
        description = "Calms players down by adding a chat cooldown",
        authors = {"Cyclize"}
)
public class Calm {
    private final ProxyServer server;
    private final HashMap<String, Long> cooldownQueue;

    @Inject
    public Calm(ProxyServer server) {
        this.server = server;
        this.cooldownQueue = new HashMap<>();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent initializeEvent) {
        server.getEventManager().register(this, PlayerChatEvent.class, chatEvent -> {
            Player player = chatEvent.getPlayer();
            String username = player.getUsername();
            long currentTime = System.currentTimeMillis();
            long liftCooldown = 0;
            long cooldown = 5;

            for (int i = 0; i <= 5; i++) {
                if (player.hasPermission("calm.cooldown." + i)) {
                    cooldown = i;
                    break;
                }
            }

            if (cooldownQueue.containsKey(username)) {
                liftCooldown = cooldownQueue.get(username);
            }

            if (liftCooldown > currentTime) {
                double intervalSeconds = (liftCooldown - currentTime) / 1000.0;
                player.sendMessage(Component.text("You can send another message in " + new DecimalFormat("#.#").format(intervalSeconds) + " seconds!", NamedTextColor.RED));
                chatEvent.setResult(PlayerChatEvent.ChatResult.denied());
                return;
            }

            cooldownQueue.put(username, currentTime + (cooldown * 1000));
        });
    }
}