package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.util.VersionUtils;
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

        if (!instance.getCore().isSetup() && instance.getCore().getPlayerInSetup().isEmpty()) {
            instance.sendMessage(player, localization.get("setup.required.first"));
            instance.sendMessage(player, localization.get("setup.required.second"));
        }

        if (Arrays.stream(instance.getCore().getMaintainerUUID()).anyMatch(uuid -> uuid.equals(player.getUniqueId()))) {
            instance.sendMessage(player, "§bHello " + player.getName() + " ;)");
            instance.sendMessage(player, "§bThis server uses your NeoPlugin");
        }
    }
}
