package de.cubeattack.neoprotect.spigot.command;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.executor.NeoProtectExecutor;
import de.cubeattack.neoprotect.spigot.NeoProtectSpigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;


public class NeoProtectCommand implements CommandExecutor {

    private final NeoProtectSpigot instance;
    private final Localization localization;

    public NeoProtectCommand(NeoProtectSpigot instance) {
        this.instance = instance;
        this.localization = instance.getCore().getLocalization();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            instance.sendMessage(sender, localization.get(Locale.getDefault(), "console.command"));
            return true;
        }

        new NeoProtectExecutor.ExecutorBuilder()
                .local(((Player) sender).locale())
                .neoProtectPlugin(instance)
                .sender(sender)
                .args(args)
                .executeCommand();

        return false;
    }
}
