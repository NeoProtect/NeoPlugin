package de.cubeattack.neoprotect.core;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.FileUtils;
import de.cubeattack.api.util.VersionUtils;
import de.cubeattack.neoprotect.core.model.DebugPingResponse;
import de.cubeattack.neoprotect.core.model.KeepAliveResponseKey;
import de.cubeattack.neoprotect.core.request.RestAPIRequests;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class Core {

    @SuppressWarnings("FieldCanBeLocal")
    private final String prefix = "§8[§bNeo§3Protect§8] §7";
    private final UUID maintainerUUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");
    private final RestAPIRequests restAPIRequests;
    private final NeoProtectPlugin plugin;
    private final Localization localization;

    private final List<Object> PLAYER_IN_SETUP = new ArrayList<>();
    private final ConcurrentHashMap<KeepAliveResponseKey, Long> pingMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Timestamp> timestampsMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DebugPingResponse> debugPingResponses = new ConcurrentHashMap<>();

    private final ExecutorService executorService;
    private VersionUtils.Result versionResult;

    public Core(NeoProtectPlugin plugin) {
        LogManager.getLogger().setLogger(plugin.getLogger());

        this.plugin = plugin;
        this.executorService = Executors.newSingleThreadExecutor();
        this.versionResult = VersionUtils.checkVersion("NeoProtect", "NeoPlugin", "v" + plugin.getVersion()).message();

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

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public UUID getMaintainerUUID() {
        return maintainerUUID;
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

    public List<Object> getPlayerInSetup() {
        return PLAYER_IN_SETUP;
    }

    public ConcurrentHashMap<KeepAliveResponseKey, Long> getPingMap() {
        return pingMap;
    }

    public ConcurrentHashMap<Long, Timestamp> getTimestampsMap() {
        return timestampsMap;
    }

    public ConcurrentHashMap<String, DebugPingResponse> getDebugPingResponses() {
        return debugPingResponses;
    }

    public VersionUtils.Result getVersionResult() {
        return versionResult;
    }

    public void setVersionResult(VersionUtils.Result versionResult) {
        this.versionResult = versionResult;
    }
}
