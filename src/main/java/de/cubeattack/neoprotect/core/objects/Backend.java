package de.cubeattack.neoprotect.core.objects;

public class Backend {

    private final String id;
    private String ip;
    private final String port;

    public Backend(String id, String ip, String port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
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

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }
}
