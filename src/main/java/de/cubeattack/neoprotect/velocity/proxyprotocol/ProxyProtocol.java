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

    private final Reflection.FieldAccessor<ConnectionManager> connectionManagerFieldAccessor = Reflection.getField(VelocityServer.class, ConnectionManager.class, 0);
    private final Reflection.FieldAccessor<ServerChannelInitializerHolder> serverChannelInitializerHolderFieldAccessor = Reflection.getField(ConnectionManager.class, ServerChannelInitializerHolder.class, 0);

    public ProxyProtocol(NeoProtectVelocity instance) {

        instance.getLogger().info("Proceeding with the server channel injection...");

        try {

            VelocityServer velocityServer = (VelocityServer)instance.getProxy();
            ConnectionManager connectionManager = connectionManagerFieldAccessor.get(velocityServer);
            ChannelInitializer<?> oldInitializer = connectionManager.getServerChannelInitializer().get();

            ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel)  {

                    try {

                        AtomicReference<InetSocketAddress> inetAddress = new AtomicReference<>();

                        initChannelMethod.getMethod().setAccessible(true);
                        initChannelMethod.invoke(oldInitializer, channel);

                        if(channel.localAddress().toString().startsWith("local:"))return;

                        if (!Config.isProxyProtocol() | !instance.getCore().isSetup()) {
                            return;
                        }

                        if (instance.getCore().getRestAPI().getNeoServerIPs() == null || !instance.getCore().getRestAPI().getNeoServerIPs().toList().
                                contains(((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress())) {
                            channel.close();
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

                                for (KeepAliveResponseKey keepAliveResponseKey : pingMap.keySet()) {

                                    if (!keepAliveResponseKey.getAddress().equals(inetAddress.get()) || !(keepAliveResponseKey.getId() == keepAlive.getRandomId())) {
                                        continue;
                                    }

                                    for (Player player : velocityServer.getAllPlayers()) {

                                        if (!(player).getRemoteAddress().equals(inetAddress.get())) {
                                            continue;
                                        }

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

                                        map.get(player.getUsername()).add(new DebugPingResponse(ping, neoRTT, backendRTT));

                                    }
                                    instance.getCore().getPingMap().remove(keepAliveResponseKey);
                                }
                            }
                        });
                    } catch (Exception ex) {
                        instance.getLogger().log(Level.SEVERE, "Cannot inject incoming channel " + channel, ex);
                    }
                }
            };

            ServerChannelInitializerHolder newChannelHolder = (ServerChannelInitializerHolder) Reflection.getConstructor(ServerChannelInitializerHolder.class, ChannelInitializer.class).invoke(channelInitializer);
            Field channelInitializerHolderField = serverChannelInitializerHolderFieldAccessor.getField();

            channelInitializerHolderField.setAccessible(true);
            channelInitializerHolderField.set(connectionManager, newChannelHolder);

            instance.getLogger().info("Found the server channel and added the handler. Injection successfully!");

        } catch (Exception ex) {
            instance.getLogger().log(Level.SEVERE, "An unknown error has occurred", ex);
        }
    }
}
