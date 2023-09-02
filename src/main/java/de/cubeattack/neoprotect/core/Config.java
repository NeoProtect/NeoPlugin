package de.cubeattack.neoprotect.core;

import de.cubeattack.api.util.FileUtils;
import de.cubeattack.api.util.versioning.VersionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class Config {

    private static String APIKey;
    private static String language;
    private static boolean proxyProtocol;
    private static String gameShieldID;
    private static String backendID;
    private static String geyserBackendID;
    private static boolean updateIP;
    private static boolean debugMode;
    private static String geyserServerIP;
    private static String updateSetting;

    private static Core core;
    private static FileUtils fileUtils;

    public static void loadConfig(Core core, FileUtils config) {

        Config.core = core;
        fileUtils = config;

        APIKey = config.getString("APIKey", "");
        language = config.getString("defaultLanguage", Locale.ENGLISH.toLanguageTag());
        proxyProtocol = config.getBoolean("ProxyProtocol", true);
        gameShieldID = config.getString("gameshield.serverId", "");
        backendID = config.getString("gameshield.backendId", "");
        geyserBackendID = config.getString("gameshield.geyserBackendId", "");
        updateIP = config.getBoolean("gameshield.autoUpdateIP", false);
        debugMode = config.getBoolean("DebugMode", false);
        geyserServerIP = config.getString("geyserServerIP", "127.0.0.1");

        if (APIKey.length() != 64) {
            core.severe("Failed to load API-Key. Key is null or not valid");
            return;
        }
        if (gameShieldID.isEmpty()) {
            core.severe("Failed to load GameshieldID. ID is null");
            return;
        }
        if (backendID.isEmpty()) {
            core.severe("Failed to load BackendID. ID is null");
            return;
        }

        core.info("API-Key loaded successful '" + "******************************" + APIKey.substring(32) + "'");
        core.info("GameshieldID loaded successful '" + gameShieldID + "'");
        core.info("BackendID loaded successful '" + backendID + "'");
    }

    public static String getAPIKey() {
        return APIKey;
    }

    public static String getLanguage() {
        return language;
    }

    public static String getGameShieldID() {
        return gameShieldID;
    }

    public static String getBackendID() {
        return backendID;
    }

    public static String getGeyserBackendID() {
        return geyserBackendID;
    }

    public static boolean isProxyProtocol() {
        return proxyProtocol;
    }

    public static boolean isUpdateIP() {
        return updateIP;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static String getGeyserServerIP() {
        return geyserServerIP;
    }

    public static VersionUtils.UpdateSetting getAutoUpdaterSettings() {
        return VersionUtils.UpdateSetting.getByNameOrDefault(updateSetting);
    }

    public static void setAPIKey(String key) {
        fileUtils.set("APIKey", key);
        fileUtils.save();
        APIKey = key;
    }

    public static void setGameShieldID(String id) {
        fileUtils.set("gameshield.serverId", id);
        fileUtils.save();
        gameShieldID = id;
    }

    public static void setBackendID(String id) {
        fileUtils.set("gameshield.backendId", id);
        fileUtils.save();
        backendID = id;
    }

    public static void setGeyserBackendID(String id) {
        fileUtils.set("gameshield.geyserBackendId", id);
        fileUtils.save();
        geyserBackendID = id;
    }

    public static void addAutoUpdater(String plan) {

        if (plan.equals("Basic") || plan.equalsIgnoreCase("Premium")) {
            fileUtils.remove("AutoUpdater");
        } else if (!fileUtils.getConfig().isSet("AutoUpdater")) {
            fileUtils.getConfig().set("AutoUpdater", "ENABLED");
        }

        List<String> description = new ArrayList<>();
        description.add("This setting is only for paid costumer and allow you to disable the AutoUpdater");
        description.add("'ENABLED'  (Recommended/Default) Update/Downgrade plugin to the current version  ");
        description.add("'DISABLED' AutoUpdater just disabled");
        description.add("'DEV'      Only update to the latest version (Please never use this)");

        fileUtils.getConfig().setComments("AutoUpdater", description);

        fileUtils.save();
        updateSetting = fileUtils.getString("AutoUpdater", "ENABLED");
    }
}


