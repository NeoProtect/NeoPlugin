package de.cubeattack.neoprotect.core;

import java.util.logging.Logger;

public interface NeoProtectPlugin {

    void sendMessage(Object sender, String text);
    void sendMessage(Object sender, String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);

    void sendAdminMessage(Permission permission,String text, String clickAction, String clickMsg, String hoverAction, String hoverMsg);

    Core getCore();
    Logger getLogger();
    String getVersion();
}
