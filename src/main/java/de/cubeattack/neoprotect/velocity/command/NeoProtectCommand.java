package de.cubeattack.neoprotect.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;
import de.cubeattack.neoprotect.velocity.listener.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NeoProtectCommand implements SimpleCommand {

    private final NeoProtectVelocity instance;
    private final Localization localization;

    public NeoProtectCommand(NeoProtectVelocity instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Override
    public void execute(Invocation invocation) {

        if(!(invocation.source() instanceof Player)){
            instance.sendMessage(invocation.source(), localization.get("console.command"));
            return;
        }

        if(invocation.arguments().length == 0){
            showHelp(invocation.source());
            return;
        }

        if(!instance.getCore().isSetup() & !invocation.arguments()[0].equals("setup") & !invocation.arguments()[0].equals("setgameshield") & !invocation.arguments()[0].equals("setbackend")){
            instance.sendMessage(invocation.source(), localization.get("setup.command.required"));
            return;
        }

        switch (invocation.arguments()[0]) {
            case "setup": {
                ChatListener.PLAYER_IN_SETUP.add(invocation.source());
                instance.sendMessage(invocation.source(), localization.get("command.setup") + localization.get("utils.click"),
                        ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://panel.neoprotect.net/profile"),
                        HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(localization.get("apikey.find"))));
                break;
            }
            case "ipanic": {
                if(invocation.arguments().length != 1){
                    instance.sendMessage(invocation.source(), localization.get("usage.ipanic"));
                } else {
                    instance.sendMessage(invocation.source(), localization.get("command.ipanic",
                            localization.get(instance.getCore().getRestAPI().togglePanicMode() ? "utils.activated" : "utils.deactivated")));
                }
                break;
            }
            case "setgameshield": {
                if(invocation.arguments().length == 1){
                    instance.sendMessage(invocation.source(), localization.get("select.gameshield"));

                    HashMap<String, String> gameshields = instance.getCore().getRestAPI().getGameshields();

                    for (Object set : gameshields.keySet()) {
                        instance.sendMessage(invocation.source(), "§5" + gameshields.get(set.toString()) + localization.get("utils.click"),
                                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/np setgameshield " + set),
                                HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(localization.get("hover.gameshield", gameshields.get(set.toString()), set))));
                    }
                } else if (invocation.arguments().length == 2)  {
                    Config.setGameShieldID(invocation.arguments()[1]);

                    instance.sendMessage(invocation.source(),  localization.get("set.gameshield", invocation.arguments()[1]));
                    instance.sendMessage(invocation.source(), localization.get("select.backend"));

                    HashMap<String, String> backend = instance.getCore().getRestAPI().getBackends();

                    for (Object set: backend.keySet()) {
                        instance.sendMessage(invocation.source(), "§5" + backend.get(set.toString()) + localization.get("utils.click"),
                                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/np setbackend " + set),
                                HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(localization.get("hover.backend", backend.get(set.toString()), set))));
                    }
                }else {
                    instance.sendMessage(invocation.source(), localization.get("usage.setgameshield"));
                }
                break;
            }
            case "setbackend": {
                if(invocation.arguments().length == 1){
                    instance.sendMessage(invocation.source(), localization.get("select.backend"));

                    HashMap<String, String> backend = instance.getCore().getRestAPI().getBackends();

                    for (Object set: backend.keySet()) {
                        instance.sendMessage(invocation.source(), "§5" + backend.get(set.toString()) + localization.get("utils.click"),
                                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/np setbackend " + set),
                                HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(localization.get("hover.backend", backend.get(set.toString()), set))));
                    }
                }else if (invocation.arguments().length == 2){
                    Config.setBackendID(invocation.arguments()[1]);

                    instance.sendMessage(invocation.source(), localization.get("set.backend", invocation.arguments()[1]));
                    instance.getCore().getRestAPI().tests();

                    if(ChatListener.PLAYER_IN_SETUP.remove(invocation.source())){
                        instance.sendMessage(invocation.source(), localization.get("setup.finished"));
                        if(Config.isProxyProtocol()){
                            instance.sendMessage(invocation.source(), localization.get("setup.restart.required"));
                        }
                    }
                }else {
                    instance.sendMessage(invocation.source(), localization.get("usage.setbackend"));
                }
                break;
            }
            default: {
                showHelp(invocation.source());
            }
        }
    }

    private void showHelp(CommandSource sender){
        instance.sendMessage(sender,  localization.get("available.commands"));
        instance.sendMessage(sender, " - /np setup");
        instance.sendMessage(sender, " - /np ipanic");
        instance.sendMessage(sender, " - /np setgameshield");
        instance.sendMessage(sender, " - /np setbackend");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("neoprotect.admin");
    }
}
