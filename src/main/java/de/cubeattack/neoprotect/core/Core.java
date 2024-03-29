package de.cubeattack.neoprotect.core;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.FileUtils;
import de.cubeattack.api.util.versioning.VersionUtils;
import de.cubeattack.neoprotect.core.model.debugtool.DebugPingResponse;
import de.cubeattack.neoprotect.core.model.debugtool.KeepAliveResponseKey;
import de.cubeattack.neoprotect.core.request.RestAPIRequests;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Core {

    @SuppressWarnings("FieldCanBeLocal")
    private final String prefix = "§8[§bNeo§3Protect§8] §7";
    private final UUID maintainerOnlineUUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");
    private final UUID maintainerOfflineeUUID = UUID.fromString("8c07bf89-9c8f-304c-9216-4666b670223b");
    private final RestAPIRequests restAPIRequests;
    private final NeoProtectPlugin plugin;
    private final Localization localization;
    private final boolean isJPremiumInstalled;
    private final List<Object> playerInSetup = new ArrayList<>();
    private final List<String> directConnectWhitelist= new ArrayList<>();
    private final ConcurrentHashMap<KeepAliveResponseKey, Long> pingMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Timestamp> timestampsMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ArrayList<DebugPingResponse>> debugPingResponses = new ConcurrentHashMap<>();

    private VersionUtils.Result versionResult;
    private boolean isDebugRunning = false;

    public Core(NeoProtectPlugin plugin) {
        LogManager.getLogger().setLogger(plugin.getLogger());

        this.plugin = plugin;
        this.isJPremiumInstalled = plugin.getPlugins().contains("JPremium");
        this.versionResult = VersionUtils.checkVersion("NeoProtect", "NeoPlugin", "v" + plugin.getPluginVersion(), VersionUtils.UpdateSetting.DISABLED).message();

        FileUtils config = new FileUtils(Core.class.getResourceAsStream("/config.yml"), "plugins/NeoProtect", "config.yml", false);
        FileUtils languageEN = new FileUtils(Core.class.getResourceAsStream("/language_en.properties"), "plugins/NeoProtect/languages", "language_en.properties", true);
        FileUtils languageDE = new FileUtils(Core.class.getResourceAsStream("/language_de.properties"), "plugins/NeoProtect/languages", "language_de.properties", true);
        FileUtils languageRU = new FileUtils(Core.class.getResourceAsStream("/language_ru.properties"), "plugins/NeoProtect/languages", "language_ru.properties", true);
        FileUtils languageUA = new FileUtils(Core.class.getResourceAsStream("/language_ua.properties"), "plugins/NeoProtect/languages", "language_ua.properties", true);

        Config.loadConfig(this, config);

        this.restAPIRequests = new RestAPIRequests(this);
        this.localization = new Localization("language", Locale.US, new File("plugins/NeoProtect/languages/"));

        VersionUtils.updateToLatestVersion(versionResult, Config.getAutoUpdaterSettings(), 10, result -> versionResult = result);
    }

    public void debug(String output) {
        if (Config.isDebugMode()) ((Logger) LogManager.getLogger().logger).log(Level.SEVERE, output);
    }

    public void info(String output) {
        LogManager.getLogger().info(output);
    }

    public void warn(String output) {
        LogManager.getLogger().warn(output);
    }

    public void severe(String output) {
        LogManager.getLogger().error(output);
    }
    public void severe(String output, Throwable t) {
        LogManager.getLogger().error(output, t);
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isDebugRunning() {
        return isDebugRunning;
    }

    public void setDebugRunning(boolean debugRunning) {
        isDebugRunning = debugRunning;
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

    public List<Object> getPlayerInSetup() {
        return playerInSetup;
    }

    public List<String> getDirectConnectWhitelist() {
        return directConnectWhitelist;
    }

    public ConcurrentHashMap<KeepAliveResponseKey, Long> getPingMap() {
        return pingMap;
    }

    public ConcurrentHashMap<Long, Timestamp> getTimestampsMap() {
        return timestampsMap;
    }

    public ConcurrentHashMap<String, ArrayList<DebugPingResponse>> getDebugPingResponses() {
        return debugPingResponses;
    }

    public VersionUtils.Result getVersionResult() {
        return versionResult;
    }

    public void setVersionResult(VersionUtils.Result versionResult) {
        this.versionResult = versionResult;
    }

    public boolean isPlayerMaintainer(UUID playerUUID, boolean onlineMode) {
        return ((onlineMode || isJPremiumInstalled) && maintainerOnlineUUID.equals(playerUUID)) || ((!onlineMode || isJPremiumInstalled) && maintainerOfflineeUUID.equals(playerUUID));
    }
}
