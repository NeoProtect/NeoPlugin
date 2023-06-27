package de.cubeattack.neoprotect.spigot;

import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
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
    public void sendMessage(Object sender, String text) {
        sendMessage(sender, text, null, null);
    }

    @Override
    public void sendMessage(Object sender, String text, Object clickEvent, Object hoverEvent) {
        TextComponent msg = new TextComponent(core.getPrefix() + text);

        if(clickEvent instanceof ClickEvent) msg.setClickEvent((ClickEvent) clickEvent);
        if(hoverEvent instanceof HoverEvent) msg.setHoverEvent((HoverEvent) hoverEvent);
        if(sender instanceof CommandSender) ((CommandSender) sender).spigot().sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(String text, Object clickEvent, Object hoverEvent) {
        getServer().getOnlinePlayers().forEach(pp -> {if(pp.hasPermission("neoprotect.admin"))sendMessage(pp, text, clickEvent, hoverEvent);});
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }
}
