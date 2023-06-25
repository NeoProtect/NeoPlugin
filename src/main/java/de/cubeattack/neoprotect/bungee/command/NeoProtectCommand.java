package de.cubeattack.neoprotect.bungee.command;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.bungee.NeoProtectBungee;
import de.cubeattack.neoprotect.bungee.listener.ChatListener;
import de.cubeattack.neoprotect.core.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collections;
import java.util.HashMap;

public class NeoProtectCommand extends Command {

    private final NeoProtectBungee instance;
    private final Localization localization;
    public NeoProtectCommand(NeoProtectBungee instance, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(!(sender instanceof ProxiedPlayer)){
            instance.sendMessage(sender, localization.get("console.command"));
            return;
        }

        if(args.length == 0){
            showHelp(sender);
            return;
        }

        if(!instance.getCore().isSetup() & !args[0].equals("setup") & !args[0].equals("setgameshield") & !args[0].equals("setbackend") ){
            instance.sendMessage(sender, localization.get("setup.command.required"));
            return;
        }

        switch (args[0]) {
            case "setup": {
                ChatListener.PLAYER_IN_SETUP.add(sender);
                instance.sendMessage(sender, localization.get("command.setup") + localization.get("utils.click"),
                        new ClickEvent(ClickEvent.Action.OPEN_URL, "https://panel.neoprotect.net/profile"),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,Collections.singletonList(new Text(localization.get("apikey.find")))));
                break;
            }
            case "ipanic": {
                if(args.length != 1){
                    instance.sendMessage(sender, localization.get("usage.ipanic"));
                } else {
                    instance.sendMessage(sender, localization.get("command.ipanic",
                            localization.get(instance.getCore().getRestAPI().togglePanicMode() ? "utils.activated" : "utils.deactivated")));
                }
                break;
            }
            case "setgameshield": {
                if(args.length == 1){
                    instance.sendMessage(sender, localization.get("select.gameshield"));

                    HashMap<String, String> gameshields = instance.getCore().getRestAPI().getGameshields();

                    for (Object set: gameshields.keySet()) {
                        instance.sendMessage(sender, "§5"+ set.toString() + localization.get("utils.click"),
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setgameshield " + gameshields.get(set.toString())),
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Collections.singletonList(new Text(localization.get("hover.gameshield", set, gameshields.get(set.toString()))))));
                    }
                } else if (args.length == 2) {
                    Config.setGameShieldID(args[1]);

                    instance.sendMessage(sender, localization.get("set.gameshield", args[1]));
                    instance.sendMessage(sender, localization.get("select.backend"));

                    HashMap<String, String> backend = instance.getCore().getRestAPI().getBackends();

                    for (Object set: backend.keySet()) {
                        instance.sendMessage(sender, "§5" +  backend.get(set.toString()) + localization.get("utils.click"),
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setbackend " + set),
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Collections.singletonList(new Text(localization.get("hover.backend", backend.get(set.toString()), set)))));
                    }
                }else {
                    instance.sendMessage(sender, localization.get("usage.setgameshield"));
                }
                break;
            }
            case "setbackend": {
                if(args.length == 1){
                    instance.sendMessage(sender, localization.get("select.backend"));

                    HashMap<String, String> backend = instance.getCore().getRestAPI().getBackends();

                    for (Object set: backend.keySet()) {
                        instance.sendMessage(sender, "§5" +  backend.get(set.toString()) + localization.get("utils.click"),
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setbackend " + set),
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Collections.singletonList(new Text(localization.get("hover.backend", backend.get(set.toString()), set)))));
                    }
                } else if (args.length == 2) {
                    Config.setBackendID(args[1]);

                    instance.sendMessage(sender, localization.get("set.backend", args[1]));
                    instance.getCore().getRestAPI().testCredentials();

                    if(ChatListener.PLAYER_IN_SETUP.remove(sender)){
                        instance.sendMessage(sender, localization.get("setup.finished"));
                    }
                }else {
                    instance.sendMessage(sender, localization.get("usage.setbackend"));
                }
                break;
            }
            default: {
                showHelp(sender);
            }
        }
    }

    private void showHelp(CommandSender sender){
        instance.sendMessage(sender, localization.get("available.commands"));
        instance.sendMessage(sender, " - /np setup");
        instance.sendMessage(sender, " - /np ipanic");
        instance.sendMessage(sender, " - /np setgameshield");
        instance.sendMessage(sender, " - /np setbackend");
    }
}
