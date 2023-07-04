package de.cubeattack.neoprotect.spigot;

import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.Permission;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

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
    public void sendMessage(Object sender, String text) {
        sendMessage(sender, text, null, null, null, null);
    }

    @Override
    public void sendMessage(Object sender, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = new TextComponent(core.getPrefix() + text);

        if(clickAction != null) msg.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if(hoverAction != null) msg.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(hoverAction), Collections.singletonList(new Text(hoverMsg))));
        if(sender instanceof Player) ((Player) sender).spigot().sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getServer().getOnlinePlayers().forEach(pp -> {
            if(pp.hasPermission("neoprotect.admin") || pp.hasPermission(permission.value))
                sendMessage(pp, text, clickAction, clickMsg, hoverAction, hoverMsg);
        });
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }
}
