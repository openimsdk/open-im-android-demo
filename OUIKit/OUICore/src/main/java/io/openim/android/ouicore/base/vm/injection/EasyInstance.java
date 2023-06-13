package io.openim.android.ouicore.base.vm.injection;

import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import io.openim.android.ouicore.utils.L;

class EasyInstance {
    private static final String TAG = "EasyInstance";
    private static EasyInstance instance = null;
    private static final Object lock = new Object();

    public static EasyInstance inst() {
        synchronized (lock) {
            if (null == instance)
                instance = new EasyInstance();
            return instance;
        }
    }

    private final Map<String, _InstanceInfo<?>> _single = new HashMap<>();

    ///注入实例
    public  synchronized <T extends BaseVM>  T put(T dependency, String tag) {
        final String key = _getKey(dependency.getClass(), tag);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            _single.putIfAbsent(key, new _InstanceInfo<>(dependency));
        } else if (!_single.containsKey(key)) {
            _single.put(key, new _InstanceInfo<>(dependency));
        }
        return find(dependency.getClass(), tag);
    }

    ///获取注入的实例
    public <T extends BaseVM> T find(Class<?> dependency, String tag) {
        final String key = _getKey(dependency, tag);
        boolean contains = _single.containsKey(key);
        if (contains) {
            return (T) _single.get(key).value;
        } else {
            throw new RuntimeException(dependency + " not find. You need to call [Easy.put(" + dependency + ")]");
        }
    }

    public synchronized Boolean delete(Class<?> dependency, String tag) {
        final String key = _getKey(dependency, tag);
        if (_single.containsKey(key)) {
            _InstanceInfo<?> instanceInfo = _single.remove(key);
            assert instanceInfo != null;
            instanceInfo.value.removed();
            instanceInfo.value = null;
            L.d(TAG, "Instance " + dependency + " deleted");
            return true;
        } else {
            L.d(TAG, "Instance " + dependency + " already removed");
            return false;
        }
    }

    String _getKey(Class<?> type, String tag) {
        return tag == null ? type.getCanonicalName() : type.getCanonicalName() + "&"+tag;
    }
}

class _InstanceInfo<T extends BaseVM> {
    T value;

    public _InstanceInfo(T value) {
        this.value = value;
    }
}



