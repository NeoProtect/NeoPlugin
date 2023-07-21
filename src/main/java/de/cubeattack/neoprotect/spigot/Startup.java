package de.cubeattack.neoprotect.spigot;

import de.cubeattack.neoprotect.spigot.command.NeoProtectCommand;
import de.cubeattack.neoprotect.spigot.command.NeoProtectTabCompleter;
import de.cubeattack.neoprotect.spigot.listener.ChatListener;
import de.cubeattack.neoprotect.spigot.listener.DisconnectListener;
import de.cubeattack.neoprotect.spigot.listener.LoginListener;
import de.cubeattack.neoprotect.spigot.proxyprotocol.ProxyProtocol;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public class Startup {

    public Startup(NeoProtectSpigot instance) {
        register(instance);
        new ProxyProtocol(instance);
    }

    private void register(NeoProtectSpigot instance) {
        PluginManager pm = Bukkit.getPluginManager();
        Objects.requireNonNull(instance.getCommand("neoprotect")).setExecutor(new NeoProtectCommand(instance));
        Objects.requireNonNull(instance.getCommand("neoprotect")).setTabCompleter(new NeoProtectTabCompleter(instance));

        pm.registerEvents(new ChatListener(instance), instance);
        pm.registerEvents(new LoginListener(instance), instance);
        pm.registerEvents(new DisconnectListener(instance), instance);
    }
}
