package io.openim.android.ouicore.ex;

import java.io.Serializable;

public class AtUser extends UserEx implements Serializable {


    public AtUser(String key) {
        super(key);
    }
    //用于在消息输入框监听删除键时 判断删除对应@的人
    public int spanHashCode;
}
