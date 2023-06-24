package de.cubeattack.neoprotect.spigot;

import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.spigot.command.NeoProtectCommand;
import de.cubeattack.neoprotect.spigot.listener.ChatListener;
import de.cubeattack.neoprotect.spigot.listener.DisconnectListener;
import de.cubeattack.neoprotect.spigot.listener.LoginListener;
import de.cubeattack.neoprotect.spigot.proxyprotocol.ProxyProtocol;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public class Startup {

    private static final PluginManager pm = Bukkit.getPluginManager();
    private boolean proxyProtocol = false;

    public Startup(NeoProtectSpigot instance){
        register(instance);
        runProxyProtocol(instance);
    }

    private void register(NeoProtectSpigot instance){
        Objects.requireNonNull(instance.getCommand("neoprotect")).setExecutor(new NeoProtectCommand(instance));

        pm.registerEvents(new ChatListener(instance), instance);
        pm.registerEvents(new LoginListener(instance), instance);
        pm.registerEvents(new DisconnectListener(), instance);
    }

    public void runProxyProtocol(NeoProtectSpigot instance){
        if(Config.isProxyProtocol() & instance.getCore().isSetup() & !proxyProtocol) {
            new ProxyProtocol(instance);
            proxyProtocol = true;
        }
    }
}
