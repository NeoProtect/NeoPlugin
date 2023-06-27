package de.cubeattack.neoprotect.core;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.utils.FileUtils;
import de.cubeattack.api.utils.VersionUtils;
import de.cubeattack.neoprotect.core.request.RestAPIRequests;

import java.io.File;
import java.util.Locale;

@SuppressWarnings("unused")
public class Core {

    @SuppressWarnings("FieldCanBeLocal")
    private final String prefix = "§8[§5NEOPROTECT§8] §b";
    private final RestAPIRequests restAPIRequests;
    private final NeoProtectPlugin plugin;
    private final Localization localization;

    public Core(NeoProtectPlugin plugin) {
        this.plugin = plugin;

        LogManager.getLogger().setLogger(plugin.getLogger());
        VersionUtils.checkVersion("NeoProtect", "NeoPlugin", plugin.getVersion());
        FileUtils config = new FileUtils(Core.class.getResourceAsStream("/config.yml"), "plugins/NeoProtect", "config.yml", false);
        FileUtils languageEN = new FileUtils(Core.class.getResourceAsStream("/language_en.properties"), "plugins/NeoProtect/languages", "language_en.properties", true);
        FileUtils languageDE = new FileUtils(Core.class.getResourceAsStream("/language_de.properties"), "plugins/NeoProtect/languages", "language_de.properties", true);

        Config.loadConfig(this, config);

        this.localization = new Localization("language", Locale.forLanguageTag(Config.getLanguage()), new File("plugins/NeoProtect/languages/"));

        restAPIRequests = new RestAPIRequests(this);
    }

    public void debug(String output){
        LogManager.getLogger().debug(output);
    }
    public void info(String output){
        LogManager.getLogger().info(output);
    }
    public void warn(String output){
        LogManager.getLogger().warn(output);
    }
    public void severe(String output){
        LogManager.getLogger().error(output);
    }

    public String getPrefix() {
        return prefix;
    }

    public NeoProtectPlugin getPlugin() {
        return plugin;
    }

    public Localization getLocalization() {
        return localization;
    }

    public RestAPIRequests getRestAPI() {
        return restAPIRequests;
    }

    public boolean isSetup() {
        return restAPIRequests.isSetup();
    }

}
