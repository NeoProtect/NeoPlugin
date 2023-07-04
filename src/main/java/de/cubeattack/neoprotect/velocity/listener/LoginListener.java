package de.cubeattack.neoprotect.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.api.language.Localization;
import de.cubeattack.api.utils.VersionUtils;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

public class LoginListener {

    private final NeoProtectVelocity instance;
    private final Localization localization;

    public LoginListener(NeoProtectVelocity instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event){
        Player player = event.getPlayer();

        if(!player.hasPermission("neoprotect.admin")) return;

        VersionUtils.Result result = instance.getCore().getVersionResult();
        if(result.getVersionStatus().equals(VersionUtils.VersionStatus.OUTDATED)){
            instance.sendMessage(player, localization.get("plugin.outdated.message", result.getCurrentVersion(), result.getLatestVersion()));
            instance.sendMessage(player, localization.get("plugin.outdated.link", result.getReleaseUrl()), "OPEN_URL", result.getReleaseUrl(), null, null);
        }

        if(!instance.getCore().isSetup() && instance.getCore().getPLAYER_IN_SETUP().isEmpty()){
            instance.sendMessage(player, localization.get("setup.required.first"));
            instance.sendMessage(player, localization.get("setup.required.second"));
        }
    }
}
