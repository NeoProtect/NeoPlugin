package de.cubeattack.neoprotect.velocity.proxyprotocol;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.network.ConnectionManager;
import com.velocitypowered.proxy.network.Connections;
import com.velocitypowered.proxy.network.ServerChannelInitializerHolder;
import com.velocitypowered.proxy.protocol.packet.KeepAlive;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.model.debugtool.DebugPingResponse;
import de.cubeattack.neoprotect.core.model.debugtool.KeepAliveResponseKey;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.epoll.EpollTcpInfo;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class ProxyProtocol {

    private final Reflection.MethodInvoker initChannelMethod = Reflection.getMethod(ChannelInitializer.class, "initChannel", Channel.class);

    public ProxyProtocol(NeoProtectVelocity instance) {

        instance.getLogger().info("Proceeding with the server channel injection...");

        try {

            VelocityServer velocityServer = (VelocityServer)instance.getProxy();
            Reflection.FieldAccessor<ConnectionManager> connectionManagerFieldAccessor = Reflection.getField(VelocityServer.class, ConnectionManager.class, 0);
            ConnectionManager connectionManager = connectionManagerFieldAccessor.get(velocityServer);
            ChannelInitializer<?> oldInitializer = connectionManager.getServerChannelInitializer().get();

            ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel)  {

                    try {

                        instance.getCore().debug("Open channel (" + channel.remoteAddress().toString() + ")");

                        AtomicReference<InetSocketAddress> inetAddress = new AtomicReference<>();

                        initChannelMethod.getMethod().setAccessible(true);
                        initChannelMethod.invoke(oldInitializer, channel);

                        if(channel.localAddress().toString().startsWith("local:")){
                            instance.getCore().debug("Detected bedrock player (return)");
                            return;
                        }

                        if (!Config.isProxyProtocol() | !instance.getCore().isSetup()) {
                            instance.getCore().debug("Plugin is not setup / ProxyProtocol is off (return)");
                            return;
                        }

                        if (instance.getCore().getRestAPI().getNeoServerIPs() == null || !instance.getCore().getRestAPI().getNeoServerIPs().toList().
                                contains(((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress())) {
                            channel.close();
                            instance.getCore().debug("Close connection IP (" + channel.remoteAddress() + ") doesn't match to Neo-IPs (close / return)");
                            return;
                        }

                        channel.pipeline().names().forEach((n) -> {
                            if (n.equals("HAProxyMessageDecoder#0"))
                                channel.pipeline().remove("HAProxyMessageDecoder#0");
                            if (n.equals("ProxyProtocol$1#0"))
                                channel.pipeline().remove("ProxyProtocol$1#0");
                        });

                        channel.pipeline().addFirst("haproxy-decoder", new HAProxyMessageDecoder());
                        channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof HAProxyMessage) {
                                    HAProxyMessage message = (HAProxyMessage) msg;
                                    Reflection.FieldAccessor<SocketAddress> fieldAccessor = Reflection.getField(MinecraftConnection.class, SocketAddress.class, 0);
                                    inetAddress.set(new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
                                    fieldAccessor.set(channel.pipeline().get(Connections.HANDLER), inetAddress.get());
                                } else {
                                    super.channelRead(ctx, msg);
                                }
                            }
                        });

                        channel.pipeline().addAfter("minecraft-decoder", "neo-keep-alive-handler", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                super.channelRead(ctx, msg);

                                if (!(msg instanceof KeepAlive)) {
                                    return;
                                }

                                KeepAlive keepAlive = (KeepAlive) msg;
                                ConcurrentHashMap<KeepAliveResponseKey, Long> pingMap =  instance.getCore().getPingMap();

                                instance.getCore().debug("Received KeepAlivePackets (" + keepAlive.getRandomId() + ")");

                                for (KeepAliveResponseKey keepAliveResponseKey : pingMap.keySet()) {

                                    if (!keepAliveResponseKey.getAddress().equals(inetAddress.get()) || !(keepAliveResponseKey.getId() == keepAlive.getRandomId())) {
                                        continue;
                                    }

                                    instance.getCore().debug("KeepAlivePackets matched to DebugKeepAlivePacket");

                                    for (Player player : velocityServer.getAllPlayers()) {

                                        if (!(player).getRemoteAddress().equals(inetAddress.get())) {
                                            continue;
                                        }

                                        instance.getCore().debug("Player matched to DebugKeepAlivePacket (loading data...)");

                                        EpollTcpInfo tcpInfo = ((EpollSocketChannel) channel).tcpInfo();
                                        EpollTcpInfo tcpInfoBackend = ((EpollSocketChannel) ((ConnectedPlayer)player).getConnection().getChannel()).tcpInfo();

                                        long ping = System.currentTimeMillis() - pingMap.get(keepAliveResponseKey);
                                        long neoRTT = 0;
                                        long backendRTT = 0;

                                        if (tcpInfo != null) {
                                            neoRTT = tcpInfo.rtt() / 1000;
                                        }
                                        if (tcpInfoBackend != null) {
                                            backendRTT = tcpInfoBackend.rtt() / 1000;
                                        }

                                        ConcurrentHashMap<String, ArrayList<DebugPingResponse>> map = instance.getCore().getDebugPingResponses();

                                        if(!map.containsKey(player.getUsername())) {
                                            instance.getCore().getDebugPingResponses().put(player.getUsername(), new ArrayList<>());
                                        }

                                        map.get(player.getUsername()).add(new DebugPingResponse(ping, neoRTT, backendRTT, inetAddress.get(), channel.remoteAddress()));

                                        instance.getCore().debug("Loading completed");
                                        instance.getCore().debug(" ");

                                    }
                                    pingMap.remove(keepAliveResponseKey);
                                }
                            }
                        });

                        instance.getCore().debug("Connecting finished");

                    } catch (Exception ex) {
                        instance.getLogger().log(Level.SEVERE, "Cannot inject incoming channel " + channel, ex);
                    }
                }
            };

            ServerChannelInitializerHolder newChannelHolder = (ServerChannelInitializerHolder) Reflection.getConstructor(ServerChannelInitializerHolder.class, ChannelInitializer.class).invoke(channelInitializer);
            Reflection.FieldAccessor<ServerChannelInitializerHolder> serverChannelInitializerHolderFieldAccessor = Reflection.getField(ConnectionManager.class, ServerChannelInitializerHolder.class, 0);
            Field channelInitializerHolderField = serverChannelInitializerHolderFieldAccessor.getField();

            channelInitializerHolderField.setAccessible(true);
            channelInitializerHolderField.set(connectionManager, newChannelHolder);

            instance.getLogger().info("Found the server channel and added the handler. Injection successfully!");

        } catch (Exception ex) {
            instance.getLogger().log(Level.SEVERE, "An unknown error has occurred", ex);
        }
    }
}
