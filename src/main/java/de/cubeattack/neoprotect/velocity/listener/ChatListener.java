package de.cubeattack.neoprotect.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.neoprotect.core.command.NeoProtectExecutor;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

public class ChatListener {

    private final NeoProtectVelocity instance;

    public ChatListener(NeoProtectVelocity instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event){

        Player player = event.getPlayer();

        if (!player.hasPermission("neoprotect.admin") || !instance.getCore().getPLAYER_IN_SETUP().contains(player)) return;

        event.setResult(PlayerChatEvent.ChatResult.denied());

        new NeoProtectExecutor.ExecutorBuilder()
                .neoProtectPlugin(instance)
                .sender(event.getPlayer())
                .msg(event.getMessage())
                .executeChatEvent();
    }
}
