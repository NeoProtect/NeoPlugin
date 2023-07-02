package de.cubeattack.neoprotect.bungee.listener;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatListener implements Listener {

    public static final List<Object> PLAYER_IN_SETUP = new ArrayList<>();

    private final NeoProtectBungee instance;
    private final Localization localization;
    public ChatListener(NeoProtectBungee instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @EventHandler
    public void onChat(ChatEvent event){
        CommandSender sender = (CommandSender) event.getSender();

        if(!sender.hasPermission("neoprotect.admin") || !PLAYER_IN_SETUP.contains(sender) || event.isCommand()) return;

        event.setCancelled(true);

        CompletableFuture.runAsync(() -> {

            if(instance.getCore().getRestAPI().isAPIInvalid(event.getMessage())){
                instance.sendMessage(sender, localization.get("apikey.invalid"));
                return;
            }

            instance.sendMessage(sender, localization.get("apikey.valid"));

            instance.sendMessage(sender, localization.get("select.gameshield"));

            HashMap<String, String> gameshields = instance.getCore().getRestAPI().getGameshields();

            for (Object set: gameshields.keySet()) {
                instance.sendMessage(sender, "§5"+ gameshields.get(set.toString()) + localization.get("utils.click"),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setgameshield " + set),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Collections.singletonList(new Text(localization.get("hover.gameshield", gameshields.get(set.toString()), set)))));
            }
        });
    }
}
