package de.cubeattack.neoprotect.bungee;

import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.Permission;
import de.cubeattack.neoprotect.core.model.debugtool.KeepAliveResponseKey;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.packet.KeepAlive;
import org.bstats.bungeecord.Metrics;

import java.util.ArrayList;
import java.util.Collections;

public final class NeoProtectBungee extends Plugin implements NeoProtectPlugin {

    private static Core core;

    @Override
    public void onLoad() {
        new Metrics(this, 18726);
    }

    @Override
    public void onEnable() {
        core = new Core(this);
        new Startup(this);
    }

    public Core getCore() {
        return core;
    }

    @Override
    public void sendMessage(Object receiver, String text) {
        sendMessage(receiver, text, null, null, null, null);
    }

    @Override
    public void sendMessage(Object receiver, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = new TextComponent(core.getPrefix() + text);

        if (clickAction != null) msg.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if (hoverAction != null)
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(hoverAction), Collections.singletonList(new Text(hoverMsg))));
        if (receiver instanceof CommandSender) ((CommandSender) receiver).sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getProxy().getPlayers().forEach(receiver -> {
            if (receiver.hasPermission("neoprotect.admin") || receiver.hasPermission(permission.value))
                sendMessage(receiver, text, clickAction, clickMsg, hoverAction, hoverMsg);
        });
    }

    @Override
    public void sendKeepAliveMessage(Object receiver, long id) {
        if (receiver instanceof ProxiedPlayer) {
            ((ProxiedPlayer) receiver).unsafe().sendPacket(new KeepAlive(id));
            getCore().getPingMap().put(new KeepAliveResponseKey(((ProxiedPlayer) receiver).getSocketAddress(), id), System.currentTimeMillis());
        }
    }

    @Override
    public long sendKeepAliveMessage(long id) {
        for (ProxiedPlayer player : this.getProxy().getPlayers()) {
            sendKeepAliveMessage(player, id);
        }
        return id;
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public String getPluginFile() {
        return getFile().getAbsolutePath();
    }

    @Override
    public String getProxyName() {
        return getProxy().getName();
    }

    @Override
    public String getProxyVersion() {
        return getProxy().getVersion();
    }

    @Override
    public ArrayList<String> getProxyPlugins() {
        ArrayList<String> plugins = new ArrayList<>();
        getProxy().getPluginManager().getPlugins().forEach(p -> plugins.add(p.getDescription().getName()));
        return plugins;
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.BUNGEECORD;
    }
}
