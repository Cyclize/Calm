package dev.luckynetwork.cyclize.calm;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
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

    @Inject
    public Calm(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent initializeEvent) {
        server.getEventManager().register(this, new SpamListener());
    }
}

class SpamListener {

    private final HashMap<String, Long> chatCooldown;
    private final HashMap<String, Long> commandCooldown;

    public SpamListener() {
        this.chatCooldown = new HashMap<>();
        this.commandCooldown = new HashMap<>();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String username = player.getUsername();
        long currentTime = System.currentTimeMillis();
        long liftCooldown = 0;
        long cooldown = 50;

        for (int i = 0; i <= 100; i++) {
            if (player.hasPermission("calm.chat." + i)) {
                cooldown = i;
                break;
            }
        }

        if (chatCooldown.containsKey(username)) {
            liftCooldown = chatCooldown.get(username);
        }

        if (liftCooldown > currentTime) {
            double intervalSeconds = (liftCooldown - currentTime) / 1000.0;
            player.sendMessage(Component.text("You can send another message in " + new DecimalFormat("#.#").format(intervalSeconds) + " seconds!", NamedTextColor.RED));
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        chatCooldown.put(username, currentTime + (cooldown * 100));
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player)) return;

        Player player = (Player) event.getCommandSource();
        String username = player.getUsername();
        long currentTime = System.currentTimeMillis();
        long liftCooldown = 0;
        long cooldown = 50;

        for (int i = 0; i <= 100; i++) {
            if (player.hasPermission("calm.command." + i)) {
                cooldown = i;
                break;
            }
        }

        if (commandCooldown.containsKey(username)) {
            liftCooldown = commandCooldown.get(username);
        }

        if (liftCooldown > currentTime) {
            double intervalSeconds = (liftCooldown - currentTime) / 1000.0;
            player.sendMessage(Component.text("You can execute another command in " + new DecimalFormat("#.#").format(intervalSeconds) + " seconds!", NamedTextColor.RED));
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        commandCooldown.put(username, currentTime + (cooldown * 100));
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        String username = event.getPlayer().getUsername();
        chatCooldown.remove(username);
        commandCooldown.remove(username);
    }
}
