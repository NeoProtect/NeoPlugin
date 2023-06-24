package de.cubeattack.neoprotect.velocity;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.proxy.VelocityServer;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.velocity.command.NeoProtectCommand;
import de.cubeattack.neoprotect.velocity.listener.ChatListener;
import de.cubeattack.neoprotect.velocity.listener.DisconnectListener;
import de.cubeattack.neoprotect.velocity.listener.LoginListener;
import de.cubeattack.neoprotect.velocity.messageunsign.JoinListener;
import de.cubeattack.neoprotect.velocity.messageunsign.SessionChatListener;
import de.cubeattack.neoprotect.velocity.proxyprotocol.ProxyProtocol;

public class Startup {

    private boolean proxyProtocol = false;

    public Startup(NeoProtectVelocity instance){
        register(instance);
        runProxyProtocol(instance);
    }

    private void register(NeoProtectVelocity instance){
        EventManager em = instance.getProxy().getEventManager();
        CommandManager cm = instance.getProxy().getCommandManager();

        cm.register(cm.metaBuilder("neoprotect").aliases("np").build(), new NeoProtectCommand(instance));

        em.register(instance, new ChatListener(instance));
        em.register(instance, new LoginListener(instance));
        em.register(instance, new DisconnectListener());
        em.register(instance, new JoinListener(instance));
        em.register(instance, new SessionChatListener(instance));
    }

    public void runProxyProtocol(NeoProtectVelocity instance){
        if(Config.isProxyProtocol() & instance.getCore().isSetup() & !proxyProtocol) {
            new ProxyProtocol((VelocityServer)instance.getProxy());
            proxyProtocol = true;
        }
    }
}
