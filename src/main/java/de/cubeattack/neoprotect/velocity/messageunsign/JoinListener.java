package de.cubeattack.neoprotect.velocity.messageunsign;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;
import io.netty.channel.Channel;

public class JoinListener {

    NeoProtectVelocity neoProtectVelocity;

    public JoinListener(NeoProtectVelocity neoProtectVelocity) {
        this.neoProtectVelocity = neoProtectVelocity;
    }

    @Subscribe
    void onJoin(PostLoginEvent event, Continuation continuation) {
        injectPlayer(event.getPlayer());
        continuation.resume();
    }

    @Subscribe(order = PostOrder.LAST)
    EventTask onDisconnect(DisconnectEvent event) {
        if (event.getLoginStatus() == DisconnectEvent.LoginStatus.CONFLICTING_LOGIN)
            return null;
        return EventTask.async(() -> removePlayer(event.getPlayer()));
    }

    private void injectPlayer(Player player) {
        ConnectedPlayer p = (ConnectedPlayer) player;
        p.getConnection()
                .getChannel()
                .pipeline()
                .addBefore("handler", "packetevents", new PlayerChannelHandler(player, neoProtectVelocity.getProxy().getEventManager(), neoProtectVelocity.getLogger()));
    }

    private void removePlayer(Player player) {
        ConnectedPlayer p = (ConnectedPlayer) player;
        Channel channel = p.getConnection().getChannel();
        channel.eventLoop().submit(() -> channel.pipeline().remove("packetevents"));
    }
}
