package de.cubeattack.neoprotect.core.request;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import de.cubeattack.api.libraries.org.json.JSONArray;
import de.cubeattack.api.libraries.org.json.JSONException;
import de.cubeattack.api.libraries.org.json.JSONObject;
import de.cubeattack.api.util.versioning.VersionUtils;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.JsonBuilder;
import de.cubeattack.neoprotect.core.Permission;
import de.cubeattack.neoprotect.core.model.Backend;
import de.cubeattack.neoprotect.core.model.Firewall;
import de.cubeattack.neoprotect.core.model.Gameshield;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class RestAPIRequests {

    @SuppressWarnings("FieldCanBeLocal")
    private final String ipGetter = "https://api4.my-ip.io/ip.json";
    @SuppressWarnings("FieldCanBeLocal")
    private final String pasteServer = "https://paste.einfachesache.de/";
    @SuppressWarnings("FieldCanBeLocal")
    private final String statsServer = "https://metrics.einfachesache.de/api/stats/plugin";
    private JSONArray neoServerIPs = null;
    private boolean setup = false;
    private final Core core;
    private final RestAPIManager rest;

    public RestAPIRequests(Core core) {
        this.core = core;
        this.rest = new RestAPIManager(core);

        testCredentials();
        attackCheckSchedule();
        statsUpdateSchedule();
        versionCheckSchedule();
        neoServerIPsUpdateSchedule();

        if (Config.isUpdateIP()) {
            backendServerIPUpdater();
        }
    }

    private String getIpv4() {
        try {
            return new ResponseManager(rest.callRequest(new Request.Builder().url(ipGetter).build())).getResponseBodyObject().getString("ip");
        }catch (Exception ignore){}
        return null;
    }

    private JSONArray getNeoIPs() {
        return new ResponseManager(rest.callRequest(rest.defaultBuilder().url(rest.getBaseURL() + rest.getSubDirectory(RequestType.GET_NEO_SERVER_IPS)).build())).getResponseBodyArray();
    }

    public boolean isAPIInvalid(String apiKey) {
        return !new ResponseManager(rest.callRequest(rest.defaultBuilder(apiKey).url(rest.getBaseURL() + rest.getSubDirectory(RequestType.GET_ATTACKS)).build())).checkCode(200);
    }

    public boolean isGameshieldInvalid(String gameshieldID) {
        return !new ResponseManager(rest.callRequest(rest.defaultBuilder().url(rest.getBaseURL() + rest.getSubDirectory(RequestType.GET_GAMESHIELD_INFO, gameshieldID)).build())).checkCode(200);
    }

    public boolean isBackendInvalid(String backendID) {
        return getBackends().stream().noneMatch(e -> e.compareById(backendID));
    }

    private boolean isAttack() {
        return rest.request(RequestType.GET_GAMESHIELD_ISUNDERATTACK, null, Config.getGameShieldID()).getResponseBody().equals("true");
    }

    public String getPlan() {
        try {
            return rest.request(RequestType.GET_GAMESHIELD_PLAN, null, Config.getGameShieldID()).getResponseBodyObject().getJSONObject("gameShieldPlan").getJSONObject("options").getString("name");
        }catch (JSONException ignore){}
        return "FAILED";
    }

    private boolean updateBackend(RequestBody requestBody, String backendID) {
        return rest.request(RequestType.POST_GAMESHIELD_BACKEND_UPDATE, requestBody, Config.getGameShieldID(),backendID).checkCode(200);
    }

    public boolean updateStats(RequestBody requestBody, String identifier, String gameshieldID, String backendID) {
        return new ResponseManager(rest.callRequest(new Request.Builder().url(statsServer).header("identifier", identifier).header("GameshieldID", gameshieldID).header("BackendID", backendID).post(requestBody).build())).checkCode(200);
    }

    public void setProxyProtocol(boolean setting) {
        JSONObject settings = rest.request(RequestType.GET_GAMESHIELD_INFO, null, Config.getGameShieldID()).getResponseBodyObject().getJSONObject("gameShieldSettings");
        rest.request(RequestType.POST_GAMESHIELD_UPDATE, RequestBody.create(MediaType.parse("application/json"), settings.put("proxyProtocol", String.valueOf(setting)).toString()), Config.getGameShieldID());
    }

    public JSONObject getAnalytics() {
        return rest.request(RequestType.GET_GAMESHIELD_LASTSTATS, null, Config.getGameShieldID()).getResponseBodyObject();
    }

    public JSONObject getTraffic() {
        return rest.request(RequestType.GET_GAMESHIELD_BANDWIDTH, null, Config.getGameShieldID()).getResponseBodyObject();
    }

    public void testCredentials() {

        if (isAPIInvalid(Config.getAPIKey())) {
            core.severe("API is not valid! Please run /neoprotect setup to set the API Key");
            setup = false;
            return;
        } else if (isGameshieldInvalid(Config.getGameShieldID())) {
            core.severe("Gameshield is not valid! Please run /neoprotect setgameshield to set the gameshield");
            setup = false;
            return;
        } else if (isBackendInvalid(Config.getBackendID())) {
            core.severe("Backend is not valid! Please run /neoprotect setbackend to set the backend");
            setup = false;
            return;
        }

        this.setup = true;
        setProxyProtocol(Config.isProxyProtocol());

        Config.addAutoUpdater(getPlan());
    }

    public String paste(String content) {
        try {
            return new ResponseManager(rest.callRequest(new Request.Builder().url(pasteServer + "/documents")
                    .post(RequestBody.create(MediaType.parse("text/plain"), content)).build())).getResponseBodyObject().getString("key");
        } catch (Exception ignore) {}
        return null;
    }

    public boolean togglePanicMode() {
        JSONObject settings = rest.request(RequestType.GET_GAMESHIELD_INFO, null, Config.getGameShieldID()).getResponseBodyObject().getJSONObject("gameShieldSettings");
        String mitigationSensitivity = settings.getString("mitigationSensitivity");

        if (mitigationSensitivity.equals("UNDER_ATTACK")) {
            rest.request(RequestType.POST_GAMESHIELD_UPDATE,
                    RequestBody.create(MediaType.parse("application/json"), settings.put("mitigationSensitivity", "MEDIUM").toString()),
                    Config.getGameShieldID());
            return false;
        } else {
            rest.request(RequestType.POST_GAMESHIELD_UPDATE,
                    RequestBody.create(MediaType.parse("application/json"), settings.put("mitigationSensitivity", "UNDER_ATTACK").toString()),
                    Config.getGameShieldID());
            return true;
        }
    }

    public int toggle(String mode) {
        JSONObject settings = rest.request(RequestType.GET_GAMESHIELD_INFO, null, Config.getGameShieldID()).getResponseBodyObject().getJSONObject("gameShieldSettings");

        if(!settings.has(mode)) return -1;

        boolean mitigationSensitivity = settings.getBoolean(mode);

        if (mitigationSensitivity) {
            int code = rest.request(RequestType.POST_GAMESHIELD_UPDATE,
                    RequestBody.create(MediaType.parse("application/json"), settings.put(mode, false).toString()),
                    Config.getGameShieldID()).getCode();

            return code == 200 ? 0 : code;
        } else {
            int code = rest.request(RequestType.POST_GAMESHIELD_UPDATE,
                    RequestBody.create(MediaType.parse("application/json"), settings.put(mode, true).toString()),
                    Config.getGameShieldID()).getCode();

            return code == 200 ? 1 : code;
        }
    }

    public List<Gameshield> getGameshields() {
        List<Gameshield> list = new ArrayList<>();

        JSONArray gameshields = rest.request(RequestType.GET_GAMESHIELDS, null).getResponseBodyArray();

        for (Object object : gameshields) {
            JSONObject jsonObject = (JSONObject) object;
            list.add(new Gameshield(jsonObject.getString("id"), jsonObject.getString("name")));
        }

        return list;
    }

    public List<Backend> getBackends() {
        List<Backend> list = new ArrayList<>();
        JSONArray backends = rest.request(RequestType.GET_GAMESHIELD_BACKENDS, null, Config.getGameShieldID()).getResponseBodyArray();

        for (Object object : backends) {
            JSONObject jsonObject = (JSONObject) object;
            list.add(new Backend(jsonObject.getString("id"), jsonObject.getString("ipv4"), String.valueOf(jsonObject.getInt("port")), jsonObject.getBoolean("geyser")));
        }

        return list;
    }

    public List<Firewall> getFirewall(String mode) {
        List<Firewall> list = new ArrayList<>();
        JSONArray firewalls = rest.request(RequestType.GET_FIREWALLS, null, Config.getGameShieldID(), mode.toUpperCase()).getResponseBodyArray();

        for (Object object : firewalls) {
            JSONObject firewallJSON = (JSONObject) object;
            list.add(new Firewall(firewallJSON.getString("ip"), firewallJSON.get("id").toString()));
        }

        return list;
    }

    public int updateFirewall(String ip, String action, String mode) {
        if(action.equalsIgnoreCase("REMOVE")){
            Firewall firewall = getFirewall(mode).stream().filter(f -> f.getIp().equals(ip)).findFirst().orElse(null);

            if(firewall == null){
                return 0;
            }

            return rest.request(RequestType.DELETE_FIREWALL, null, Config.getGameShieldID(), firewall.getId()).getCode();
        }else if(action.equalsIgnoreCase("ADD")){
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new JsonBuilder().appendField("entry", ip).build().toString());
            return rest.request(RequestType.POST_FIREWALL_CREATE, requestBody, Config.getGameShieldID(), mode).getCode();
        }
        return -1;
    }

    private void statsUpdateSchedule() {

        core.info("StatsUpdate scheduler started");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if (!setup) return;

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(core.getPlugin().getStats()));
                if(!updateStats(requestBody, String.valueOf(UUID.nameUUIDFromBytes((Config.getGameShieldID() + ":" + Config.getBackendID() + ":" + core.getPlugin().getServerAddress()).getBytes(StandardCharsets.UTF_8))), Config.getGameShieldID(), Config.getBackendID()))
                    core.debug("Request to Update stats failed");
            }
        }, 1000, 1000 * 5);
    }

    private void neoServerIPsUpdateSchedule() {

        core.info("NeoServerIPsUpdate scheduler started");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                JSONArray IPs = getNeoIPs();
                neoServerIPs = IPs.isEmpty() ? neoServerIPs : IPs;
            }
        }, 0, 1000 * 60);
    }

    private void versionCheckSchedule() {

        core.info("VersionCheck scheduler started");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                core.setVersionResult(VersionUtils.checkVersion("NeoProtect", "NeoPlugin", "v" + core.getPlugin().getPluginVersion(), Config.getAutoUpdaterSettings(), 0, null));
            }
        }, 1000 * 60 * 5, 1000 * 60 * 5);
    }

    private void attackCheckSchedule() {

        core.info("AttackCheck scheduler started");

        final Boolean[] attackRunning = {false};

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if (!setup) return;

                if (!isAttack()) {
                    attackRunning[0] = false;
                    return;
                }

                if (!attackRunning[0]) {
                    core.warn("Gameshield ID '" + Config.getGameShieldID() + "' is under attack");
                    core.getPlugin().sendAdminMessage(Permission.NOTIFY, "Gameshield ID '" + Config.getGameShieldID() + "' is under attack", null, null, null, null);
                    attackRunning[0] = true;
                }
            }
        }, 1000 * 5, 1000 * 10);
    }

    private void backendServerIPUpdater() {

        core.info("BackendServerIPUpdate scheduler started");

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                if (!setup) return;

                Backend javaBackend = getBackends().stream().filter(unFilteredBackend -> unFilteredBackend.compareById(Config.getBackendID())).findAny().orElse(null);
                Backend geyserBackend = getBackends().stream().filter(unFilteredBackend -> unFilteredBackend.compareById(Config.getGeyserBackendID())).findAny().orElse(null);

                String ip = getIpv4();
                if (ip == null) return;

                if (javaBackend != null) {
                    if (!ip.equals(javaBackend.getIp())) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new JsonBuilder().appendField("ipv4", ip).build().toString());

                        if (!updateBackend(requestBody, Config.getBackendID())) {
                            core.warn("Update java backendserver ID '" + Config.getBackendID() + "' to IP '" + ip + "' failed");
                        } else {
                            core.info("Update java backendserver ID '" + Config.getBackendID() + "' to IP '" + ip + "' success");
                            javaBackend.setIp(ip);
                        }
                    }
                }

                if (geyserBackend != null) {
                    if (!ip.equals(geyserBackend.getIp())) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new JsonBuilder().appendField("ipv4", ip).build().toString());

                        if (!updateBackend(requestBody, Config.getGeyserBackendID())) {
                            core.warn("Update geyser backendserver ID '" + Config.getGeyserBackendID() + "' to IP '" + ip + "' failed");
                        } else {
                            core.info("Update geyser backendserver ID '" + Config.getGeyserBackendID() + "' to IP '" + ip + "' success");
                            geyserBackend.setIp(ip);
                        }
                    }
                }
            }
        }, 1000, 1000 * 20);
    }

    public JSONArray getNeoServerIPs() {
        return neoServerIPs;
    }

    public String getPasteServer() {
        return pasteServer;
    }

    public String getStatsServer() {
        return statsServer;
    }

    public boolean isSetup() {
        return setup;
    }
}
