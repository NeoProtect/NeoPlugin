package de.cubeattack.neoprotect.spigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        ChatListener.PLAYER_IN_SETUP.remove(event.getPlayer());
    }
}
