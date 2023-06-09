package de.cubeattack.neoprotect.core;

import java.util.logging.Logger;

public interface NeoProtectPlugin {

    void sendMessage(Object receiver, String text);
    void sendMessage(Object receiver, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);
    void sendAdminMessage(Permission permission,String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);

    long sendKeepAliveMessage(long id);
    void sendKeepAliveMessage(Object receiver, long id);

    Core getCore();
    Logger getLogger();
    String getVersion();
    PluginType getPluginType();

    enum PluginType {
        SPIGOT,
        VELOCITY,
        BUNGEECORD,
    }
}
