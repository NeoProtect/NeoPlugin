package de.cubeattack.neoprotect.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.Permission;
import de.cubeattack.neoprotect.core.model.KeepAliveResponseKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bstats.velocity.Metrics;

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
    public void onProxyInitialize(ProxyInitializeEvent event){
        metricsFactory.make(this, 18727);
        core = new Core(this);
        new Startup(this);
    }

    public Core getCore() {
        return core;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    @Override
    public void sendMessage(Object sender, String text) {
        sendMessage(sender, text, null, null, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendMessage(Object sender, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = Component.text(core.getPrefix() + text);

        if(clickAction != null) msg = msg.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if(hoverAction != null) msg = msg.hoverEvent(HoverEvent.hoverEvent((HoverEvent.Action<Object>) Objects.requireNonNull(HoverEvent.Action.NAMES.value(hoverAction.toLowerCase())),
                Component.text(hoverMsg)));

        if(sender instanceof CommandSource) ((CommandSource) sender).sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getProxy().getAllPlayers().forEach(pp -> {
            if(pp.hasPermission("neoprotect.admin") || pp.hasPermission(permission.value))
                sendMessage(pp, text, clickAction, clickMsg, hoverAction, hoverMsg);
        });
    }

    @Override
    public KeepAliveResponseKey sendKeepAliveMessage(Object sender, long id) {
        if(sender instanceof Player){
            //((Player)sender).unsafe().sendPacket(new KeepAlive().setRandomId(id));
            return new KeepAliveResponseKey(((Player)sender).getRemoteAddress(), id);
        }
        return null;
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
    public PluginType getPluginType() {
        return PluginType.VELOCITY;
    }
}
