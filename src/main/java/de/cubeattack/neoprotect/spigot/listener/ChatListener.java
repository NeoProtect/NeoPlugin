package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final NeoProtectSpigot instance;

    public ChatListener(NeoProtectSpigot instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("neoprotect.admin") || !instance.getCore().getPlayerInSetup().contains(player))
            return;

        event.setCancelled(true);

        new NeoProtectExecutor.ExecutorBuilder()
                .local(player.locale())
                .neoProtectPlugin(instance)
                .sender(event.getPlayer())
                .msg(event.getMessage())
                .executeChatEvent();
    }
}
