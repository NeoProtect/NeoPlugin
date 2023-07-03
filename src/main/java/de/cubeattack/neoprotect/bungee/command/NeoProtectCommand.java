package de.cubeattack.neoprotect.bungee.command;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.command.NeoProtectExecutor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NeoProtectCommand extends net.md_5.bungee.api.plugin.Command {

    private final NeoProtectPlugin instance;
    private final Localization localization;
    public NeoProtectCommand(NeoProtectPlugin instance, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(!(sender instanceof ProxiedPlayer)) {
            instance.sendMessage(sender, localization.get("console.command"));
            return;
        }

        new NeoProtectExecutor.ExecutorBuilder()
                .neoProtectPlugin(instance)
                .sender(sender)
                .args(args)
                .executeCommand();
    }
}
