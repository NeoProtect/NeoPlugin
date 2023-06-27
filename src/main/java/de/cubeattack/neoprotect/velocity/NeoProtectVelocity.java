package de.cubeattack.neoprotect.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

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
        sendMessage(sender, text, null, null);
    }

    @Override
    public void sendMessage(Object sender, String text, Object clickEvent, Object hoverEvent) {
        TextComponent msg = Component.text(core.getPrefix() + text);

        if(clickEvent instanceof ClickEvent) msg = msg.clickEvent((ClickEvent) clickEvent);
        if(hoverEvent instanceof HoverEvent) msg = msg.hoverEvent((HoverEvent<?>) hoverEvent);
        if(sender instanceof CommandSource) ((CommandSource) sender).sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(String text, Object clickEvent, Object hoverEvent) {
        getProxy().getAllPlayers().forEach(pp -> {if(pp.hasPermission("neoprotect.admin"))sendMessage(pp, text, clickEvent, hoverEvent);});
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getVersion() {
        return proxy.getPluginManager().ensurePluginContainer(this).getDescription().getVersion().orElse("");
    }
}
