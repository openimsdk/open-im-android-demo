package io.openim.android.ouicore.net.RXRetrofit;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import io.openim.android.ouicore.net.bage.GsonHel;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by WJ on 2017/11/6.
 */
public class Parameter {
    private LinkedHashMap<String, RequestBody> linkedHashMap;
    private LinkedHashMap<String, Object> jsonMap;

    public Parameter() {
        linkedHashMap = new LinkedHashMap<>();
        jsonMap = new LinkedHashMap<>();
    }

    public LinkedHashMap<String, RequestBody> buildFrom() {
        return linkedHashMap;
    }

    public LinkedHashMap<String, Object> buildMap() {
        return jsonMap;
    }

    public String buildJson() {
        return GsonHel.toJson(jsonMap);
    }
    public RequestBody buildJsonBody() {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), buildJson());
    }

    public Parameter add(String key, Object oj) {
        put(key, oj);
        return this;
    }

    public Parameter add(String key, File file) {
        put(key, file);
        return this;
    }

    public Parameter add(String key, List<File> files) {
        for (File file : files) {
            put(key, file);
        }
        return this;
    }


    private void put(String key, Object o) {
        RequestBody body = null;
        if (o instanceof String) {
            body = RequestBody.create(MediaType.parse("text/plain"), (String) o);
            jsonMap.put(key, o);
            linkedHashMap.put(key, body);
        } else if (o instanceof File) {
            File file = (File) o;
            body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            jsonMap.put(key, file.getPath());
            linkedHashMap.put(key + "\"; filename=\"" + file.getName(), body);
        } else {
            jsonMap.put(key, o);
        }

    }


}
