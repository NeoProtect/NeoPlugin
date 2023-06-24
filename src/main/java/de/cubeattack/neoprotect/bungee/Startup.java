package de.cubeattack.neoprotect.bungee;

import de.cubeattack.neoprotect.bungee.command.NeoProtectCommand;
import de.cubeattack.neoprotect.bungee.listener.ChatListener;
import de.cubeattack.neoprotect.bungee.listener.DisconnectListener;
import de.cubeattack.neoprotect.bungee.listener.LoginListener;
import de.cubeattack.neoprotect.bungee.proxyprotocol.ProxyProtocol;
import de.cubeattack.neoprotect.core.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginManager;

public class Startup {

    private final PluginManager pm = ProxyServer.getInstance().getPluginManager();
    private boolean proxyProtocol = false;

    public Startup(NeoProtectBungee instance){
        register(instance);
        runProxyProtocol(instance);
    }

    private void register(NeoProtectBungee instance){
        pm.registerCommand(instance, new NeoProtectCommand(instance,"neoprotect", "neoprotect.admin", "np"));

        pm.registerListener(instance, new ChatListener(instance));
        pm.registerListener(instance, new LoginListener(instance));
        pm.registerListener(instance, new DisconnectListener());
    }

    public void runProxyProtocol(NeoProtectBungee instance){
        if(Config.isProxyProtocol() & instance.getCore().isSetup() & !proxyProtocol) {
            new ProxyProtocol();
            proxyProtocol = true;
        }
    }
}
