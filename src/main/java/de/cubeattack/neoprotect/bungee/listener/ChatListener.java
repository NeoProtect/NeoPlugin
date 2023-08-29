package de.cubeattack.neoprotect.bungee.listener;

import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    private final NeoProtectBungee instance;

    public ChatListener(NeoProtectBungee instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        CommandSender sender = (CommandSender) event.getSender();

        if ((!sender.hasPermission("neoprotect.admin") && !instance.getCore().isPlayerMaintainer((((ProxiedPlayer) sender).getUniqueId()), instance.getProxy().getConfig().isOnlineMode())) || !instance.getCore().getPlayerInSetup().contains(sender) || event.isCommand())
            return;

        event.setCancelled(true);

        new NeoProtectExecutor.ExecutorBuilder()
                .local(((ProxiedPlayer) sender).getLocale())
                .neoProtectPlugin(instance)
                .sender(event.getSender())
                .msg(event.getMessage())
                .executeChatEvent();
    }
}
