package de.cubeattack.neoprotect.core.model;

import java.net.SocketAddress;

public class KeepAliveResponseKey {

    private final SocketAddress address;
    private final long id;

    public KeepAliveResponseKey(SocketAddress address, long id) {
        this.address = address;
        this.id = id;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getId() {
        return id;
    }
}
