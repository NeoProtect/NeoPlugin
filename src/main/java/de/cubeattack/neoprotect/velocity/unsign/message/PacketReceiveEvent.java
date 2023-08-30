package de.cubeattack.neoprotect.velocity.unsign.message;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.MinecraftPacket;

import static java.util.Objects.requireNonNull;

public final class PacketReceiveEvent implements ResultedEvent<ResultedEvent.GenericResult> {
    private GenericResult result = GenericResult.allowed();

    private final MinecraftPacket packet;
    private final Player player;

    public PacketReceiveEvent(final MinecraftPacket packet, final Player player) {
        this.packet = packet;
        this.player = player;
    }

    @Override
    public GenericResult getResult() {
        return result;
    }

    @Override
    public void setResult(final GenericResult result) {
        this.result = requireNonNull(result);
    }

    public MinecraftPacket getPacket() {
        return this.packet;
    }

    public Player getPlayer() {
        return this.player;
    }
}
