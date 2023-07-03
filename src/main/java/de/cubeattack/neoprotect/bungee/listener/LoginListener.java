package de.cubeattack.neoprotect.bungee.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginListener implements Listener {

    private final NeoProtectBungee instance;
    private final Localization localization;
    public LoginListener(NeoProtectBungee instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }
    @EventHandler
    public void onLogin(PostLoginEvent event){
        ProxiedPlayer player = event.getPlayer();

        if(!player.hasPermission("neoprotect.admin") || instance.getCore().isSetup() || !instance.getCore().getPLAYER_IN_SETUP().isEmpty()) return;

        instance.sendMessage(player, localization.get("setup.required.first"));
        instance.sendMessage(player, localization.get("setup.required.second"));

    }
}
