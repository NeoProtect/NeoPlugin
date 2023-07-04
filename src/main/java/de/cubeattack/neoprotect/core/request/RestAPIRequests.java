package de.cubeattack.neoprotect.core.request;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import de.cubeattack.api.utils.VersionUtils;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.Core;
import de.cubeattack.neoprotect.core.JsonBuilder;
import de.cubeattack.neoprotect.core.Permission;
import de.cubeattack.neoprotect.core.objects.Backend;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class RestAPIRequests {

    @SuppressWarnings("FieldCanBeLocal")
    private final String ipGetter = "https://api4.my-ip.io/ip.json";
    private JSONArray neoServerIPs = null;
    private boolean setup = false;
    private final Core core;
    private final RestAPIManager rest;

    public RestAPIRequests(Core core) {
        this.core = core;
        this.rest = new RestAPIManager(core);

        testCredentials();
        attackCheckSchedule();
        versionCheckSchedule();
        neoServerIPsUpdateSchedule();

        if(Config.isUpdateIP()){
            backendServerIPUpdater();
        }
    }

    private String getIpv4(){
        return new ResponseManager(rest.callRequest(new Request.Builder().url(ipGetter).build())).getResponseBodyObject().getString("ip");
    }

    private JSONArray getNeoIPs(){
        return new ResponseManager(rest.callRequest(rest.defaultBuilder().url(rest.getBaseURL() + rest.getSubDirectory(RequestType.GET_NEO_SERVER_IPS)).build())).getResponseBodyArray();
    }

    public boolean isAPIInvalid(String apiKey){
        return !new ResponseManager(rest.callRequest(rest.defaultBuilder(apiKey).url(rest.getBaseURL() + rest.getSubDirectory(RequestType.GET_ATTACKS)).build())).checkCode(200);
    }

    private boolean isGameshieldFound(){
        return new ResponseManager(rest.callRequest(rest.defaultBuilder().url(rest.getBaseURL() + rest.getSubDirectory(RequestType.GET_GAMESHIELD_INFO, Config.getGameShieldID())).build())).checkCode(200);
    }

    private boolean isBackendFound(){
        return getBackends().stream().anyMatch(e -> e.compareById(Config.getBackendID()));
    }

    private boolean isAttack(){
        return rest.request(RequestType.GET_GAMESHIELD_ISUNDERATTACK, null, Config.getGameShieldID()).getResponseBody().equals("true");
    }

    private boolean updateBackend(RequestBody formBody){
        return rest.request(RequestType.POST_GAMESHIELD_BACKEND_UPDATE, formBody, Config.getGameShieldID(), Config.getBackendID()).checkCode(200);
    }

    public void setProxyProtocol(boolean setting){
        rest.request(RequestType.POST_GAMESHIELD_UPDATE, RequestBody.create(MediaType.parse("application/json"), new JsonBuilder().appendField("proxyProtocol", String.valueOf(setting)).build().toString()), Config.getGameShieldID());
    }

    public void testCredentials(){

        if(isAPIInvalid(Config.getAPIKey())){
            core.severe("API is not valid! Please run /neoprotect setup to set the API Key");
            setup = false;
            return;
        }else if(!isGameshieldFound()){
            core.severe("Gameshield is not valid! Please run /neoprotect setgameshield to set the gameshield");
            setup = false;
            return;
        }else if(!isBackendFound()) {
            core.severe("Backend is not valid! Please run /neoprotect setbackend to set the backend");
            setup = false;
            return;
        }

        this.setup = true;
        setProxyProtocol(Config.isProxyProtocol());
    }

    public boolean togglePanicMode(){
        JSONObject settings = rest.request(RequestType.GET_GAMESHIELD_INFO, null, Config.getGameShieldID()).getResponseBodyObject().getJSONObject("gameShieldSettings");
        String mitigationSensitivity = settings.getString("mitigationSensitivity");

        if(mitigationSensitivity.equals("UNDER_ATTACK")){
            rest.request(RequestType.POST_GAMESHIELD_UPDATE,
                    RequestBody.create(MediaType.parse("application/json"), settings.put("mitigationSensitivity", "MEDIUM").toString()),
                    Config.getGameShieldID());
            return false;
        }else {
            rest.request(RequestType.POST_GAMESHIELD_UPDATE,
                    RequestBody.create(MediaType.parse("application/json"), settings.put("mitigationSensitivity", "UNDER_ATTACK").toString()),
                    Config.getGameShieldID());
            return true;
        }
    }

    public HashMap<String, String> getGameshields(){
        HashMap<String, String> map = new HashMap<>();

        JSONArray gameshields = rest.request(RequestType.GET_GAMESHIELDS, null).getResponseBodyArray();

        for (Object object : gameshields) {
            JSONObject jsonObject = (JSONObject) object;
            map.put(jsonObject.get("id").toString(), jsonObject.get("name").toString());
        }

        return map;
    }

    public ArrayList<Backend> getBackends(){
        ArrayList<Backend> map = new ArrayList<>();
        JSONArray backends = rest.request(RequestType.GET_GAMESHIELD_BACKENDS, null, Config.getGameShieldID()).getResponseBodyArray();

        for (Object object : backends) {
            JSONObject jsonObject = (JSONObject) object;
            map.add(new Backend(jsonObject.getString("id"), jsonObject.getString("ipv4"), String.valueOf(jsonObject.getInt("port"))));
        }

        return map;
    }

    private void neoServerIPsUpdateSchedule(){

        core.info("NeoServerIPsUpdate scheduler started");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                neoServerIPs = getNeoIPs();
            }
        }, 0, 1000 * 10);
    }

    private void versionCheckSchedule(){

        core.info("VersionCheck scheduler started");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                core.setVersionResult(VersionUtils.checkVersion("NeoProtect", "NeoPlugin", "v" + core.getPlugin().getVersion()));
            }
        }, 0, 1000 * 60 * 30);
    }

    private void attackCheckSchedule(){

        core.info("AttackCheck scheduler started");

        final Boolean[] attackRunning = {false};

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if(!setup)return;

                if(!isAttack()) {
                    attackRunning[0] = false;
                    return;
                }

                if(!attackRunning[0]) {
                    core.warn("Gameshield ID '" + Config.getGameShieldID() + "' is under attack");
                    core.getPlugin().sendAdminMessage(Permission.NOTIFY,"Gameshield ID '" + Config.getGameShieldID() + "' is under attack" , null, null, null, null);
                    attackRunning[0] = true;
                }
            }
        }, 0, 1000 * 10);
    }

    private void backendServerIPUpdater(){

        core.info("BackendServerIPUpdate scheduler started");

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                Backend backend = getBackends().stream().filter(unFilteredBackend -> unFilteredBackend.compareById(Config.getBackendID())).findAny().orElse(null);

                if(!setup | backend == null) return;

                String ip = getIpv4();

                if(ip == null) return;
                if(ip.equals(backend.getIp())) return;

                RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), new JsonBuilder().appendField("ipv4", ip).build().toString());

                if(!updateBackend(formBody)){
                    core.warn("Update backendserver ID '" + Config.getBackendID() + "' to IP '" + ip + "' failed");
                }else {
                    core.info("Update backendserver ID '" + Config.getBackendID() + "' to IP '" + ip + "' success");
                    backend.setIp(ip);
                }
            }
        }, 1000, 1000*10);
    }

    public JSONArray getNeoServerIPs() {
        return neoServerIPs;
    }

    public boolean isSetup() {
        return setup;
    }
}
