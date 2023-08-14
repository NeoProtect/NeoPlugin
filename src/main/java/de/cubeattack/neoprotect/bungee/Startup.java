package de.cubeattack.neoprotect.bungee;

import de.cubeattack.neoprotect.bungee.command.NeoProtectCommand;
import de.cubeattack.neoprotect.bungee.listener.ChatListener;
import de.cubeattack.neoprotect.bungee.listener.DisconnectListener;
import de.cubeattack.neoprotect.bungee.listener.LoginListener;
import de.cubeattack.neoprotect.bungee.proxyprotocol.ProxyProtocol;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginManager;

public class Startup {

    public Startup(NeoProtectBungee instance) {
        register(instance);
        new ProxyProtocol(instance);
    }

    private void register(NeoProtectBungee instance) {
        PluginManager pm = ProxyServer.getInstance().getPluginManager();
        pm.registerCommand(instance, new NeoProtectCommand(instance, "neoprotect", "neoprotect.admin", "np"));

        pm.registerListener(instance, new ChatListener(instance));
        pm.registerListener(instance, new LoginListener(instance));
        pm.registerListener(instance, new DisconnectListener(instance));
    }
}
