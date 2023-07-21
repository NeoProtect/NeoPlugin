package de.cubeattack.neoprotect.core.model.debugtool;

import java.net.SocketAddress;

@SuppressWarnings("unused")
public class DebugPingResponse {

    private final long proxyToBackendLatenz;
    private final long playerToProxyLatenz;
    private final long neoToProxyLatenz;
    private final SocketAddress playerAddress;
    private final SocketAddress neoAddress;

    public DebugPingResponse(long playerToProxyLatenz, long neoToProxyLatenz, long proxyToBackendLatenz, SocketAddress playerAddress, SocketAddress neoAddress) {
        this.proxyToBackendLatenz = proxyToBackendLatenz;
        this.playerToProxyLatenz = playerToProxyLatenz;
        this.neoToProxyLatenz = neoToProxyLatenz;
        this.playerAddress = playerAddress;
        this.neoAddress = neoAddress;
    }

    public long getPlayerToProxyLatenz() {
        return playerToProxyLatenz;
    }

    public long getNeoToProxyLatenz() {
        return neoToProxyLatenz;
    }

    public long getProxyToBackendLatenz() {
        return proxyToBackendLatenz;
    }

    public long getPlayerToNeoLatenz() {
        return playerToProxyLatenz - neoToProxyLatenz;
    }

    public String getPlayerAddress() {
        return playerAddress.toString();
    }

    public String getNeoAddress() {
        return neoAddress.toString();
    }
}
