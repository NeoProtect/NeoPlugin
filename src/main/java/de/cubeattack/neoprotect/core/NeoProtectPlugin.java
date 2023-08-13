package de.cubeattack.neoprotect.core;

import de.cubeattack.neoprotect.core.model.Stats;

import java.util.ArrayList;
import java.util.logging.Logger;

public interface NeoProtectPlugin {

    void sendMessage(Object receiver, String text);

    void sendMessage(Object receiver, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);

    void sendAdminMessage(Permission permission, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);

    long sendKeepAliveMessage(long id);

    void sendKeepAliveMessage(Object receiver, long id);

    Core getCore();

    Stats getStats();

    Logger getLogger();

    ArrayList<String> getPlugins();

    PluginType getPluginType();

    String getPluginVersion();

    enum PluginType {
        SPIGOT,
        VELOCITY,
        BUNGEECORD,
    }
}
