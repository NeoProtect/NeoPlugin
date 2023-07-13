package de.cubeattack.neoprotect.core.model.debugtool;

@SuppressWarnings("unused")
public class DebugPingResponse {
    private final long playerToProxyLatenz;
    private final long neoToProxyLatenz;
    private final long proxyToBackendLatenz;

    public DebugPingResponse(long playerPing, long neoToProxyLatenz, long proxyToBackendLatenz){
        this.playerToProxyLatenz = playerPing;
        this.neoToProxyLatenz = neoToProxyLatenz;
        this.proxyToBackendLatenz = proxyToBackendLatenz;
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
}
