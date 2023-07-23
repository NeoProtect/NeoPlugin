package de.cubeattack.neoprotect.core;

import de.cubeattack.api.util.FileUtils;
import de.cubeattack.api.util.VersionUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Config {

    private static String APIKey;
    private static String language;
    private static boolean proxyProtocol;
    private static String GameShieldID;
    private static String BackendID;
    private static boolean updateIP;
    private static boolean debugMode;
    private static String updateSetting;

    private static Core core;
    private static FileUtils fileUtils;

    public static void loadConfig(Core core, FileUtils config) {

        Config.core = core;
        fileUtils = config;

        APIKey = config.getString("APIKey", "");
        language = config.getString("Language", "en-US");
        proxyProtocol = config.getBoolean("ProxyProtocol", true);
        GameShieldID = config.getString("gameshield.serverId", "");
        BackendID = config.getString("gameshield.backendId", "");
        updateIP = config.getBoolean("gameshield.autoUpdateIP", false);
        debugMode = config.getBoolean("DebugMode", false);

        if (APIKey.length() != 64) {
            core.severe("Failed to load API-Key. Key is null or not valid");
            return;
        }
        if (GameShieldID.equals("")) {
            core.severe("Failed to load GameshieldID. ID is null");
            return;
        }
        if (BackendID.equals("")) {
            core.severe("Failed to load BackendID. ID is null");
            return;
        }

        core.info("API-Key loaded successful '" + "******************************" + APIKey.substring(32) + "'");
        core.info("GameshieldID loaded successful '" + GameShieldID + "'");
        core.info("BackendID loaded successful '" + BackendID + "'");
    }

    public static String getAPIKey() {
        return APIKey;
    }

    public static String getLanguage() {
        return language;
    }

    public static String getGameShieldID() {
        return GameShieldID;
    }

    public static String getBackendID() {
        return BackendID;
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

    public static VersionUtils.UpdateSetting isAutoUpdater() {
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
        GameShieldID = id;
    }

    public static void setBackendID(String id) {
        fileUtils.set("gameshield.backendId", id);
        fileUtils.save();
        BackendID = id;
    }

    public static void addAutoUpdater(boolean basicPlan) {

        if (basicPlan) {
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


