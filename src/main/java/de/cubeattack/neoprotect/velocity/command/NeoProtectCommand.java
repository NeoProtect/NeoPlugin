package de.cubeattack.neoprotect.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

import java.util.ArrayList;
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

        if(!(invocation.source() instanceof Player)) {
            instance.sendMessage(invocation.source(), localization.get("console.command"));
            return;
        }

        new NeoProtectExecutor.ExecutorBuilder()
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
        return CompletableFuture.supplyAsync(() ->{
            List<String> list = new ArrayList<>();
            List<String> completorList = new ArrayList<>();
            String[] args = invocation.arguments();


            if (args.length == 2) {
                System.out.println(args[0]);
                if(args[0].equalsIgnoreCase("debugtool")){
                    for (int i = 10; i <= 100; i = i + 10) {
                        completorList.add(String.valueOf(i));
                    }
                    completorList.add("cancel");
                }
                return completorList;
            }

            if (args.length > 1) {
                return completorList;
            }

            list.add("setup");

            if(instance.getCore().isSetup()) {
                list.add("setgameshield");
                list.add("setbackend");
                list.add("analytics");
                list.add("debugTool");
                list.add("ipanic");
            }

            for (String tab : list) {

                if(args.length == 0){
                    completorList.add(tab);
                    continue;
                }

                if(tab.toLowerCase().startsWith(args[args.length-1].toLowerCase())){
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
