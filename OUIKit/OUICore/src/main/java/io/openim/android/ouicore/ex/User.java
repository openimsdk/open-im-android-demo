package io.openim.android.ouicore.ex;

import java.io.Serializable;

public class User extends Ex implements Serializable {
    public User(String key) {
        super(key);
    }
    public String name;
    public String faceUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        this.faceUrl = faceUrl;
    }
}
