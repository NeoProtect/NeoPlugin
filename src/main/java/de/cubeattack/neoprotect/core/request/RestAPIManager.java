package de.cubeattack.neoprotect.core.request;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import de.cubeattack.neoprotect.core.Config;
import de.cubeattack.neoprotect.core.Core;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;

public class RestAPIManager {

    private final OkHttpClient client = new OkHttpClient();
    private final String baseURL = "https://api.neoprotect.net/v2";
    private final Core core;

    public RestAPIManager(Core core) {
        this.core = core;
    }

    {
        client.setConnectTimeout(3, TimeUnit.SECONDS);
    }

    protected ResponseManager request(RequestType type, RequestBody requestBody, Object... value) {
        if (type.toString().startsWith("GET")) {
            return new ResponseManager(callRequest(defaultBuilder().url(baseURL + getSubDirectory(type, value)).build()));
        } else if (type.toString().startsWith("POST")) {
            return new ResponseManager(callRequest(defaultBuilder().url(baseURL + getSubDirectory(type, value)).post(requestBody).build()));
        } else {
            return new ResponseManager(callRequest(defaultBuilder().url(baseURL + getSubDirectory(type, value)).delete().build()));
        }
    }

    protected Response callRequest(Request request) {
        try {
            return client.newCall(request).execute();
        } catch (UnknownHostException | SocketTimeoutException | SocketException connectionException) {
            if(!request.url().toString().equals(core.getRestAPI().getPasteServer()) && !request.url().toString().equals(core.getRestAPI().getStatsServer())) {
                core.severe(request + " failed cause (" + connectionException + ")");
            }else
                core.debug(request + " failed cause (" + connectionException + ")");
        } catch (Exception exception) {
            core.severe(exception.getMessage(), exception);
        }
        return null;
    }

    protected Request.Builder defaultBuilder() {
        return defaultBuilder(Config.getAPIKey());
    }

    protected Request.Builder defaultBuilder(String apiKey) {
        return new Request.Builder()
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json");
    }

