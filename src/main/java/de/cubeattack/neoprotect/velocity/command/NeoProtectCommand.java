package de.cubeattack.neoprotect.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class NeoProtectCommand implements SimpleCommand {

    private final NeoProtectVelocity instance;

    public NeoProtectCommand(NeoProtectVelocity instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        new NeoProtectExecutor.ExecutorBuilder()
                .viaConsole(!(invocation.source() instanceof Player))
                .local((invocation.source() instanceof Player) ? ((Player) invocation.source()).getEffectiveLocale() : Locale.ENGLISH)
                .neoProtectPlugin(instance)
                .sender(invocation.source())
                .args(invocation.arguments())
                .executeCommand();
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> list = new ArrayList<>();
            List<String> completorList = new ArrayList<>();
            String[] args = invocation.arguments();

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
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("neoprotect.admin");
    }
}
