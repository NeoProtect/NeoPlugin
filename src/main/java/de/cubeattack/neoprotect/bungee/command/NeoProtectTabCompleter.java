package de.cubeattack.neoprotect.bungee.command;

import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.Collections;

public class NeoProtectTabCompleter implements Listener {

    private final NeoProtectPlugin instance;

    public NeoProtectTabCompleter(NeoProtectPlugin instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTabComplete(TabCompleteEvent event) {

        String cursor = event.getCursor().toLowerCase();
        ArrayList<String> commands = new ArrayList<>();
        ArrayList<String> tabListOne = new ArrayList<>();

        commands.add("/np");
        commands.add("/neoprotect");
        tabListOne.add("setup");

        if(instance.getCore().isSetup()){
            tabListOne.add("ipanic");
            tabListOne.add("debugTool");
            tabListOne.add("analytics");
            tabListOne.add("setgameshield");
            tabListOne.add("setbackend");
        }

        event.getSuggestions().addAll(completer(true, cursor, commands, tabListOne));
    }


    @SafeVarargs
    public static ArrayList<String> completer(boolean sort, String cursor, ArrayList<String> cmds, ArrayList<String>... tabLists) {

        ArrayList<String> completions = new ArrayList<>();

        int pos = 0;
        String[] splitCursor = cursor.split(" ");

        for (ArrayList<String> tabList : tabLists) {
            pos++;

            if (sort) {
                Collections.sort(cmds);
                Collections.sort(tabList);
            }

            for (String cmd : cmds) {
                
                if (!cursor.startsWith(cmd + " ")) {
                    continue;
                }

                if (splitCursor.length == pos + 1) {
                    String current = splitCursor[splitCursor.length - 1];
                    for (String tab : tabList) {
                        if (tab.toLowerCase().startsWith(current) && !cursor.endsWith(" ")) {
                            completions.add(tab);
                        }
                    }
                } else if (cursor.endsWith(" ") && splitCursor.length == (pos)) {
                    if (pos >= 2 && !tabLists[pos - 2].contains(splitCursor[pos - 1])) {
                        continue;
                    }
                    completions.addAll(tabList);
                }
            }
        }
        return completions;
    }
}