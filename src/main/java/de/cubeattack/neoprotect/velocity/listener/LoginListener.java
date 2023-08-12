package de.cubeattack.neoprotect.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.versioning.VersionUtils;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LoginListener {

    private final NeoProtectVelocity instance;
    private final Localization localization;

    public LoginListener(NeoProtectVelocity instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                Locale locale = (player.getEffectiveLocale() != null) ? player.getEffectiveLocale() : Locale.ENGLISH;

                if (!player.hasPermission("neoprotect.admin") && !instance.getCore().isPlayerMaintainer(player.getUniqueId(), instance.getProxy().getConfiguration().isOnlineMode()))
                    return;

                VersionUtils.Result result = instance.getCore().getVersionResult();
                if (result.getVersionStatus().equals(VersionUtils.VersionStatus.OUTDATED)) {
                    instance.sendMessage(player, localization.get(locale, "plugin.outdated.message", result.getCurrentVersion(), result.getLatestVersion()));
                    instance.sendMessage(player, MessageFormat.format("§7-> §b{0}",
                                    result.getReleaseUrl().replace("/NeoPlugin", "").replace("/releases/tag", "")),
                            "OPEN_URL", result.getReleaseUrl(), null, null);
                }

                if (result.getVersionStatus().equals(VersionUtils.VersionStatus.REQUIRED_RESTART)) {
                    instance.sendMessage(player, localization.get(locale, "plugin.restart-required.message", result.getCurrentVersion(), result.getLatestVersion()));
                }

                if (!instance.getCore().isSetup() && instance.getCore().getPlayerInSetup().isEmpty()) {
                    instance.sendMessage(player, localization.get(locale, "setup.required.first"));
                    instance.sendMessage(player, localization.get(locale, "setup.required.second"));
                }

                if (instance.getCore().isPlayerMaintainer(player.getUniqueId(), instance.getProxy().getConfiguration().isOnlineMode())) {
                    String infos =
                            "§bOsName§7: " + System.getProperty("os.name") + " \n" +
                                    "§bJavaVersion§7: " + System.getProperty("java.version") + " \n" +
                                    "§bPluginVersion§7: " + instance.getVersion() + " \n" +
                                    "§bVersionStatus§7: " + instance.getCore().getVersionResult().getVersionStatus() + " \n" +
                                    "§bUpdateSetting§7: " + Config.getAutoUpdaterSettings() + " \n" +
                                    "§bProxyProtocol§7: " + Config.isProxyProtocol() + " \n" +
                                    "§bNeoProtectPlan§7: " + (instance.getCore().isSetup() ? instance.getCore().getRestAPI().getPlan() : "§cNOT CONNECTED") + " \n" +
                                    "§bVelocityName§7: " + instance.getServerName() + " \n" +
                                    "§bVelocityVersion§7: " + instance.getServerVersion() + " \n" +
                                    "§bVelocityPlugins§7: " + Arrays.toString(instance.getPlugins().stream().filter(p -> !p.startsWith("cmd_") && !p.equals("reconnect_yaml")).toArray());

                    instance.sendMessage(player, "§bHello " + player.getUsername() + " ;)", null, null, "SHOW_TEXT", infos);
                    instance.sendMessage(player, "§bThis server uses your NeoPlugin", null, null, "SHOW_TEXT", infos);
                }
            }
        }, 500);
    }
}
