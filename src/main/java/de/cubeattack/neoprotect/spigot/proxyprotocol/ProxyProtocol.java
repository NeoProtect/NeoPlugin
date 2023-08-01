package de.cubeattack.neoprotect.spigot.proxyprotocol;

import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import io.netty.channel.*;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * Represents a very tiny alternative to ProtocolLib.
 * <p>
 * It now supports intercepting packets during login and status ping (such as OUT_SERVER_PING)!
 *
 * @author Kristian // Love ya <3 ~ytendx
 */
public class ProxyProtocol {

    // Looking up ServerConnection
    private final Class<Object> minecraftServerClass = Reflection.getUntypedClass("{nms}.MinecraftServer");
    private final Class<Object> serverConnectionClass = Reflection.getUntypedClass("{nms}" + (Reflection.isNewerPackage() ? ".network" : "") + ".ServerConnection");
    private final Reflection.FieldAccessor<Object> getMinecraftServer = Reflection.getField("{obc}.CraftServer", minecraftServerClass, 0);
    private final Reflection.FieldAccessor<Object> getServerConnection = Reflection.getField(minecraftServerClass, serverConnectionClass, 0);
    private final Reflection.MethodInvoker getNetworkMarkers = !Reflection.isNewerPackage() && !Reflection.VERSION.contains("16") ? Reflection.getTypedMethod(serverConnectionClass, null, List.class, serverConnectionClass) : null;
    private final Reflection.FieldAccessor<List> networkManagersFieldAccessor = Reflection.isNewerPackage() || Reflection.VERSION.contains("16") ? Reflection.getField(serverConnectionClass, List.class, 0) : null;
    private final Class<Object> networkManager = Reflection.getUntypedClass(Reflection.isNewerPackage() ? "net.minecraft.network.NetworkManager" : "{nms}.NetworkManager");
    private final Reflection.FieldAccessor<SocketAddress> socketAddressFieldAccessor = Reflection.getField(networkManager, SocketAddress.class, 0);

    private List<Object> networkManagers;
    private ServerChannelInitializer serverChannelHandler;
    private ChannelInitializer<Channel> beginInitProtocol;
    private ChannelInitializer<Channel> endInitProtocol;

    protected NeoProtectSpigot instance;

    /**
     * Construct a new instance of TinyProtocol, and start intercepting packets for all connected clients and future clients.
     * <p>
     * You can construct multiple instances per plugin.
     *
     * @param instance - the plugin.
     */
    public ProxyProtocol(NeoProtectSpigot instance) {
        this.instance = instance;

        try {
            instance.getCore().info("Proceeding with the server channel injection...");
            registerChannelHandler();
        } catch (IllegalArgumentException ex) {
            // Damn you, late bind
            instance.getCore().info("Delaying server channel injection due to late bind.");

            new BukkitRunnable() {
                @Override
                public void run() {
                    registerChannelHandler();
                    instance.getCore().info("Late bind injection successful.");
                }
            }.runTask(instance);
        }
    }

    private void createServerChannelHandler() {
        // Handle connected channels
        endInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {

                if (!Config.isProxyProtocol() | !instance.getCore().isSetup() | instance.getCore().getDirectConnectWhitelist().contains(((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress())) {
                    instance.getCore().debug("Plugin is not setup / ProxyProtocol is off / Player is on DirectConnectWhitelist (return)");
                    return;
                }

                if (instance.getCore().isSetup() && (instance.getCore().getRestAPI().getNeoServerIPs() == null ||
                        instance.getCore().getRestAPI().getNeoServerIPs().toList().stream().noneMatch(ipRange -> isIPInRange((String) ipRange, ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress())))) {
                    channel.close();
                    instance.getCore().debug("Player connected over IP (" + channel.remoteAddress() + ") doesn't match to Neo-IPs (warning)");
                    return;
                }

                try {
                    instance.getCore().debug("Adding Handler...");
                    synchronized (networkManagers) {
                        // Adding the decoder to the pipeline
                        channel.pipeline().addFirst("haproxy-decoder", new HAProxyMessageDecoder());
                        // Adding the proxy message handler to the pipeline too
                        channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", HAPROXY_MESSAGE_HANDLER);
                    }

                    instance.getCore().debug("Connecting finished");

                } catch (Exception ex) {
                    instance.getCore().severe("Cannot inject incoming channel " + channel, ex);
                }
            }

        };

        // This is executed before Minecraft's channel handler
        beginInitProtocol = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(endInitProtocol);
            }

        };

