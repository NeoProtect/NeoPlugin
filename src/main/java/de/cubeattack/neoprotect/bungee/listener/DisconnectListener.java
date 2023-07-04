package de.cubeattack.neoprotect.bungee.listener;

import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DisconnectListener implements Listener {

    private final NeoProtectBungee instance;
    public DisconnectListener(NeoProtectBungee instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event){
        instance.getCore().getPlayerInSetup().remove(event.getPlayer());
    }
}
