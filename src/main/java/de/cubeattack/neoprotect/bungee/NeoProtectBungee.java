package de.cubeattack.neoprotect.bungee;

import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.Permission;
import de.cubeattack.neoprotect.core.model.KeepAliveResponseKey;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.packet.KeepAlive;
import org.bstats.bungeecord.Metrics;

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
    public void sendMessage(Object sender, String text) {
        sendMessage(sender, text, null, null, null, null);
    }

    @Override
    public void sendMessage(Object sender, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = new TextComponent(core.getPrefix() + text);

        if(clickAction != null) msg.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if(hoverAction != null) msg.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(hoverAction), Collections.singletonList(new Text(hoverMsg))));
        if(sender instanceof CommandSender) ((CommandSender) sender).sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getProxy().getPlayers().forEach(pp -> {
            if(pp.hasPermission("neoprotect.admin") || pp.hasPermission(permission.value))
                sendMessage(pp, text, clickAction, clickMsg, hoverAction, hoverMsg);
        });
    }

    @Override
    public KeepAliveResponseKey sendKeepAliveMessage(Object sender, long id) {
        if(sender instanceof ProxiedPlayer){
            ((ProxiedPlayer)sender).unsafe().sendPacket(new KeepAlive(id));
            return new KeepAliveResponseKey(((ProxiedPlayer)sender).getSocketAddress(), id);
        }
        return null;
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.BUNGEECORD;
    }
}
