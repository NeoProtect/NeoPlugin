package de.cubeattack.neoprotect.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

public class DisconnectListener {

    @Subscribe
    public void onDisconnect(DisconnectEvent event){
        ChatListener.PLAYER_IN_SETUP.remove(event.getPlayer());
    }
}
