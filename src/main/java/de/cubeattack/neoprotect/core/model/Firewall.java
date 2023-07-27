package de.cubeattack.neoprotect.core.model;

public class Firewall {
    String ip;
    String id;

    public Firewall(String ip, String id) {
        this.ip = ip;
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public String getId() {
        return id;
    }
}
