package de.cubeattack.neoprotect.spigot.command;

import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NeoProtectTabCompleter implements TabCompleter {

    private final NeoProtectPlugin instance;

    public NeoProtectTabCompleter(NeoProtectPlugin instance) {
        this.instance = instance;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        List<String> completorList = new ArrayList<>();

        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            completorList.add("antiVPN");
            completorList.add("anycast");
            completorList.add("motdCache");
            completorList.add("blockForge");
            completorList.add("ipWhitelist");
            completorList.add("ipBlacklist");
            completorList.add("secureProfiles");
            completorList.add("advancedAntiBot");
            return completorList;
        }

        if (args.length >= 2 && (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("blacklist"))) {
            completorList.add("add");
            completorList.add("remove");
        }

        if (args.length != 1) {
            return completorList;
        }

        list.add("setup");

        if (instance.getCore().isSetup()) {
            list.add("directConnectWhitelist");
            list.add("setgameshield");
            list.add("setbackend");
            list.add("analytics");
            list.add("whitelist");
            list.add("blacklist");
            list.add("ipanic");
            list.add("toggle");
        }

        for (String tab : list) {
            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                completorList.add(tab);
            }
        }
        return completorList;
    }
}
