package eliasstar.jsonrpc.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Request {

    private final String jsonrpc = "2.0";
    private final String id;
    private final String method;
    private final JsonElement params;

    public Request(String id, String method, JsonObject params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public Request(String id, String method, JsonArray params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public String jsonrpc() {
        return jsonrpc;
    }

    public String id() {
        return id;
    }

    public String method() {
        return method;
    }

    public JsonElement params() {
        return params;
    }

}