package de.cubeattack.neoprotect.bungee.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.versioning.VersionUtils;
import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.model.Stats;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
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
        if(instance.getCore().isPlayerMaintainer(event.getPlayer().getUniqueId(), instance.getProxy().getConfig().isOnlineMode()))
            event.getPlayer().setPermission("neoprotect.admin", true);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ProxiedPlayer player = event.getPlayer();
                Locale locale = (player.getLocale() != null) ? player.getLocale() : Locale.ENGLISH;

                if (!player.hasPermission("neoprotect.admin") && !instance.getCore().isPlayerMaintainer(player.getUniqueId(), instance.getProxy().getConfig().isOnlineMode()))
                    return;

                VersionUtils.Result result = instance.getCore().getVersionResult();
                if (result.getVersionStatus().equals(VersionUtils.VersionStatus.OUTDATED)) {
                    instance.sendMessage(player, localization.get(locale, result.getCurrentVersion(), result.getLatestVersion()));
                    instance.sendMessage(player, MessageFormat.format("§7-> §b{0}",
                                    result.getReleaseUrl().replace("/NeoPlugin", "").replace("/releases/tag", "")),
                            "OPEN_URL", result.getReleaseUrl(), null, null);
                }

                if (result.getVersionStatus().equals(VersionUtils.VersionStatus.REQUIRED_RESTART)) {
                    instance.sendMessage(player, localization.get(locale, "plugin.restart-required.message", result.getCurrentVersion(), result.getLatestVersion()));
                }

                if (!instance.getCore().isSetup()) {
                    instance.sendMessage(player, localization.get(locale, "setup.required.first"));
                    instance.sendMessage(player, localization.get(locale, "setup.required.second"));
                }

                if (instance.getCore().isPlayerMaintainer(player.getUniqueId(), instance.getProxy().getConfig().isOnlineMode())) {
                    Stats stats = instance.getStats();
                    String infos =
                            "§bOsName§7: " + System.getProperty("os.name") + " \n" +
                                    "§bJavaVersion§7: " + System.getProperty("java.version") + " \n" +
                                    "§bPluginVersion§7: " + stats.getPluginVersion() + " \n" +
                                    "§bVersionStatus§7: " + instance.getCore().getVersionResult().getVersionStatus() + " \n" +
                                    "§bUpdateSetting§7: " + Config.getAutoUpdaterSettings() + " \n" +
                                    "§bProxyProtocol§7: " + Config.isProxyProtocol() + " \n" +
                                    "§bNeoProtectPlan§7: " + (instance.getCore().isSetup() ? instance.getCore().getRestAPI().getPlan() : "§cNOT CONNECTED") + " \n" +
                                    "§bBungeecordName§7: " + stats.getServerName() + " \n" +
                                    "§bBungeecordVersion§7: " + stats.getServerVersion()+ " \n" +
                                    "§bBungeecordPlugins§7: " + Arrays.toString(instance.getPlugins().stream().filter(p -> !p.startsWith("cmd_") && !p.equals("reconnect_yaml")).toArray());

                    instance.sendMessage(player, "§bHello " + player.getName() + " ;)", null, null, "SHOW_TEXT", infos);
                    instance.sendMessage(player, "§bThis server uses your NeoPlugin", null, null, "SHOW_TEXT", infos);
                }
            }
        }, 500);
    }
}
