package de.cubeattack.neoprotect.spigot;

import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.Permission;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NeoProtectSpigot extends JavaPlugin implements NeoProtectPlugin {

    private static Core core;

    @Override
    public void onLoad() {
        new Metrics(this, 18725);
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
    @SuppressWarnings("deprecation")
    public void sendMessage(Object receiver, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = new TextComponent(core.getPrefix() + text);

        if(clickAction != null) msg.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if(hoverAction != null) msg.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(hoverAction), new ComponentBuilder(hoverMsg).create()));
        if(receiver instanceof ConsoleCommandSender) ((ConsoleCommandSender) receiver).sendMessage(msg.toLegacyText());
        if(receiver instanceof Player) ((Player) receiver).spigot().sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getServer().getOnlinePlayers().forEach(pp -> {
            if(pp.hasPermission("neoprotect.admin") || pp.hasPermission(permission.value))
                sendMessage(pp, text, clickAction, clickMsg, hoverAction, hoverMsg);
        });
    }

    @Override
    public void sendKeepAliveMessage(Object sender, long id) {}

    @Override
    public long sendKeepAliveMessage(long id) {return id;}

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.SPIGOT;
    }
}
