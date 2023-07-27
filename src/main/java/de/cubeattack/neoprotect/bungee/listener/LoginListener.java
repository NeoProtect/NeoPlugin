package de.cubeattack.neoprotect.bungee.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.VersionUtils;
import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import de.cubeattack.neoprotect.core.Config;
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
                    String infos =
                            "§bOsName§7: " + System.getProperty("os.name") + " \n" +
                            "§bJavaVersion§7: " + System.getProperty("java.version") + " \n" +
                            "§bPluginVersion§7: " + instance.getVersion() + " \n" +
                            "§bVersionStatus§7: " + instance.getCore().getVersionResult().getVersionStatus() + " \n" +
                            "§bUpdateSetting§7: " + Config.getAutoUpdaterSettings() + " \n" +
                            "§bProxyProtocol§7: " + Config.isProxyProtocol() + " \n" +
                            "§bNeoProtectPlan§7: " + instance.getCore().getRestAPI().getPlan() + " \n" +
                            "§bBungeecordName§7: " + instance.getProxyName() + " \n" +
                            "§bBungeecordVersion§7: " + instance.getProxyVersion() + " \n" +
                            "§bBungeecordPlugins§7: " + Arrays.toString(instance.getProxyPlugins().stream().filter(p -> !p.startsWith("cmd_") && !p.equals("reconnect_yaml")).toArray());

                    instance.sendMessage(player, "§bHello " + player.getName() + " ;)", null, null, "SHOW_TEXT", infos);
                    instance.sendMessage(player, "§bThis server uses your NeoPlugin", null, null, "SHOW_TEXT", infos);
                }
            }
        }, 500);
    }
}
