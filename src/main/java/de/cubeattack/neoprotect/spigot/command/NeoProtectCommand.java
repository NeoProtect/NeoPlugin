package de.cubeattack.neoprotect.spigot.command;

import de.cubeattack.api.util.JavaUtils;
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

    public NeoProtectCommand(NeoProtectSpigot instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        new NeoProtectExecutor.ExecutorBuilder()
                .viaConsole(!(sender instanceof Player))
                .local(JavaUtils.javaVersionCheck() != 8 ? ((sender instanceof Player) ? Locale.forLanguageTag(((Player) sender).getLocale()) : Locale.ENGLISH) : Locale.ENGLISH)
                .neoProtectPlugin(instance)
                .sender(sender)
                .args(args)
                .executeCommand();

        return false;
    }
}
