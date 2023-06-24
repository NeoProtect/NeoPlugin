package de.cubeattack.neoprotect.velocity.messageunsign;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChat;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

public final class SessionChatListener {

    private final NeoProtectVelocity plugin;

    public SessionChatListener(NeoProtectVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onChat(PacketReceiveEvent event) {

        if (!(event.getPacket() instanceof SessionPlayerChat)) {
            return;
        }

        SessionPlayerChat chatPacket = (SessionPlayerChat) event.getPacket();
        ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        String chatMessage = chatPacket.getMessage();

        if (!checkConnection(player)) return;

        event.setResult(ResultedEvent.GenericResult.denied());

        player.getChatQueue().queuePacket(
                plugin.getProxy().getEventManager().fire(new PlayerChatEvent(player, chatMessage))
                        .thenApply(PlayerChatEvent::getResult)
                        .thenApply(result -> {
                            if (!result.isAllowed()) {
                                return null;
                            }

                            final boolean isModified = result
                                    .getMessage()
                                    .map(str -> !str.equals(chatMessage))
                                    .orElse(false);

                            if (isModified) {
                                return player.getChatBuilderFactory()
                                        .builder()
                                        .message(result.getMessage().get())
                                        .setTimestamp(chatPacket.getTimestamp())
                                        .toServer();
                            }
                            return chatPacket;
                        }),
                chatPacket.getTimestamp()
        );
    }

    public boolean checkConnection(final ConnectedPlayer player) {
        try {
            player.ensureAndGetCurrentServer().ensureConnected();
            return true;
        } catch(final IllegalStateException e) {
            return false;
        }
    }
}