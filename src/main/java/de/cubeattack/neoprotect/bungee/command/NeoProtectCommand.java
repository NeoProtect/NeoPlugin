package de.cubeattack.neoprotect.bungee.command;

import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Locale;

public class NeoProtectCommand extends Command {

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
}
