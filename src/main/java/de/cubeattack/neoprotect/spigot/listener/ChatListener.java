package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.api.util.JavaUtils;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Locale;

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
                .local(JavaUtils.javaVersionCheck() != 8 ? Locale.forLanguageTag(player.getLocale()) : Locale.ENGLISH)
                .neoProtectPlugin(instance)
                .sender(event.getPlayer())
                .msg(event.getMessage())
                .executeChatEvent();
    }
}
