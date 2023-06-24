package de.cubeattack.neoprotect.velocity.messageunsign;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerChannelHandler extends ChannelDuplexHandler {
    private final Player player;

    private final EventManager eventManager;

    private final Logger logger;

    public PlayerChannelHandler(Player player, EventManager eventManager, Logger logger) {
        this.player = player;
        this.eventManager = eventManager;
        this.logger = logger;
    }

    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object packet) throws Exception {
        MinecraftPacket minecraftPacket;
        if (!(packet instanceof MinecraftPacket)) {
            super.channelRead(ctx, packet);
            return;
        }

        minecraftPacket = (MinecraftPacket)packet;

        boolean allowed = this.eventManager.fire(new PacketReceiveEvent(minecraftPacket, this.player)).handle((event, ex) -> {
            if (ex != null) {
                this.logger.log(Level.SEVERE,"An error has occurred while reading packet " + packet, ex);
                return Boolean.FALSE;
            }
            return event.getResult().isAllowed();
        }).join();
        if (allowed)
            super.channelRead(ctx, packet);
    }
}

