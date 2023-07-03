package de.cubeattack.neoprotect.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import de.cubeattack.neoprotect.velocity.NeoProtectVelocity;

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
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("neoprotect.admin");
    }
}
