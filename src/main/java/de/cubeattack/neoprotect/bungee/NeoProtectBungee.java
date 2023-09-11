package de.cubeattack.neoprotect.bungee;

import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.model.Stats;
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
    public Stats getStats() {
        return new Stats(
                getPluginType(),
                getProxy().getVersion(),
                getProxy().getName(),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                getDescription().getVersion(),
                getCore().getVersionResult().getVersionStatus().toString(),
                getCore().getVersionResult().getError(),
                Config.getAutoUpdaterSettings().toString(),
                getCore().isSetup() ? getCore().getRestAPI().getPlan() : "§cNOT CONNECTED",
                getProxy().getOnlineCount(),
                getProxy().getServers().size(),
                Runtime.getRuntime().availableProcessors(),
                getProxy().getConfig().isOnlineMode(),
                Config.isProxyProtocol()
        );
    }

    @Override
    public String getServerAddress() {
        return getProxy().getConfig().getListeners().stream().findFirst().orElseThrow(null).getSocketAddress().toString();
    }

    @Override
    public void sendMessage(Object receiver, String text) {
        sendMessage(receiver, text, null, null, null, null);
    }

    @Override
    public void sendMessage(Object receiver, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = new TextComponent(core.getPrefix() + text);

        if (clickAction != null)
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if (hoverAction != null)
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(hoverAction), Collections.singletonList(new Text(hoverMsg))));
        if (receiver instanceof CommandSender) ((CommandSender) receiver).sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getProxy().getPlayers().forEach(receiver -> {
            if (receiver.hasPermission("neoprotect.admin") || receiver.hasPermission("neoprotect.notify"))
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
    public ArrayList<String> getPlugins() {
        ArrayList<String> plugins = new ArrayList<>();
        getProxy().getPluginManager().getPlugins().forEach(p -> plugins.add(p.getDescription().getName()));
        return plugins;
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.BUNGEECORD;
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }
}
