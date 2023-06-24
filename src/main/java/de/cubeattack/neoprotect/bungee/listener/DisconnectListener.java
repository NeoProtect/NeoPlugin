package de.cubeattack.neoprotect.bungee.listener;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DisconnectListener implements Listener {

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event){
        ChatListener.PLAYER_IN_SETUP.remove(event.getPlayer());
    }
}
