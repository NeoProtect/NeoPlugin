package de.cubeattack.neoprotect.core.model;

public class Backend {

    private final String id;
    private String ip;
    private final String port;
    private final boolean geyser;

    public Backend(String id, String ip, String port, boolean geyser) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.geyser = geyser;
    }

    public boolean compareById(String otherId) {
        return id.equals(otherId);
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public boolean isGeyser() {
        return geyser;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }
}
