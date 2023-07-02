package de.cubeattack.neoprotect.bungee.proxyprotocol;

import de.cubeattack.api.utils.JavaUtils;
import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import de.cubeattack.neoprotect.core.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.logging.Level;

public class ProxyProtocol {

    private final Reflection.FieldAccessor<ChannelWrapper> channelWrapperAccessor = Reflection.getField(HandlerBoss.class, "channel", ChannelWrapper.class);
    private final ChannelInitializer<Channel> bungeeChannelInitializer = PipelineUtils.SERVER_CHILD;
    private final Reflection.MethodInvoker initChannelMethod = Reflection.getMethod(bungeeChannelInitializer.getClass(), "initChannel", Channel.class);

    public ProxyProtocol(NeoProtectBungee instance) {

        instance.getLogger().info("Proceeding with the server channel injection...");

        try {
            ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {

                    try {

                        initChannelMethod.invoke(bungeeChannelInitializer, channel);

                        if(channel.localAddress().toString().startsWith("local:"))return;

                        if (!Config.isProxyProtocol() | !instance.getCore().isSetup()) {
                            return;
                        }

                        if (!instance.getCore().getRestAPI().getNeoServerIPs().toList().
                                contains(channel.remoteAddress().toString().substring(1).split(":")[0])) {
                            channel.close();
                            return;
                        }

                        channel.pipeline().names().forEach((n) -> {
                            if (n.equals("HAProxyMessageDecoder#0"))
                                channel.pipeline().remove("HAProxyMessageDecoder#0");
                        });


                        channel.pipeline().addFirst("haproxy-decoder", new HAProxyMessageDecoder());
                        channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof HAProxyMessage) {
                                    HAProxyMessage message = (HAProxyMessage) msg;
                                    channelWrapperAccessor.get(channel.pipeline().get(HandlerBoss.class)).setRemoteAddress(new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
                                } else {
                                    super.channelRead(ctx, msg);
                                }
                            }
                        });
                    } catch (Exception ex) {
                        instance.getLogger().log(Level.SEVERE, "Cannot inject incoming channel " + channel, ex);
                    }
                }
            };

            Field serverChild = PipelineUtils.class.getField("SERVER_CHILD");
            serverChild.setAccessible(true);

            if(JavaUtils.javaVersionCheck() == 8){
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(serverChild, serverChild.getModifiers() & ~Modifier.FINAL);

                serverChild.set(PipelineUtils.class, channelInitializer);
            }else {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);

                Unsafe unsafe = (Unsafe) unsafeField.get(null);
                unsafe.putObject(unsafe.staticFieldBase(serverChild), unsafe.staticFieldOffset(serverChild), channelInitializer);
            }

            instance.getLogger().info("Found the server channel and added the handler. Injection successfully!");

        } catch (Exception ex) {
            instance.getLogger().log(Level.SEVERE, "An unknown error has occurred", ex);
        }
    }
}
