package io.openim.android.ouicore.net.bage;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * json解析帮助类
 */

public class GsonHel {

    private static GsonHel instance = null;
    private static Gson mGson = null;

    private static synchronized GsonHel get() {
        if (instance == null) {
            instance = new GsonHel();
        }
        return instance;
    }

    private GsonHel() {
        mGson = new GsonBuilder().serializeNulls().create();
    }

    public static Gson getGson() {
        return mGson;
    }

    public static String toJson(Object object) {
        get();
        return JSONObject.toJSONString(object);
    }

    public static <T> T fromJson(String json, Class<T> aClass) {
        get();
        return mGson.fromJson(json, aClass);
    }

    /**
     * 处理 data 为 object 的情况
     *
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Base<T> dataObject(String jsonStr, Class<T> clazz) throws Exception {
        get();
        Type type = new ParameterizedTypeImpl(Base.class, new Class[]{clazz});
        return JSONObject.parseObject(jsonStr, type);
    }

    /**
     * 处理 data 为 array 的情况
     *
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Base<List<T>> dataArray(String jsonStr, Class<T> clazz) throws Exception {
        get();
        // 生成List<T> 中的 Type
        Type listType = new ParameterizedTypeImpl(List.class, new Class[]{clazz});
        // 根据List<T>生成的，再生出完整的Base<List<T>>
        Type type = new ParameterizedTypeImpl(Base.class, new Type[]{listType});
        return JSONObject.parseObject(jsonStr, type);
    }


    public static class ParameterizedTypeImpl implements ParameterizedType {

        private final Class raw;
        private final Type[] args;

        public ParameterizedTypeImpl(Class raw, Type[] args) {
            this.raw = raw;
            this.args = args != null ? args : new Type[0];
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}


