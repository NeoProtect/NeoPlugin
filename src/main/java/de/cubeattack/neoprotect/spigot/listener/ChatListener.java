package de.cubeattack.neoprotect.spigot.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatListener implements Listener {

    private final NeoProtectSpigot instance;
    private final Localization localization;

    public ChatListener(NeoProtectSpigot instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    public static final List<CommandSender> PLAYER_IN_SETUP = new ArrayList<>();

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("neoprotect.admin") || !PLAYER_IN_SETUP.contains(player)) return;

        event.setCancelled(true);

        CompletableFuture.runAsync(() -> {

            if (instance.getCore().getRestAPI().isAPIInvalid(event.getMessage())) {
                instance.sendMessage(player, localization.get("apikey.invalid"));
                return;
            }

            instance.sendMessage(player, localization.get("apikey.valid"));

            instance.sendMessage(player, localization.get("select.gameshield"));

            HashMap<String, String> gameshields = instance.getCore().getRestAPI().getGameshields();

            for (Object set : gameshields.keySet()) {
                instance.sendMessage(player, "§5" + gameshields.get(set.toString()) + localization.get("utils.click"),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setgameshield " + set),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(localization.get("hover.gameshield", gameshields.get(set.toString()), set)).create()));
            }
        });
    }
}
