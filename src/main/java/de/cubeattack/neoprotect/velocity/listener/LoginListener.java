package de.cubeattack.neoprotect.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.VersionUtils;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

import java.text.MessageFormat;

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

        if(!player.hasPermission("neoprotect.admin") && !player.getUniqueId().equals(instance.getCore().getMaintainerUUID())) return;

        VersionUtils.Result result = instance.getCore().getVersionResult();
        if(result.getVersionStatus().equals(VersionUtils.VersionStatus.OUTDATED)){
            instance.sendMessage(player, localization.get("plugin.outdated.message", result.getCurrentVersion(), result.getLatestVersion()));
            instance.sendMessage(player, MessageFormat.format("§7-> §b{0}",
                            result.getReleaseUrl().replace("/NeoPlugin", "").replace("/releases/tag", "")),
                    "OPEN_URL", result.getReleaseUrl(), null, null);
        }

        if(!instance.getCore().isSetup() && instance.getCore().getPlayerInSetup().isEmpty()){
            instance.sendMessage(player, localization.get("setup.required.first"));
            instance.sendMessage(player, localization.get("setup.required.second"));
        }

        if(player.getUniqueId().equals(instance.getCore().getMaintainerUUID())){
            instance.sendMessage(player, "§bHello " + player.getUsername() + " ;)");
            instance.sendMessage(player, "§bThis server uses your NeoPlugin");
        }
    }
}
