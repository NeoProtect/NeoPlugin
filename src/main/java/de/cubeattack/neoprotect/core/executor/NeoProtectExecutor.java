package de.cubeattack.neoprotect.core.executor;

import de.cubeattack.api.language.Localization;
import de.cubeattack.api.libraries.org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import de.cubeattack.api.libraries.org.json.JSONObject;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.model.Backend;
import de.cubeattack.neoprotect.core.model.Gameshield;
import de.cubeattack.neoprotect.core.model.debugtool.DebugPingResponse;

import java.io.File;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

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

    private void chatEvent(ExecutorBuilder executorBuilder) {
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

        if (args.length == 0) {
            showHelp(sender);
            return;
        }

        if (!instance.getCore().isSetup() & !args[0].equals("setup") & !args[0].equals("setgameshield") & !args[0].equals("setbackend")) {
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
                debugTool();
                break;
            }

            case "analytics": {
                analytics();
                break;
            }

            case "setgameshield": {
                if (args.length == 1) {
                    gameshieldSelector(sender);
                } else if (args.length == 2) {
                    setGameshield(sender, args);
                } else {
                    instance.sendMessage(sender, localization.get("usage.setgameshield"));
                }
                break;
            }

            case "setbackend": {
                if (args.length == 1) {
                    backendSelector(sender);
                } else if (args.length == 2) {
                    setBackend(sender, args);
                } else {
                    instance.sendMessage(sender, localization.get("usage.setbackend"));
                }
                break;
            }
            default: {
                showHelp(sender);
            }
        }
    }


    private void setup(Object sender) {
        instance.getCore().getPlayerInSetup().add(sender);
        instance.sendMessage(sender, localization.get("command.setup") + localization.get("utils.click"),
                "OPEN_URL", "https://panel.neoprotect.net/profile",
                "SHOW_TEXT", localization.get("apikey.find"));
    }

    private void iPanic(Object sender, String[] args) {
        if (args.length != 1) {
            instance.sendMessage(sender, localization.get("usage.ipanic"));
        } else {
            instance.sendMessage(sender, localization.get("command.ipanic",
                    localization.get(instance.getCore().getRestAPI().togglePanicMode() ? "utils.activated" : "utils.deactivated")));
        }
    }

    private void analytics() {
        instance.sendMessage(sender, "§7§l-------- §bAnalytics §7§l--------");
        JSONObject analytics = instance.getCore().getRestAPI().getAnalytics();
        instance.getCore().getRestAPI().getAnalytics().keySet().forEach(ak -> {
            if (ak.equals("bandwidth")) {
                return;
            }

            if (ak.equals("traffic")) {
                instance.sendMessage(sender, ak.replace("traffic", "bandwidth") + ": " +
                        new DecimalFormat("#.####").format((float) analytics.getInt(ak) * 8 / (1000 * 1000)) + " mbit/s");
                JSONObject traffic = instance.getCore().getRestAPI().getTraffic();

                AtomicReference<String> trafficUsed = new AtomicReference<>();
                AtomicReference<String> trafficAvailable = new AtomicReference<>();
                traffic.keySet().forEach(bk -> {
                    if (bk.equals("used")) {
                        trafficUsed.set(traffic.getFloat(bk) / (1000 * 1000 * 1000) + " gb");
                    }
                    if (bk.equals("available")) {
                        trafficAvailable.set(String.valueOf(traffic.getLong(bk)).equals("999999999") ? "unlimited" : traffic.getLong(bk) + " gb");
                    }
                });
                instance.sendMessage(sender, "bandwidth used" + ": " + trafficUsed.get() + "/" + trafficAvailable.get());

                return;
            }

            instance.sendMessage(sender, ak
                    .replace("onlinePlayers", "online players")
                    .replace("cps", "connections/s") + ": " + analytics.get(ak));
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void debugTool() {

        if (instance.getPluginType() == NeoProtectPlugin.PluginType.SPIGOT) {
            instance.sendMessage(sender, localization.get("debug.spigot"));
            return;
        }

        if (instance.getPluginType() == NeoProtectPlugin.PluginType.VELOCITY) {
            instance.sendMessage(sender, "This command is currently not yet supported for Velocity");
            return;
        }

        if (instance.getCore().isDebugRunning()) {
            instance.sendMessage(sender, localization.get("debug.running"));
            return;
        }

        instance.getCore().setDebugRunning(true);
        instance.sendMessage(sender, localization.get("debug.starting"));

        new Timer().schedule(new TimerTask() {
            int counter = 0;
            @Override
            public void run() {
                counter++;
                instance.getCore().getTimestampsMap().put(instance.sendKeepAliveMessage(new Random().nextInt(90) * 10000 + 1337), new Timestamp(System.currentTimeMillis()));
                instance.sendMessage(sender, localization.get("debug.sendingPackets") + " (" + counter + "/5)");
                if(counter >= 5)this.cancel();
            }
        },500, 2000);


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.getCore().getExecutorService().submit(() -> {
                    try {
                        File file = new File("plugins/NeoProtect/debug" + "/" + new Timestamp(System.currentTimeMillis()) + ".yml");
                        YamlConfiguration configuration = new YamlConfiguration();

                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                        }

                        configuration.load(file);

                        instance.getCore().getDebugPingResponses().keySet().forEach((playerName -> {
                            List<DebugPingResponse> list = instance.getCore().getDebugPingResponses().get(playerName);

                            long maxPlayerToProxyLatenz = 0;
                            long maxNeoToProxyLatenz = 0;
                            long maxProxyToBackendLatenz = 0;
                            long maxPlayerToNeoLatenz = 0;

                            long avgPlayerToProxyLatenz = 0;
                            long avgNeoToProxyLatenz = 0;
                            long avgProxyToBackendLatenz = 0;
                            long avgPlayerToNeoLatenz = 0;

                            long minPlayerToProxyLatenz = Long.MAX_VALUE;
                            long minNeoToProxyLatenz = Long.MAX_VALUE;
                            long minProxyToBackendLatenz = Long.MAX_VALUE;
                            long minPlayerToNeoLatenz = Long.MAX_VALUE;

                            for (DebugPingResponse response : list) {
                                if (maxPlayerToProxyLatenz < response.getPlayerToProxyLatenz())
                                    maxPlayerToProxyLatenz = response.getPlayerToProxyLatenz();
                                if (maxNeoToProxyLatenz < response.getNeoToProxyLatenz())
                                    maxNeoToProxyLatenz = response.getNeoToProxyLatenz();
                                if (maxProxyToBackendLatenz < response.getProxyToBackendLatenz())
                                    maxProxyToBackendLatenz = response.getProxyToBackendLatenz();
                                if (maxPlayerToNeoLatenz < response.getPlayerToNeoLatenz())
                                    maxPlayerToNeoLatenz = response.getPlayerToNeoLatenz();

                                avgPlayerToProxyLatenz = avgPlayerToProxyLatenz + response.getPlayerToProxyLatenz();
                                avgNeoToProxyLatenz = avgNeoToProxyLatenz + response.getNeoToProxyLatenz();
                                avgProxyToBackendLatenz = avgProxyToBackendLatenz + response.getProxyToBackendLatenz();
                                avgPlayerToNeoLatenz = avgPlayerToNeoLatenz + response.getPlayerToNeoLatenz();

                                if (minPlayerToProxyLatenz > response.getPlayerToProxyLatenz())
                                    minPlayerToProxyLatenz = response.getPlayerToProxyLatenz();
                                if (minNeoToProxyLatenz > response.getNeoToProxyLatenz())
                                    minNeoToProxyLatenz = response.getNeoToProxyLatenz();
                                if (minProxyToBackendLatenz > response.getProxyToBackendLatenz())
                                    minProxyToBackendLatenz = response.getProxyToBackendLatenz();
                                if (minPlayerToNeoLatenz > response.getPlayerToNeoLatenz())
                                    minPlayerToNeoLatenz = response.getPlayerToNeoLatenz();
                            }

                            configuration.set("players." + playerName + ".max.PlayerToProxyLatenz", maxPlayerToProxyLatenz);
                            configuration.set("players." + playerName + ".max.NeoToProxyLatenz", maxNeoToProxyLatenz);
                            configuration.set("players." + playerName + ".max.ProxyToBackendLatenz", maxProxyToBackendLatenz);
                            configuration.set("players." + playerName + ".max.PlayerToNeoLatenz", maxPlayerToNeoLatenz);

                            configuration.set("players." + playerName + ".average.PlayerToProxyLatenz", avgPlayerToProxyLatenz / list.size());
                            configuration.set("players." + playerName + ".average.NeoToProxyLatenz", avgNeoToProxyLatenz / list.size());
                            configuration.set("players." + playerName + ".average.ProxyToBackendLatenz", avgProxyToBackendLatenz / list.size());
                            configuration.set("players." + playerName + ".average.PlayerToNeoLatenz", avgPlayerToNeoLatenz / list.size());

                            configuration.set("players." + playerName + ".min.PlayerToProxyLatenz", minPlayerToProxyLatenz);
                            configuration.set("players." + playerName + ".min.NeoToProxyLatenz", minNeoToProxyLatenz);
                            configuration.set("players." + playerName + ".min.ProxyToBackendLatenz", minProxyToBackendLatenz);
                            configuration.set("players." + playerName + ".min.PlayerToNeoLatenz", minPlayerToNeoLatenz);

                        }));

                        configuration.save(file);
                        instance.sendMessage(sender, localization.get("debug.finished.first"));
                        instance.sendMessage(sender, localization.get("debug.finished.second") + file.getAbsolutePath() + " " + localization.get("utils.copy"), "COPY_TO_CLIPBOARD", file.getAbsolutePath(), null, null);
                        instance.getCore().setDebugRunning(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 2000 * 4 + 2500);
    }

    private void gameshieldSelector(Object sender) {
        instance.sendMessage(sender, localization.get("select.gameshield"));

        List<Gameshield> gameshieldList = instance.getCore().getRestAPI().getGameshields();

        for (Gameshield gameshield : gameshieldList) {
            instance.sendMessage(sender, "§5" + gameshield.getName() + localization.get("utils.click"),
                    "RUN_COMMAND", "/np setgameshield " + gameshield.getId(),
                    "SHOW_TEXT", localization.get("hover.gameshield", gameshield.getName(), gameshield.getId()));
        }
    }

    private void setGameshield(Object sender, String[] args) {

        if (instance.getCore().getRestAPI().isGameshieldInvalid(args[1])) {
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

        if (instance.getCore().getPlayerInSetup().remove(sender)) {
            instance.sendMessage(sender, localization.get("setup.finished"));
        }

        instance.getCore().getRestAPI().testCredentials();
    }

    private void showHelp(Object sender) {
        instance.sendMessage(sender, localization.get("available.commands"));
        instance.sendMessage(sender, " - /np setup");
        instance.sendMessage(sender, " - /np ipanic");
        instance.sendMessage(sender, " - /np analytics");
        instance.sendMessage(sender, " - /np debugPing");
        instance.sendMessage(sender, " - /np setgameshield");
        instance.sendMessage(sender, " - /np setbackend");
    }

    public static class ExecutorBuilder {
        private NeoProtectPlugin instance;
        private Object sender;
        private String[] args;
        private String msg;

        public ExecutorBuilder neoProtectPlugin(NeoProtectPlugin instance) {
            this.instance = instance;
            return this;
        }

        public ExecutorBuilder sender(Object sender) {
            this.sender = sender;
            return this;
        }

        public ExecutorBuilder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public ExecutorBuilder args(String[] args) {
            this.args = args;
            return this;
        }

        public void executeChatEvent() {
            instance.getCore().getExecutorService().submit(() -> new NeoProtectExecutor().chatEvent(this));
        }

        public void executeCommand() {
            instance.getCore().getExecutorService().submit(() -> new NeoProtectExecutor().command(this));
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
