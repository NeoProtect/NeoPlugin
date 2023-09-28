package de.cubeattack.neoprotect.core.executor;

import de.cubeattack.api.API;
import de.cubeattack.api.language.Localization;
import de.cubeattack.api.libraries.org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import de.cubeattack.api.libraries.org.json.JSONObject;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.NeoProtectPlugin;
import de.cubeattack.neoprotect.core.model.Backend;
import de.cubeattack.neoprotect.core.model.Gameshield;
import de.cubeattack.neoprotect.core.model.Stats;
import de.cubeattack.neoprotect.core.model.debugtool.DebugPingResponse;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class NeoProtectExecutor {

    private static Timer debugTimer = new Timer();
    private NeoProtectPlugin instance;
    private Localization localization;

    private Object sender;
    private Locale locale;
    private String msg;
    private String[] args;
    private boolean isViaConsole;

    private void initials(ExecutorBuilder executeBuilder) {
        this.instance = executeBuilder.getInstance();
        this.localization = instance.getCore().getLocalization();

        this.sender = executeBuilder.getSender();
        this.locale = executeBuilder.getLocal();
        this.args = executeBuilder.getArgs();
        this.msg = executeBuilder.getMsg();
        this.isViaConsole = executeBuilder.isViaConsole();
    }

    private void chatEvent(ExecutorBuilder executorBuilder) {
        initials(executorBuilder);

        if (instance.getCore().getRestAPI().isAPIInvalid(msg)) {
            instance.sendMessage(sender, localization.get(locale, "apikey.invalid"));
            return;
        }

        Config.setAPIKey(msg);

        instance.sendMessage(sender, localization.get(locale, "apikey.valid"));

        gameshieldSelector();
    }

    private void command(ExecutorBuilder executorBuilder) {

        initials(executorBuilder);

        if (args.length == 0) {
            showHelp();
            return;
        }

        if (!instance.getCore().isSetup() & !args[0].equals("setup") & !args[0].equals("setgameshield") & !args[0].equals("setbackend")) {
            instance.sendMessage(sender, localization.get(locale, "setup.command.required"));
            return;
        }

        switch (args[0].toLowerCase()) {

            case "setup": {
                if (isViaConsole) {
                    instance.sendMessage(sender, localization.get(Locale.getDefault(), "console.command"));
                } else {
                    setup();
                }
                break;
            }

            case "ipanic": {
                iPanic(args);
                break;
            }

            case "directconnectwhitelist": {
                directConnectWhitelist(args);
                break;
            }

            case "toggle": {
                toggle(args);
                break;
            }

            case "analytics": {
                analytics();
                break;
            }

            case "whitelist":
            case "blacklist": {
                firewall(args);
                break;
            }

            case "debugtool": {
                debugTool(args);
                break;
            }

            case "setgameshield": {
                if (args.length == 1 && !isViaConsole) {
                    gameshieldSelector();
                } else if (args.length == 2) {
                    setGameshield(args);
                } else {
                    instance.sendMessage(sender, localization.get(locale, "usage.setgameshield"));
                }
                break;
            }

            case "setbackend": {
                if (args.length == 1 && !isViaConsole) {
                    javaBackendSelector();
                } else if (args.length == 2) {
                    setJavaBackend(args);
                } else {
                    instance.sendMessage(sender, localization.get(locale, "usage.setbackend"));
                }
                break;
            }

            case "setgeyserbackend": {
                if (args.length == 1 && !isViaConsole) {
                    bedrockBackendSelector();
                } else if (args.length == 2) {
                    setBedrockBackend(args);
                } else {
                    instance.sendMessage(sender, localization.get(locale, "usage.setgeyserbackend"));
                }
                break;
            }
            default: {
                showHelp();
            }
        }
    }


    private void setup() {
        instance.getCore().getPlayerInSetup().add(sender);
        instance.sendMessage(sender, localization.get(locale, "command.setup") + localization.get(locale, "utils.click"),
                "OPEN_URL", "https://panel.neoprotect.net/profile",
                "SHOW_TEXT", localization.get(locale, "apikey.find"));
    }

    private void iPanic(String[] args) {
        if (args.length != 1) {
            instance.sendMessage(sender, localization.get(locale, "usage.ipanic"));
        } else {
            instance.sendMessage(sender, localization.get(locale, "command.ipanic",
                    localization.get(locale, instance.getCore().getRestAPI().togglePanicMode() ? "utils.activated" : "utils.deactivated")));
        }
    }

    private void directConnectWhitelist(String[] args) {
        if (args.length == 2) {
            instance.getCore().getDirectConnectWhitelist().add(args[1]);
            instance.sendMessage(sender, localization.get(locale, "command.directconnectwhitelist", args[1]));
        } else {
            instance.sendMessage(sender, localization.get(locale, "usage.directconnectwhitelist"));
        }
    }

    private void toggle(String[] args) {
        if (args.length != 2) {
            instance.sendMessage(sender, localization.get(locale, "usage.toggle"));
        } else {
            int response = instance.getCore().getRestAPI().toggle(args[1]);

            if (response == 403) {
                instance.sendMessage(sender, localization.get(locale, "err.upgrade-plan"));
                return;
            }

            if (response == 429) {
                instance.sendMessage(sender, localization.get(locale, "err.rate-limit"));
                return;
            }

            if (response == -1) {
                instance.sendMessage(sender, "§cCan not found setting '" + args[1] + "'");
                return;
            }

            instance.sendMessage(sender, localization.get(locale, "command.toggle", args[1],
                    localization.get(locale, response == 1 ? "utils.activated" : "utils.deactivated")));
        }
    }

    private void analytics() {
        instance.sendMessage(sender, "§7§l--------- §bAnalytics §7§l---------");
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

    private void firewall(String[] args) {
        if (args.length == 1) {
            instance.sendMessage(sender, "§7§l----- §bFirewall (" + args[0].toUpperCase() + ")§7§l -----");
            instance.getCore().getRestAPI().getFirewall(args[0]).forEach((firewall ->
                    instance.sendMessage(sender, "IP: " + firewall.getIp() + " ID(" + firewall.getId() + ")")));
        } else if (args.length == 3) {
            String ip = args[2];
            String action = args[1];
            String mode = args[0].toUpperCase();
            int response = instance.getCore().getRestAPI().updateFirewall(ip, action, mode);

            if (response == -1) {
                instance.sendMessage(sender, localization.get(locale, "usage.firewall"));
                return;
            }

            if (response == 0) {
                instance.sendMessage(sender, localization.get(locale, "command.firewall.notfound", ip, mode));
                return;
            }

            if (response == 400) {
                instance.sendMessage(sender, localization.get(locale, "command.firewall.ip-invalid", ip));
                return;
            }

            if (response == 403) {
                instance.sendMessage(sender, localization.get(locale, "err.upgrade-plan"));
                return;
            }

            if (response == 429) {
                instance.sendMessage(sender, localization.get(locale, "err.rate-limit"));
                return;
            }

            instance.sendMessage(sender, (action.equalsIgnoreCase("add") ? "Added '" : "Removed '") + ip + "' to firewall (" + mode + ")");
        } else {
            instance.sendMessage(sender, localization.get(locale, "usage.firewall"));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void debugTool(String[] args) {

        if (instance.getPluginType() == NeoProtectPlugin.PluginType.SPIGOT) {
            instance.sendMessage(sender, localization.get(locale, "debug.spigot"));
            return;
        }

        if (args.length == 2) {
            if (args[1].equals("cancel")) {
                debugTimer.cancel();
                instance.getCore().setDebugRunning(false);
                instance.sendMessage(sender, localization.get(locale, "debug.cancelled"));
                return;
            }

            if (!isInteger(args[1])) {
                instance.sendMessage(sender, localization.get(locale, "usage.debug"));
                return;
            }
        }

        if (instance.getCore().isDebugRunning()) {
            instance.sendMessage(sender, localization.get(locale, "debug.running"));
            return;
        }

        instance.getCore().setDebugRunning(true);
        instance.sendMessage(sender, localization.get(locale, "debug.starting"));

        int amount = args.length == 2 ? (Integer.parseInt(args[1]) <= 0 ? 1 : Integer.parseInt(args[1])) : 5;

        debugTimer = new Timer();

        debugTimer.schedule(new TimerTask() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                instance.getCore().getTimestampsMap().put(instance.sendKeepAliveMessage(new Random().nextInt(90) * 10000 + 1337), new Timestamp(System.currentTimeMillis()));
                instance.sendMessage(sender, localization.get(locale, "debug.sendingPackets") + " (" + counter + "/" + amount + ")");
                if (counter >= amount) this.cancel();
            }
        }, 500, 2000);

        debugTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                API.getExecutorService().submit(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        Stats stats = instance.getStats();
                        File file = new File("plugins/NeoProtect/debug" + "/" + new Timestamp(System.currentTimeMillis()) + ".yml");
                        YamlConfiguration configuration = new YamlConfiguration();

                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                        }

                        configuration.load(file);

                        configuration.set("general.osName", System.getProperty("os.name"));
                        configuration.set("general.javaVersion", System.getProperty("java.version"));
                        configuration.set("general.pluginVersion", stats.getPluginVersion());
                        configuration.set("general.ProxyName", stats.getServerName());
                        configuration.set("general.ProxyVersion", stats.getServerVersion());
                        configuration.set("general.ProxyPlugins", instance.getPlugins());

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


                            configuration.set("players." + playerName + ".playerAddress", list.get(0).getPlayerAddress());
                            configuration.set("players." + playerName + ".neoAddress", list.get(0).getNeoAddress());

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

                            configuration.set("players." + playerName + ".ping.max.PlayerToProxyLatenz", maxPlayerToProxyLatenz);
                            configuration.set("players." + playerName + ".ping.max.NeoToProxyLatenz", maxNeoToProxyLatenz);
                            configuration.set("players." + playerName + ".ping.max.ProxyToBackendLatenz", maxProxyToBackendLatenz);
                            configuration.set("players." + playerName + ".ping.max.PlayerToNeoLatenz", maxPlayerToNeoLatenz);

                            configuration.set("players." + playerName + ".ping.average.PlayerToProxyLatenz", avgPlayerToProxyLatenz / list.size());
                            configuration.set("players." + playerName + ".ping.average.NeoToProxyLatenz", avgNeoToProxyLatenz / list.size());
                            configuration.set("players." + playerName + ".ping.average.ProxyToBackendLatenz", avgProxyToBackendLatenz / list.size());
                            configuration.set("players." + playerName + ".ping.average.PlayerToNeoLatenz", avgPlayerToNeoLatenz / list.size());

                            configuration.set("players." + playerName + ".ping.min.PlayerToProxyLatenz", minPlayerToProxyLatenz);
                            configuration.set("players." + playerName + ".ping.min.NeoToProxyLatenz", minNeoToProxyLatenz);
                            configuration.set("players." + playerName + ".ping.min.ProxyToBackendLatenz", minProxyToBackendLatenz);
                            configuration.set("players." + playerName + ".ping.min.PlayerToNeoLatenz", minPlayerToNeoLatenz);

                        }));
                        configuration.save(file);

                        final String content = new String(Files.readAllBytes(file.toPath()));
                        final String pasteKey = instance.getCore().getRestAPI().paste(content);

                        instance.getCore().getDebugPingResponses().clear();
                        instance.sendMessage(sender, localization.get(locale, "debug.finished.first") + " (took " + (System.currentTimeMillis() - startTime) + "ms)");
                        if(pasteKey != null) {
                            final String url = instance.getCore().getRestAPI().getPasteServer() + pasteKey + ".yml";
                            instance.sendMessage(sender, localization.get(locale, "debug.finished.url") + url + localization.get(locale, "utils.open"), "OPEN_URL", url, null, null);
                        } else {
                            instance.sendMessage(sender, localization.get(locale, "debug.finished.file") + file.getAbsolutePath() + localization.get(locale, "utils.copy"), "COPY_TO_CLIPBOARD", file.getAbsolutePath(), null, null);
                        }
                        instance.getCore().setDebugRunning(false);
                    } catch (Exception ex) {
                        instance.getCore().severe(ex.getMessage(), ex);
                    }
                });
            }
        }, 2000L * amount + 500);
    }

    private void gameshieldSelector() {
        instance.sendMessage(sender, localization.get(locale, "select.gameshield"));

        List<Gameshield> gameshieldList = instance.getCore().getRestAPI().getGameshields();

        for (Gameshield gameshield : gameshieldList) {
            instance.sendMessage(sender, "§5" + gameshield.getName() + localization.get(locale, "utils.click"),
                    "RUN_COMMAND", "/np setgameshield " + gameshield.getId(),
                    "SHOW_TEXT", localization.get(locale, "hover.gameshield", gameshield.getName(), gameshield.getId()));
        }
    }

    private void setGameshield(String[] args) {

        if (instance.getCore().getRestAPI().isGameshieldInvalid(args[1])) {
            instance.sendMessage(sender, localization.get(locale, "invalid.gameshield", args[1]));
            return;
        }

        Config.setGameShieldID(args[1]);
        instance.sendMessage(sender, localization.get(locale, "set.gameshield", args[1]));

        javaBackendSelector();
    }


    private void javaBackendSelector() {
        List<Backend> backendList = instance.getCore().getRestAPI().getBackends();

        instance.sendMessage(sender, localization.get(locale, "select.backend", "java"));

        for (Backend backend : backendList) {
            if(backend.isGeyser())continue;
            instance.sendMessage(sender, "§5" + backend.getIp() + ":" + backend.getPort() + localization.get(locale, "utils.click"),
                    "RUN_COMMAND", "/np setbackend " + backend.getId(),
                    "SHOW_TEXT", localization.get(locale, "hover.backend", backend.getIp(), backend.getPort(), backend.getId()));
        }
    }

    private void setJavaBackend(String[] args) {

        if (instance.getCore().getRestAPI().isBackendInvalid(args[1])) {
            instance.sendMessage(sender, localization.get(locale, "invalid.backend", "java", args[1]));
            return;
        }

        Config.setBackendID(args[1]);

        instance.sendMessage(sender, localization.get(locale, "set.backend", "java", args[1]));
        instance.getCore().getRestAPI().testCredentials();

        bedrockBackendSelector();
    }

    private void bedrockBackendSelector() {
        List<Backend> backendList = instance.getCore().getRestAPI().getBackends();

        if(backendList.stream().anyMatch(Backend::isGeyser)) {
            instance.sendMessage(sender, localization.get(locale, "select.backend", "geyser"));

            for (Backend backend : backendList) {
                if (!backend.isGeyser()) continue;
                instance.sendMessage(sender, "§5" + backend.getIp() + ":" + backend.getPort() + localization.get(locale, "utils.click"),
                        "RUN_COMMAND", "/np setgeyserbackend " + backend.getId(),
                        "SHOW_TEXT", localization.get(locale, "hover.backend", backend.getIp(), backend.getPort(), backend.getId()));
            }
        }else if (instance.getCore().getPlayerInSetup().remove(sender)) {
            instance.sendMessage(sender, localization.get(locale, "setup.finished"));
        }
    }

    private void setBedrockBackend(String[] args) {

        if (instance.getCore().getRestAPI().isBackendInvalid(args[1])) {
            instance.sendMessage(sender, localization.get(locale, "invalid.backend", "geyser",  args[1]));
            return;
        }

        Config.setGeyserBackendID(args[1]);
        instance.sendMessage(sender, localization.get(locale, "set.backend","geyser",  args[1]));

        if (instance.getCore().getPlayerInSetup().remove(sender)) {
            instance.sendMessage(sender, localization.get(locale, "setup.finished"));
        }
    }

    private void showHelp() {
        instance.sendMessage(sender, localization.get(locale, "available.commands"));
        instance.sendMessage(sender, " - /np setup");
        instance.sendMessage(sender, " - /np ipanic");
        instance.sendMessage(sender, " - /np analytics");
        instance.sendMessage(sender, " - /np toggle (option)");
        instance.sendMessage(sender, " - /np whitelist (add/remove) (ip)");
        instance.sendMessage(sender, " - /np blacklist (add/remove) (ip)");
        instance.sendMessage(sender, " - /np debugTool (cancel / amount)");
        instance.sendMessage(sender, " - /np directConnectWhitelist (ip)");
        instance.sendMessage(sender, " - /np setgameshield [id]");
        instance.sendMessage(sender, " - /np setbackend [id]");
        instance.sendMessage(sender, " - /np setgeyserbackend [id]");
    }

    public static class ExecutorBuilder {
        private NeoProtectPlugin instance;
        private Object sender;
        private String[] args;
        private Locale local;
        private String msg;
        private boolean viaConsole;

        public ExecutorBuilder neoProtectPlugin(NeoProtectPlugin instance) {
            this.instance = instance;
            return this;
        }

        public ExecutorBuilder sender(Object sender) {
            this.sender = sender;
            return this;
        }

        public ExecutorBuilder args(String[] args) {
            this.args = args;
            return this;
        }

        public ExecutorBuilder local(Locale local) {
            this.local =  local == null ? Locale.ENGLISH : local;
            return this;
        }

        public ExecutorBuilder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public ExecutorBuilder viaConsole(boolean viaConsole) {
            this.viaConsole = viaConsole;
            return this;
        }

        public void executeChatEvent() {
            API.getExecutorService().submit(() -> new NeoProtectExecutor().chatEvent(this));
        }

        public void executeCommand() {
            new NeoProtectExecutor().command(this);
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

        public Locale getLocal() {
            return local;
        }

        public String getMsg() {
            return msg;
        }

        public boolean isViaConsole() {
            return viaConsole;
        }
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
