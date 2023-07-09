package de.cubeattack.neoprotect.core.executor;

import de.cubeattack.api.language.Localization;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.model.Backend;
import de.cubeattack.neoprotect.core.model.Gameshield;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class NeoProtectExecutor {

    private NeoProtectPlugin instance;
    private Localization localization;

    private Object sender;
    private String msg;
    private String[] args;

    private void initials(ExecutorBuilder executeBuilder) {
        this.instance = executeBuilder.getInstance();
        this.localization = instance.getCore().getLocalization();

        this.sender = executeBuilder.getSender();
        this.args = executeBuilder.getArgs();
        this.msg = executeBuilder.getMsg();
    }

    private void chatEvent(ExecutorBuilder executorBuilder){
        initials(executorBuilder);

        if (instance.getCore().getRestAPI().isAPIInvalid(msg)) {
            instance.sendMessage(sender, localization.get("apikey.invalid"));
            return;
        }

        Config.setAPIKey(msg);

        instance.sendMessage(sender, localization.get("apikey.valid"));

        gameshieldSelector(sender);
    }

    private void command(ExecutorBuilder executorBuilder) {

        initials(executorBuilder);

        if(args.length == 0){
            showHelp(sender);
            return;
        }

        if(!instance.getCore().isSetup() & !args[0].equals("setup") & !args[0].equals("setgameshield") & !args[0].equals("setbackend") ){
            instance.sendMessage(sender, localization.get("setup.command.required"));
            return;
        }

        switch (args[0].toLowerCase()) {

            case "setup": {
                setup(sender);
                break;
            }

            case "ipanic": {
                iPanic(sender, args);
                break;
            }

            case "debugping": {
                debugPing();
                break;
            }

            case "analytics": {
                analytics();
                break;
            }

            case "setgameshield": {
                if(args.length == 1){
                    gameshieldSelector(sender);
                } else if (args.length == 2) {
                    setGameshield(sender, args);
                }else {
                    instance.sendMessage(sender, localization.get("usage.setgameshield"));
                }
                break;
            }

            case "setbackend": {
                if(args.length == 1){
                    backendSelector(sender);
                } else if (args.length == 2) {
                    setBackend(sender, args);
                }else {
                    instance.sendMessage(sender, localization.get("usage.setbackend"));
                }
                break;
            }
            default: {
                showHelp(sender);
            }
        }
    }

    private void analytics(){
        JSONObject analytics =  instance.getCore().getRestAPI().getAnalytics();
        analytics.keySet().forEach(k -> instance.sendMessage(sender, k + ": " + analytics.get(k).toString().replace("onlinePlayers", "online players")));
    }

    private void setup(Object sender){
        instance.getCore().getPlayerInSetup().add(sender);
        instance.sendMessage(sender, localization.get("command.setup") + localization.get("utils.click"),
                "OPEN_URL", "https://panel.neoprotect.net/profile",
                "SHOW_TEXT", localization.get("apikey.find"));
    }

    private void iPanic(Object sender, String[] args){
        if(args.length != 1){
            instance.sendMessage(sender, localization.get("usage.ipanic"));
        } else {
            instance.sendMessage(sender, localization.get("command.ipanic",
                    localization.get(instance.getCore().getRestAPI().togglePanicMode() ? "utils.activated" : "utils.deactivated")));
        }
    }

    private void debugPing(){
        if(1==1){
            instance.sendMessage(sender , "§cthis feature is coming soon");
            return;
        }

        if(instance.getPluginType() == NeoProtectPlugin.PluginType.SPIGOT){
            instance.sendMessage(sender, "This command is only available for proxy-server");
            return;
        }

        int id = new Random().nextInt(9) * 10000 + 1337;

        instance.getCore().getPingMap().put(instance.sendKeepAliveMessage(sender, id), System.currentTimeMillis());
        instance.sendMessage(sender , "Keep-alive packets send");
    }

    private void gameshieldSelector(Object sender){
        instance.sendMessage(sender, localization.get("select.gameshield"));

        List<Gameshield> gameshieldList = instance.getCore().getRestAPI().getGameshields();

        for (Gameshield gameshield: gameshieldList) {
            instance.sendMessage(sender, "§5" + gameshield.getName() + localization.get("utils.click"),
                    "RUN_COMMAND", "/np setgameshield " + gameshield.getId(),
                    "SHOW_TEXT", localization.get("hover.gameshield", gameshield.getName(), gameshield.getId()));
        }
    }

    private void setGameshield(Object sender, String[] args){

        if(instance.getCore().getRestAPI().isGameshieldInvalid(args[1])) {
            instance.sendMessage(sender, localization.get("invalid.gameshield", args[1]));
            return;
        }

        Config.setGameShieldID(args[1]);
        instance.sendMessage(sender, localization.get("set.gameshield", args[1]));

        backendSelector(sender);
    }


    private void backendSelector(Object sender) {
        instance.sendMessage(sender, localization.get("select.backend"));

        List<Backend> backendList = instance.getCore().getRestAPI().getBackends();

        for (Backend backend : backendList) {
            instance.sendMessage(sender, "§5" + backend.getIp() + ":" + backend.getPort() + localization.get("utils.click"),
                    "RUN_COMMAND", "/np setbackend " + backend.getId(),
                    "SHOW_TEXT", localization.get("hover.backend", backend.getIp(), backend.getPort(), backend.getId()));
        }
    }

    private void setBackend(Object sender, String[] args) {

        if (instance.getCore().getRestAPI().isBackendInvalid(args[1])) {
            instance.sendMessage(sender, localization.get("invalid.backend", args[1]));
            return;
        }

        Config.setBackendID(args[1]);
        instance.sendMessage(sender, localization.get("set.backend", args[1]));

        if(instance.getCore().getPlayerInSetup().remove(sender)){
            instance.sendMessage(sender, localization.get("setup.finished"));
        }

        instance.getCore().getRestAPI().testCredentials();
    }

    private void showHelp(Object sender){
        instance.sendMessage(sender, localization.get("available.commands"));
        instance.sendMessage(sender, " - /np setup");
        instance.sendMessage(sender, " - /np ipanic");
        instance.sendMessage(sender, " - /np analytics");
        instance.sendMessage(sender, " - /np debugping");
        instance.sendMessage(sender, " - /np setgameshield");
        instance.sendMessage(sender, " - /np setbackend");
    }

    public static class ExecutorBuilder {
        private NeoProtectPlugin instance;
        private Object sender;
        private String[] args;
        private String msg;

        public ExecutorBuilder neoProtectPlugin(NeoProtectPlugin instance){
            this.instance = instance;
            return this;
        }

        public ExecutorBuilder sender(Object sender){
            this.sender = sender;
            return this;
        }

        public ExecutorBuilder msg(String msg){
            this.msg = msg;
            return this;
        }

        public ExecutorBuilder args(String[] args){
            this.args = args;
            return this;
        }

        public void executeChatEvent(){
            CompletableFuture.runAsync(() -> new NeoProtectExecutor().chatEvent(this));
        }
        public void executeCommand(){
            CompletableFuture.runAsync(()-> new NeoProtectExecutor().command(this));
        }

        public NeoProtectPlugin getInstance() {
            return instance;
        }

        public Object getSender() {
            return sender;
        }

        public String[] getArgs() {
            return args;
        }

        public String getMsg() {
            return msg;
        }
    }
}
