package de.cubeattack.neoprotect.bungee.command;

import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NeoProtectCommand extends Command implements TabExecutor {

    private final NeoProtectPlugin instance;

    public NeoProtectCommand(NeoProtectPlugin instance, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        new NeoProtectExecutor.ExecutorBuilder()
                .viaConsole(!(sender instanceof ProxiedPlayer))
                .local((sender instanceof ProxiedPlayer) ? ((ProxiedPlayer) sender).getLocale() : Locale.ENGLISH)
                .neoProtectPlugin(instance)
                .sender(sender)
                .args(args)
                .executeCommand();
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        List<String> list = new ArrayList<>();
        List<String> completorList = new ArrayList<>();

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("debugtool")) {
                for (int i = 10; i <= 100; i = i + 10) {
                    list.add(String.valueOf(i));
                }
                list.add("cancel");
            }

            if ((args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("blacklist"))) {
                list.add("add");
                list.add("remove");
            }

            if (args[0].equalsIgnoreCase("toggle")) {
                list.add("antiVPN");
                list.add("anycast");
                list.add("motdCache");
                list.add("blockForge");
                list.add("ipWhitelist");
                list.add("ipBlacklist");
                list.add("secureProfiles");
                list.add("advancedAntiBot");
            }
        }

        if (args.length <= 1) {

            list.add("setup");

            if (instance.getCore().isSetup()) {
                list.add("directConnectWhitelist");
                list.add("setgameshield");
                list.add("setbackend");
                list.add("analytics");
                list.add("debugTool");
                list.add("whitelist");
                list.add("blacklist");
                list.add("ipanic");
                list.add("toggle");
            }
        }

        for (String tab : list) {

            if (args.length == 0) {
                completorList.add(tab);
                continue;
            }

            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                completorList.add(tab);
            }
        }
        return completorList;
    }
}
