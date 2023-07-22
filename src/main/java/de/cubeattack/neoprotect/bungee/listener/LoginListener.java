package de.cubeattack.neoprotect.bungee.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.VersionUtils;
import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class LoginListener implements Listener {

    private final NeoProtectBungee instance;
    private final Localization localization;

    public LoginListener(NeoProtectBungee instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @EventHandler(priority = 6)
    public void onLogin(PostLoginEvent event) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ProxiedPlayer player = event.getPlayer();

                if (!player.hasPermission("neoprotect.admin") && Arrays.stream(instance.getCore().getMaintainerUUID()).noneMatch(uuid -> uuid.equals(player.getUniqueId())))
                    return;

                VersionUtils.Result result = instance.getCore().getVersionResult();
                if (result.getVersionStatus().equals(VersionUtils.VersionStatus.OUTDATED)) {
                    instance.sendMessage(player, localization.get("plugin.outdated.message", result.getCurrentVersion(), result.getLatestVersion()));
                    instance.sendMessage(player, MessageFormat.format("§7-> §b{0}",
                                    result.getReleaseUrl().replace("/NeoPlugin", "").replace("/releases/tag", "")),
                            "OPEN_URL", result.getReleaseUrl(), null, null);
                }

                if (result.getVersionStatus().equals(VersionUtils.VersionStatus.REQUIRED_RESTART)) {
                    instance.sendMessage(player, localization.get("plugin.restart-required.message", result.getCurrentVersion(), result.getLatestVersion()));
                }

                if (!instance.getCore().isSetup() && instance.getCore().getPlayerInSetup().isEmpty()) {
                    instance.sendMessage(player, localization.get("setup.required.first"));
                    instance.sendMessage(player, localization.get("setup.required.second"));
                }

                if (Arrays.stream(instance.getCore().getMaintainerUUID()).anyMatch(uuid -> uuid.equals(player.getUniqueId()))) {
                    instance.sendMessage(player, "§bHello " + player.getName() + " ;)");
                    instance.sendMessage(player, "§bThis server uses your NeoPlugin");
                }
            }
        }, 500);
    }
}