        serverChannelHandler = new ServerChannelInitializer();
    }

    @SuppressWarnings("unchecked")
    private void registerChannelHandler() {
        Object mcServer = getMinecraftServer.get(Bukkit.getServer());
        Object serverConnection = getServerConnection.get(mcServer);
        boolean looking = true;

        // We need to synchronize against this list
        networkManagers = Reflection.isNewerPackage() || Reflection.VERSION.contains("16") ? networkManagersFieldAccessor.get(serverConnection) : (List<Object>) getNetworkMarkers.invoke(null, serverConnection);
        createServerChannelHandler();

        // Find the correct list, or implicitly throw an exception
        for (int i = 0; looking; i++) {
            List<Object> list = Reflection.getField(serverConnection.getClass(), List.class, i).get(serverConnection);

            for (Object item : list) {
                if (!(item instanceof ChannelFuture))
                    break;

                // Channel future that contains the server connection
                Channel serverChannel = ((ChannelFuture) item).channel();


                serverChannel.pipeline().addFirst(serverChannelHandler);
                looking = false;

                this.instance.getCore().info("Found the server channel and added the handler. Injection successfully!");
            }
        }
    }

    private final HAProxyMessageHandler HAPROXY_MESSAGE_HANDLER = new HAProxyMessageHandler();

    @ChannelHandler.Sharable
    public class HAProxyMessageHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!(msg instanceof HAProxyMessage)) {
                super.channelRead(ctx, msg);
                return;
            }

            try {
                final HAProxyMessage message = (HAProxyMessage) msg;

                // Set the SocketAddress field of the NetworkManager ("packet_handler" handler) to the client address
                socketAddressFieldAccessor.set(ctx.channel().pipeline().get("packet_handler"), new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
            } catch (Exception exception) {
                // Closing the channel because we do not want people on the server with a proxy ip
                ctx.channel().close();

                // Logging for the lovely server admins :)
                instance.getCore().severe("Error: The server was unable to set the IP address from the 'HAProxyMessage'. Therefore we closed the channel.", exception);
            }
        }
    }

    @ChannelHandler.Sharable
    class ServerChannelInitializer extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Channel channel = (Channel) msg;

            instance.getCore().debug("Open channel (" + channel.remoteAddress().toString() + ")");

            if (channel.localAddress().toString().startsWith("local:") || ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress().equals(Config.getGeyserServerIP())) {
                instance.getCore().debug("Detected bedrock player (return)");
                return;
            }

            // Prepare to initialize ths channel
            channel.pipeline().addFirst(beginInitProtocol);
            ctx.fireChannelRead(msg);
        }
    }

    public static boolean isIPInRange(String ipRange, String ipAddress) {
        if(!ipRange.contains("/")){
            ipRange = ipRange + "/32";
        }

        long targetIntAddress = ipToDecimal(ipAddress);

        int range = Integer.parseInt(ipRange.split("/")[1]);
        String startIP = ipRange.split("/")[0];

        long startIntAddress = ipToDecimal(startIP);

        return targetIntAddress <= (startIntAddress + (long) Math.pow(2, (32 - range))) && targetIntAddress >= startIntAddress;
    }

    public static long ipToDecimal(String ipAddress) throws IllegalArgumentException {
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return -1;
        }

        long decimal = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(parts[i]);
            if (octet < 0 || octet > 255) {
                return -1;
            }
            decimal += (long) (octet * Math.pow(256, 3 - i));
        }

        return decimal;
    }
}
