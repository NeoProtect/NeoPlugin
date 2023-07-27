package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.VersionUtils;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.MessageFormat;
import java.util.Arrays;

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
}
