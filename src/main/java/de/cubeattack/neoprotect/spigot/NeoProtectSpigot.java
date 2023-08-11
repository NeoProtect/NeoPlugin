package de.cubeattack.neoprotect.spigot;

import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.Permission;
import de.cubeattack.neoprotect.core.model.Stats;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

public class NeoProtectSpigot extends JavaPlugin implements NeoProtectPlugin {

    private static Core core;

    @Override
    public void onLoad() {
        Metrics metrics = new Metrics(this, 18725);
        metrics.addCustomChart(new SimplePie("language", Config::getLanguage));
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
        return new Stats(getServer().getOnlinePlayers().size());
    }

    @Override
    public void sendMessage(Object receiver, String text) {
        sendMessage(receiver, text, null, null, null, null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendMessage(Object receiver, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        TextComponent msg = new TextComponent(core.getPrefix() + text);

        if (clickAction != null)
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), clickMsg));
        if (hoverAction != null)
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(hoverAction), new ComponentBuilder(hoverMsg).create()));
        if (receiver instanceof ConsoleCommandSender) ((ConsoleCommandSender) receiver).sendMessage(msg.toLegacyText());
        if (receiver instanceof Player) ((Player) receiver).spigot().sendMessage(msg);
    }

    @Override
    public void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg) {
        getServer().getOnlinePlayers().forEach(pp -> {
            if (pp.hasPermission("neoprotect.admin") || pp.hasPermission(permission.value))
                sendMessage(pp, text, clickAction, clickMsg, hoverAction, hoverMsg);
        });
    }

    @Override
    public void sendKeepAliveMessage(Object sender, long id) {
    }

    @Override
    public long sendKeepAliveMessage(long id) {
        return id;
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.SPIGOT;
    }

    @Override
    public String getServerName() {
        return getServer().getName();
    }

    @Override
    public String getServerVersion() {
        return getServer().getVersion();
    }

    @Override
    public ArrayList<String> getPlugins() {
        ArrayList<String> plugins = new ArrayList<>();
        Arrays.stream(getServer().getPluginManager().getPlugins()).forEach(p -> plugins.add(p.getName()));
        return plugins;
    }
}
