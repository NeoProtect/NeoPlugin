package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {

    private final NeoProtectSpigot instance;

    public DisconnectListener(NeoProtectSpigot instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        instance.getCore().getPlayerInSetup().remove(event.getPlayer());
    }
}
