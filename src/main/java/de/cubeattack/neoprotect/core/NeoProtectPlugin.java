package de.cubeattack.neoprotect.core;

import de.cubeattack.neoprotect.core.model.KeepAliveResponseKey;

import java.util.logging.Logger;

public interface NeoProtectPlugin {

    void sendMessage(Object sender, String text);
    void sendMessage(Object sender, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);

    void sendAdminMessage(Permission permission,String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);

    KeepAliveResponseKey sendKeepAliveMessage(Object sender, long id);
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
