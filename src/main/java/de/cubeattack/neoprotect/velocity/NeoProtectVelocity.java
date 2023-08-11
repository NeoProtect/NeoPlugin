package de.cubeattack.neoprotect.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.KeepAlive;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.Permission;
import de.cubeattack.neoprotect.core.model.Stats;
import de.cubeattack.neoprotect.core.model.debugtool.KeepAliveResponseKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class NeoProtectVelocity implements NeoProtectPlugin {

    private final Metrics.Factory metricsFactory;
    private final Logger logger;
    private final ProxyServer proxy;
    private Core core;

    @Inject
    public NeoProtectVelocity(ProxyServer proxy, Logger logger, Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        Metrics metrics = metricsFactory.make(this, 18727);
        metrics.addCustomChart(new SimplePie("language", Config::getLanguage));
        core = new Core(this);
        new Startup(this);
    }

    public Core getCore() {
        return core;
    }

    @Override
    public Stats getStats() {
        return new Stats(proxy.getPlayerCount());
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    @Override
    public void sendMessage(Object receiver, String text) {
        sendMessage(receiver, text, null, null, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendMessage(Object receiver, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = Component.text(core.getPrefix() + text);

        if (clickAction != null)
            msg = msg.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if (hoverAction != null)
            msg = msg.hoverEvent(HoverEvent.hoverEvent((HoverEvent.Action<Object>) Objects.requireNonNull(HoverEvent.Action.NAMES.value(hoverAction.toLowerCase())),
                    Component.text(hoverMsg)));

        if (receiver instanceof CommandSource) ((CommandSource) receiver).sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getProxy().getAllPlayers().forEach(receiver -> {
            if (receiver.hasPermission("neoprotect.admin") || receiver.hasPermission(permission.value))
                sendMessage(receiver, text, clickAction, clickMsg, hoverAction, hoverMsg);
        });
    }

    @Override
    public void sendKeepAliveMessage(Object receiver, long id) {
        if (receiver instanceof Player) {
            KeepAlive keepAlive = new KeepAlive();
            keepAlive.setRandomId(id);
            ((ConnectedPlayer) receiver).getConnection().getChannel().writeAndFlush(keepAlive);
            getCore().getPingMap().put(new KeepAliveResponseKey(((Player) receiver).getRemoteAddress(), id), System.currentTimeMillis());
        }
    }

    @Override
    public long sendKeepAliveMessage(long id) {
        for (Player player : this.proxy.getAllPlayers()) {
            sendKeepAliveMessage(player, id);
        }
        return id;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getVersion() {
        return proxy.getPluginManager().ensurePluginContainer(this).getDescription().getVersion().orElse("");
    }

    @Override
    public String getServerName() {
        return proxy.getVersion().getName();
    }

    @Override
    public String getServerVersion() {
        return proxy.getVersion().getVersion();
    }

    @Override
    public ArrayList<String> getPlugins() {
        ArrayList<String> plugins = new ArrayList<>();
        getProxy().getPluginManager().getPlugins().forEach(p -> plugins.add(p.getDescription().getName().orElseThrow(null)));
        return plugins;
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.VELOCITY;
    }
}
