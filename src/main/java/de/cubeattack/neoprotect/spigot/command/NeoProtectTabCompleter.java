package de.cubeattack.neoprotect.spigot.command;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NeoProtectTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        List<String> completorList = new ArrayList<>();

        if (args.length == 1) {
            list.add("setup");
            list.add("setgameshield");
            list.add("setbackend");
            list.add("ipanic");
        }

        for (String tab : list) {
            if(tab.toLowerCase().startsWith(args[args.length-1].toLowerCase())){
                completorList.add(tab);
            }
        }
        return completorList;
    }
}
