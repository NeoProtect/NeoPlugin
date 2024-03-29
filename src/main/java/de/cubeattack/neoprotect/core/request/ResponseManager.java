package de.cubeattack.neoprotect.core.request;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import de.cubeattack.api.libraries.org.json.JSONArray;
import de.cubeattack.api.libraries.org.json.JSONException;
import de.cubeattack.api.libraries.org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

public class ResponseManager extends JSONObject {

    private final String responseBody;
    private final Response response;
    private final int code;

    public ResponseManager(Response response) {
        this.responseBody = getBody(response);
        this.response = response;
        this.code = getCode();
    }

    private String getBody(Response response) {
        try (ResponseBody body = response.body()) {
            return body.string();
        } catch (NullPointerException | SocketTimeoutException ignored) {
            return "{}";
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean checkCode(int code) {
        return Objects.equals(this.code, code);
    }

    public int getCode() {
        try {
            return response.code();
        } catch (Exception ex) {
            return -1;
        }
    }

    public String getResponseBody() {
        return responseBody;
    }

    public JSONObject getResponseBodyObject() {
        try {
            return new JSONObject(responseBody);
        } catch (JSONException ignored) {}
        return new JSONObject();
    }

    public JSONArray getResponseBodyArray() {
        try {
            return new JSONArray(responseBody);
        } catch (JSONException ignored) {}
        return new JSONArray();
    }

    @Override
    public String getString(String key) {
        return super.getString(key);
    }
}
