package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.utils.VersionUtils;
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

        if(!player.hasPermission("neoprotect.admin")) return;

        VersionUtils.Result result = instance.getCore().getVersionResult();
        if(result.getVersionStatus().equals(VersionUtils.VersionStatus.OUTDATED)){
            instance.sendMessage(player, localization.get("plugin.outdated.message", result.getCurrentVersion(), result.getLatestVersion()));
            instance.sendMessage(player, localization.get("plugin.outdated.link", result.getReleaseUrl()), "OPEN_URL", result.getReleaseUrl(), null, null);
        }

        if(!instance.getCore().isSetup() && instance.getCore().getPlayerInSetup().isEmpty()){
            instance.sendMessage(player, localization.get("setup.required.first"));
            instance.sendMessage(player, localization.get("setup.required.second"));
        }
    }
}
