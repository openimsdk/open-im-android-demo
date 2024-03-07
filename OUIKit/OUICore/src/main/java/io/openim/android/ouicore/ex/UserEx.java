package io.openim.android.ouicore.ex;

import java.io.Serializable;

public class UserEx extends Ex implements Serializable {

    public UserEx(String key) {
        super(key);
    }
    public String name;
    public String faceUrl;
}
