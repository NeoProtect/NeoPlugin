package de.cubeattack.neoprotect.core.model;

@SuppressWarnings("unused")
public class Stats {

    private final boolean onlineMode;

    private final int playerAmount;
    private final int managedServers;
    private final int coreCount;

    private final String serverVersion;
    private final String serverName;
    private final String javaVersion;
    private final String osName;
    private final String osArch;
    private final String osVersion;
    private final String pluginVersion;

    public Stats(boolean onlineMode, int playerAmount, int managedServers, int coreCount, String serverVersion, String serverName, String javaVersion, String osName, String osArch, String osVersion, String pluginVersion) {
        this.onlineMode = onlineMode;
        this.playerAmount = playerAmount;
        this.managedServers = managedServers;
        this.coreCount = coreCount;
        this.serverVersion = serverVersion;
        this.serverName = serverName;
        this.javaVersion = javaVersion;
        this.osName = osName;
        this.osArch = osArch;
        this.osVersion = osVersion;
        this.pluginVersion = pluginVersion;
    }

    public boolean isOnlineMode() {
        return onlineMode;
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
}
