package de.cubeattack.neoprotect.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

public class DisconnectListener {

    private final NeoProtectVelocity instance;

    public DisconnectListener(NeoProtectVelocity instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event){
        instance.getCore().getPlayerInSetup().remove(event.getPlayer());
    }
}
