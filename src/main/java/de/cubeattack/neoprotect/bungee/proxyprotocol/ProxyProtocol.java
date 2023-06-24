package de.cubeattack.neoprotect.bungee.proxyprotocol;

import de.cubeattack.api.utils.JavaUtils;
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

public class ProxyProtocol {

    private final Reflection.FieldAccessor<ChannelWrapper> channelWrapperAccessor = Reflection.getField(HandlerBoss.class, "channel", ChannelWrapper.class);
    private final ChannelInitializer<Channel> bungeeChannelInitializer = PipelineUtils.SERVER_CHILD;
    private final Reflection.MethodInvoker initChannelMethod = Reflection.getMethod(bungeeChannelInitializer.getClass(), "initChannel", Channel.class);

    public ProxyProtocol() {
        try {
            ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {

                    initChannelMethod.invoke(bungeeChannelInitializer, channel);

                    channel.pipeline().names().forEach((n) ->{
                        if(n.equals("HAProxyMessageDecoder#0"))
                            channel.pipeline().remove("HAProxyMessageDecoder#0");
                    });

                    channel.pipeline().addFirst("haproxy-decoder", new HAProxyMessageDecoder());
                    channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            if (msg instanceof HAProxyMessage) {
                                HAProxyMessage message = (HAProxyMessage) msg;
                                channelWrapperAccessor.get(channel.pipeline().get(HandlerBoss.class)).setRemoteAddress( new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
                            } else {
                                super.channelRead(ctx, msg);
                            }
                        }
                    });
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
