package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.JavaUtils;
import de.cubeattack.api.util.versioning.VersionUtils;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.model.Stats;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;

public class LoginListener implements Listener {

    private final NeoProtectSpigot instance;
    private final Localization localization;

    public LoginListener(NeoProtectSpigot instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Locale locale = JavaUtils.javaVersionCheck() != 8 ? Locale.forLanguageTag(player.getLocale()) : Locale.ENGLISH;

        if (!player.hasPermission("neoprotect.admin") && !instance.getCore().isPlayerMaintainer(player.getUniqueId(), instance.getServer().getOnlineMode()))
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

        if (!instance.getCore().isSetup()) {
            instance.sendMessage(player, localization.get(locale, "setup.required.first"));
            instance.sendMessage(player, localization.get(locale, "setup.required.second"));
        }

        if (instance.getCore().isPlayerMaintainer(player.getUniqueId(), instance.getServer().getOnlineMode())) {
            Stats stats = instance.getStats();
            String infos =
                    "§bOsName§7: " + System.getProperty("os.name") + " \n" +
                            "§bJavaVersion§7: " + System.getProperty("java.version") + " \n" +
                            "§bPluginVersion§7: " + stats.getPluginVersion() + " \n" +
                            "§bVersionStatus§7: " + instance.getCore().getVersionResult().getVersionStatus() + " \n" +
                            "§bUpdateSetting§7: " + Config.getAutoUpdaterSettings() + " \n" +
                            "§bProxyProtocol§7: " + Config.isProxyProtocol() + " \n" +
                            "§bNeoProtectPlan§7: " + (instance.getCore().isSetup() ? instance.getCore().getRestAPI().getPlan() : "§cNOT CONNECTED") + " \n" +
                            "§bSpigotName§7: " + stats.getServerName() + " \n" +
                            "§bSpigotVersion§7: " + stats.getServerVersion() + " \n" +
                            "§bSpigotPlugins§7: " + Arrays.toString(instance.getPlugins().stream().filter(p -> !p.startsWith("cmd_") && !p.equals("reconnect_yaml")).toArray());

            instance.sendMessage(player, "§bHello " + player.getName() + " ;)", null, null, "SHOW_TEXT", infos);
            instance.sendMessage(player, "§bThis server uses your NeoPlugin", null, null, "SHOW_TEXT", infos);
        }
    }
}
