package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginListener implements Listener {

    private final NeoProtectSpigot instance;
    private final Localization localization;

    public LoginListener(NeoProtectSpigot instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }
    @EventHandler
    public void onLogin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(!player.hasPermission("neoprotect.admin") || instance.getCore().isSetup() || !ChatListener.PLAYER_IN_SETUP.isEmpty()) return;

        instance.sendMessage(player, localization.get("setup.required.first"));
        instance.sendMessage(player, localization.get("setup.required.second"));
    }
}
