package de.cubeattack.neoprotect.velocity.unsign.command;

import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.CommandHandler;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerCommand;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;
import de.cubeattack.neoprotect.velocity.unsign.message.PacketReceiveEvent;

import java.util.concurrent.CompletableFuture;

public final class KeyedCommandListener implements CommandHandler<KeyedPlayerCommand> {

    private final VelocityServer proxyServer;

    @Inject
    public KeyedCommandListener(final NeoProtectVelocity plugin) {
        this.proxyServer = (VelocityServer) plugin.getProxy();
        proxyServer.getEventManager().register(plugin, PacketReceiveEvent.class, this::onCommand);
    }

    public void onCommand(final PacketReceiveEvent event) {
        if (!(event.getPacket() instanceof KeyedPlayerCommand)) {
            return;
        }

        KeyedPlayerCommand packet = (KeyedPlayerCommand) event.getPacket();

        final ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        if (checkConnectionFailed(player)) return;

        event.setResult(ResultedEvent.GenericResult.denied());
        final String commandExecuted = packet.getCommand();

        queueCommandResult(proxyServer, player, commandEvent -> {
            final CommandExecuteEvent.CommandResult result = commandEvent.getResult();
            if (result == CommandExecuteEvent.CommandResult.denied()) {
                return CompletableFuture.completedFuture(null);
            }

            final String commandToRun = result.getCommand().orElse(commandExecuted);
            if (result.isForwardToServer()) {
                ChatBuilderV2 write = player.getChatBuilderFactory()
                        .builder()
                        .setTimestamp(packet.getTimestamp())
                        .asPlayer(player);

                if (commandToRun.equals(commandExecuted)) {
                    return CompletableFuture.completedFuture(packet);
                } else {
                    write.message("/" + commandToRun);
                }
                return CompletableFuture.completedFuture(write.toServer());
            }

            return runCommand(proxyServer, player, commandToRun, hasRun -> {
                if (hasRun) return null;

                if (commandToRun.equals(packet.getCommand())) {
                    return packet;
                }

                return player.getChatBuilderFactory()
                        .builder()
                        .setTimestamp(packet.getTimestamp())
                        .asPlayer(player)
                        .message("/" + commandToRun)
                        .toServer();
            });
        }, packet.getCommand(), packet.getTimestamp());
    }

    public Class<KeyedPlayerCommand> packetClass() {
        return KeyedPlayerCommand.class;
    }

    @Override
    public void handlePlayerCommandInternal(KeyedPlayerCommand keyedPlayerCommand) {

    }

    public static boolean checkConnectionFailed(final ConnectedPlayer player) {
        try {
            player.ensureAndGetCurrentServer().ensureConnected();
            return false;
        } catch (final IllegalStateException e) {
            return true;
        }
    }
}