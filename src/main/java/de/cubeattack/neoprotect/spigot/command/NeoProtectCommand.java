package de.cubeattack.neoprotect.spigot.command;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import de.cubeattack.neoprotect.spigot.listener.ChatListener;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class NeoProtectCommand implements CommandExecutor {

    private final NeoProtectSpigot instance;
    private final Localization localization;

    public NeoProtectCommand(NeoProtectSpigot instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player)){
            instance.sendMessage(sender, localization.get("console.command"));
            return true;
        }

        Player player = ((Player) sender);

        if(args.length == 0){
            showHelp(player);
            return true;
        }

        if(!instance.getCore().isSetup() & !args[0].equals("setup") & !args[0].equals("setgameshield") & !args[0].equals("setbackend") ){
            instance.sendMessage(player, localization.get("setup.command.required"));
            return true;
        }

        switch (args[0]) {
            case "setup": {
                ChatListener.PLAYER_IN_SETUP.add(player);
                instance.sendMessage(player, localization.get("command.setup") + localization.get("utils.click"),
                        new ClickEvent(ClickEvent.Action.OPEN_URL, "https://panel.neoprotect.net/profile"),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(localization.get("apikey.find")).create()));
                break;
            }
            case "ipanic": {
                if(args.length != 1){
                    instance.sendMessage(player, localization.get("usage.ipanic"));
                } else {
                    instance.sendMessage(player, localization.get("command.ipanic",
                            localization.get(instance.getCore().getRestAPI().togglePanicMode() ? "utils.activated" : "utils.deactivated")));
                }
                break;
            }
            case "setgameshield": {
                if(args.length == 1){
                    instance.sendMessage(player, localization.get("select.gameshield"));

                    HashMap<String, String> gameshields = instance.getCore().getRestAPI().getGameshields();

                    for (Object set : gameshields.keySet()) {
                        instance.sendMessage(player, "§5" + gameshields.get(set.toString()) + localization.get("utils.click"),
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setgameshield " + set),
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(localization.get("hover.gameshield", gameshields.get(set.toString()), set)).create()));
                    }
                } else if (args.length == 2) {
                    Config.setGameShieldID(args[1]);

                    instance.sendMessage(player, localization.get("set.gameshield", args[1]));
                    instance.sendMessage(player, localization.get("select.backend"));

                    HashMap<String, String> backend = instance.getCore().getRestAPI().getBackends();

                    for (Object set: backend.keySet()) {
                        instance.sendMessage(player, "§5" + backend.get(set.toString()) + localization.get("utils.click"),
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setbackend " + set),
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(localization.get("hover.backend", backend.get(set.toString()), set)).create()));
                    }
                }else {
                    instance.sendMessage(player, localization.get("usage.setgameshield"));
                }
                break;
            }
            case "setbackend": {
                if(args.length == 1){
                    instance.sendMessage(player, localization.get("select.backend"));

                    HashMap<String, String> backend = instance.getCore().getRestAPI().getBackends();

                    for (Object set: backend.keySet()) {
                        instance.sendMessage(player, "§5" + backend.get(set.toString()) + localization.get("utils.click"),
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/np setbackend " + set),
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(localization.get("hover.backend", backend.get(set.toString()), set)).create()));
                    }
                } else if (args.length == 2) {
                    Config.setBackendID(args[1]);

                    instance.sendMessage(player,  localization.get("set.backend", args[1]));
                    instance.getCore().getRestAPI().tests();

                    if(ChatListener.PLAYER_IN_SETUP.remove(sender)){
                        instance.getStartup().runProxyProtocol(instance);
                        instance.sendMessage(player, localization.get("setup.finished", args[0]));
                    }
                }else {
                    instance.sendMessage(player, localization.get("usage.setbackend"));
                }
                break;
            }
            default: {
                showHelp(player);
            }
        }
        return false;
    }

    private void showHelp(CommandSender sender){
        instance.sendMessage(sender, localization.get("available.commands"));
        instance.sendMessage(sender, " - /np setup");
        instance.sendMessage(sender, " - /np ipanic");
        instance.sendMessage(sender, " - /np setgameshield");
        instance.sendMessage(sender, " - /np setbackend");
    }
}
