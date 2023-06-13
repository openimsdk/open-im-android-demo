package io.openim.android.ouicore.base.vm.injection;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

public class Easy {
    public static <T extends BaseVM> T put(T dependency) {
        return put(dependency, null);
    }

    public static <T extends BaseVM> T put(T dependency, String tag) {
        return EasyInstance.inst().put(dependency, tag);
    }

    public static <T extends BaseVM> T find(Class<T> dependency) {
        return find(dependency, null);
    }

    public static <T extends BaseVM> T find(Class<T> dependency, String tag) {
        return EasyInstance.inst().find(dependency, tag);
    }

    public static <T extends BaseVM> Boolean delete(Class<T> dependency) {
        return delete(dependency, null);
    }

    public static <T extends BaseVM> Boolean delete(Class<T> dependency, String tag) {
        return EasyInstance.inst().delete(dependency, tag);
    }


    /**
     * global use this
     *
     * @param dependency
     * @param <T>
     * @return
     */
    public static <T extends BaseVM> T installVM(Class<T> dependency) {
        return installVM(null, dependency, null);
    }

    public static <T extends BaseVM> T installVM(Class<T> dependency, String tag) {
        return installVM(null, dependency, tag);
    }
    /**
     * single activity use this
     *
     * @param owner
     * @param dependency
     * @param <T>
     * @return
     */
    public static <T extends BaseVM> T installVM(ViewModelStoreOwner owner, Class<T> dependency) {
        return installVM(owner, dependency, null);
    }

    public static <T extends BaseVM> T installVM(ViewModelStoreOwner owner, Class<T> dependency,
                                                 String tag) {
        T vm = null;
        if (null == owner) {
            //global
            try {
                vm = dependency.newInstance();
                Easy.put(vm, tag);
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        } else {
            if (null == tag)
                vm = new ViewModelProvider(owner).get(dependency);
            else
                vm = new ViewModelProvider(owner).get(tag, dependency);
        }
        return vm;
    }
}
