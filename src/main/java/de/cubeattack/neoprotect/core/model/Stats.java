package de.cubeattack.neoprotect.core.model;

import de.cubeattack.neoprotect.core.NeoProtectPlugin;

@SuppressWarnings("unused")
public class Stats {

    private final String serverType;
    private final String serverVersion;
    private final String serverName;
    private final String javaVersion;
    private final String osName;
    private final String osArch;
    private final String osVersion;
    private final String pluginVersion;
    private final String versionStatus;
    private final String versionError;
    private final String updateSetting;
    private final String neoProtectPlan;
    private final String serverPlugins;

    private final int playerAmount;
    private final int managedServers;
    private final int coreCount;

    private final boolean onlineMode;
    private final boolean proxyProtocol;

    public Stats(NeoProtectPlugin.PluginType serverType, String serverVersion, String serverName, String javaVersion, String osName, String osArch, String osVersion, String pluginVersion, String versionStatus, String versionError, String updateSetting, String neoProtectPlan, String serverPlugins, int playerAmount, int managedServers, int coreCount, boolean onlineMode, boolean proxyProtocol) {
        this.serverType = serverType.name().toLowerCase();
        this.serverVersion = serverVersion;
        this.serverName = serverName;
        this.javaVersion = javaVersion;
        this.osName = osName;
        this.osArch = osArch;
        this.osVersion = osVersion;
        this.pluginVersion = pluginVersion;
        this.versionStatus = versionStatus;
        this.versionError = versionError;
        this.updateSetting = updateSetting;
        this.neoProtectPlan = neoProtectPlan;
        this.serverPlugins = serverPlugins;
        this.playerAmount = playerAmount;
        this.managedServers = managedServers;
        this.coreCount = coreCount;
        this.onlineMode = onlineMode;
        this.proxyProtocol = proxyProtocol;
    }

    public String getServerType() {
        return serverType;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getServerName() {
        return serverName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public String getVersionStatus() {
        return versionStatus;
    }

    public String getVersionError() {
        return versionError;
    }

    public String getUpdateSetting() {
        return updateSetting;
    }

    public String getNeoProtectPlan() {
        return neoProtectPlan;
    }

    public String getServerPlugins() {
        return serverPlugins;
    }

    public int getPlayerAmount() {
        return playerAmount;
    }

    public int getManagedServers() {
        return managedServers;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public boolean isProxyProtocol() {
        return proxyProtocol;
    }
}