    protected String getSubDirectory(RequestType type, Object... values) {

        switch (type) {

            case GET_ATTACKS: {
                return new Formatter().format("/attacks", values).toString();
            }
            case GET_ATTACKS_GAMESHIELD: {
                return new Formatter().format("/attacks/gameshield/%s", values).toString();
            }

            case GET_GAMESHIELD_BACKENDS: {
                return new Formatter().format("/gameshields/%s/backends", values).toString();
            }
            case POST_GAMESHIELD_BACKEND_CREATE: {
                return new Formatter().format("/gameshields/%s/backends", values).toString();
            }
            case POST_GAMESHIELD_BACKEND_UPDATE: {
                return new Formatter().format("/gameshields/%s/backends/%s", values).toString();
            }
            case DELETE_GAMESHIELD_BACKEND_UPDATE: {
                return new Formatter().format("/gameshields/%s/backends/%s", values).toString();
            }
            case POST_GAMESHIELD_BACKEND_AVAILABLE: {
                return new Formatter().format("/gameshield/backends/available", values).toString();
            }

            case GET_GAMESHIELD_DOMAINS: {
                return new Formatter().format("/gameshields/domains/%s", values).toString();
            }
            case POST_GAMESHIELD_DOMAIN_CREATE: {
                return new Formatter().format("/gameshields/domains/%s", values).toString();
            }
            case POST_GAMESHIELD_DOMAIN_AVAILABLE: {
                return new Formatter().format("/gameshields/domains/available", values).toString();
            }
            case DELETE_GAMESHIELD_DOMAIN: {
                return new Formatter().format("/gameshields/domains/%s", values).toString();
            }

            case GET_GAMESHIELD_FRONTENDS: {
                return new Formatter().format("/gameshields/%s/frontends", values).toString();
            }
            case POST_GAMESHIELD_FRONTEND_CREATE: {
                return new Formatter().format("/gameshields/%s/frontends", values).toString();
            }

            case GET_GAMESHIELDS: {
                return new Formatter().format("/gameshields", values).toString();
            }
            case POST_GAMESHIELD_CREATE: {
                return new Formatter().format("/gameshields", values).toString();
            }
            case POST_GAMESHIELD_UPDATE: {
                return new Formatter().format("/gameshields/%s/settings", values).toString();
            }
            case POST_GAMESHIELD_UPDATE_REGION: {
                return new Formatter().format("/gameshields/%s/region/%s", values).toString();
            }
            case GET_GAMESHIELD_PLAN: {
                return new Formatter().format("/gameshields/%s/plan", values).toString();
            }
            case POST_GAMESHIELD_PLAN_UPGRADE: {
                return new Formatter().format("/gameshields/%s/plan", values).toString();
            }
            case POST_GAMESHIELD_UPDATE_NAME: {
                return new Formatter().format("/gameshields/%s/name", values).toString();
            }
            case POST_GAMESHIELD_UPDATE_ICON: {
                return new Formatter().format("/gameshields/%s/icon", values).toString();
            }
            case DELETE_GAMESHIELD_UPDATE_ICON: {
                return new Formatter().format("/gameshields/%s/icon", values).toString();
            }
            case POST_GAMESHIELD_UPDATE_BANNER: {
                return new Formatter().format("/gameshields/%s/banner", values).toString();
            }
            case DELETE_GAMESHIELD_BANNER: {
                return new Formatter().format("/gameshields/%s/banner", values).toString();
            }
            case POST_GAMESHIELD_AVAILABLE: {
                return new Formatter().format("/gameshields/available", values).toString();
            }
            case GET_GAMESHIELD_INFO: {
                return new Formatter().format("/gameshields/%s", values).toString();
            }
            case DELETE_GAMESHIELD: {
                return new Formatter().format("/gameshields/%s", values).toString();
            }
            case GET_GAMESHIELD_LASTSTATS: {
                return new Formatter().format("/gameshields/%s/lastStats", values).toString();
            }
            case GET_GAMESHIELD_ISUNDERATTACK: {
                return new Formatter().format("/gameshields/%s/isUnderAttack", values).toString();
            }
            case GET_GAMESHIELD_BANDWIDTH: {
                return new Formatter().format("/gameshields/%s/bandwidth", values).toString();
            }
            case GET_GAMESHIELD_ANALYTICS: {
                return new Formatter().format("/gameshields/%s/analytics/%s", values).toString();
            }

            case GET_FIREWALLS: {
                return new Formatter().format("/firewall/gameshield/%s/%s", values).toString();
            }
            case POST_FIREWALL_CREATE: {
                return new Formatter().format("/firewall/gameshield/%s/%s", values).toString();
            }
            case DELETE_FIREWALL: {
                return new Formatter().format("/firewall/gameshield/%s/%s", values).toString();
            }

            case GET_PLANS_AVAILABLE: {
                return new Formatter().format("/plans/gameshield", values).toString();
            }

            case GET_PROFILE_TRANSACTIONS: {
                return new Formatter().format("/profile/transactions", values).toString();
            }
            case GET_PROFILE_INFOS: {
                return new Formatter().format("/profile/infos", values).toString();
            }
            case GET_PROFILE_GENERALINFORMATION: {
                return new Formatter().format("/profile/generalInformation", values).toString();
            }

            case GET_NEO_SERVER_IPS: {
                return new Formatter().format("/public/servers", values).toString();
            }
            case GET_NEO_SERVER_REGIONS: {
                return new Formatter().format("/public/regions", values).toString();
            }

            case GET_VULNERABILITIES_GAMESHIELD: {
                return new Formatter().format("/vulnerabilities/%s", values).toString();
            }
            case POST_VULNERABILITIES: {
                return new Formatter().format("/vulnerabilities/%s", values).toString();
            }
            case GET_VULNERABILITIES_ALL: {
                return new Formatter().format("/vulnerabilities", values).toString();
            }
            case DELETE_VULNERABILITIES: {
                return new Formatter().format("/vulnerabilities/%s", values).toString();
            }

            case GET_WEBHOOKS: {
                return new Formatter().format("/webhooks/%s", values).toString();
            }
            case POST_WEBHOOK_CREATE: {
                return new Formatter().format("/webhooks/%s", values).toString();
            }
            case POST_WEBHOOK_TEST: {
                return new Formatter().format("/webhooks/%s/%s/test", values).toString();
            }
            case DELETE_WEBHOOK: {
                return new Formatter().format("/webhooks/%s/%s", values).toString();
            }
            default: {
                return null;
            }
        }
    }

    public String getBaseURL() {
        return baseURL;
    }
}
