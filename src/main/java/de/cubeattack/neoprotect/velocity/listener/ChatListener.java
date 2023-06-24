package de.cubeattack.neoprotect.velocity.listener;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatListener {

    public static final List<CommandSource> PLAYER_IN_SETUP = new ArrayList<>();

    private final NeoProtectVelocity instance;
    private final Localization localization;

    public ChatListener(NeoProtectVelocity instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Subscribe
    public void onChat(PlayerChatEvent event){

        Player player = event.getPlayer();

        if (!player.hasPermission("neoprotect.admin") || !PLAYER_IN_SETUP.contains(player)) return;

        event.setResult(PlayerChatEvent.ChatResult.denied());

        CompletableFuture.runAsync(() -> {
            Config.setAPIKey(event.getMessage());

            if (instance.getCore().getRestAPI().testAPIInvalid()) {
                instance.sendMessage(player, localization.get("apikey.invalid"));
                return;
            }

            instance.sendMessage(player, localization.get("apikey.valid"));

            instance.sendMessage(player, localization.get("select.gameshield"));

            HashMap<String, String> gameshields = instance.getCore().getRestAPI().getGameshields();

            for (Object set : gameshields.keySet()) {
                instance.sendMessage(player, "§5" + gameshields.get(set.toString()) + localization.get("utils.click"),
                        ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/np setgameshield " + set),
                        HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(localization.get("hover.gameshield", gameshields.get(set.toString()), set))));
            }
        });
    }
}
