package de.cubeattack.neoprotect.core;

import java.util.logging.Logger;

public interface NeoProtectPlugin {

    void sendMessage(Object sender, String text);
    void sendMessage(Object sender, String text, Object clickEvent, Object hoverEvent);

    void sendAdminMessage(String text, Object clickEvent, Object hoverEvent);

    Logger getLogger();
}
