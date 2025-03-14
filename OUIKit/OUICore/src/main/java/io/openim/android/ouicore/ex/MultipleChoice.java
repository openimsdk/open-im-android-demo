package io.openim.android.ouicore.ex;

import java.io.Serializable;

public class MultipleChoice extends CommEx implements Serializable {
    public MultipleChoice() {
    }

    public MultipleChoice(String key) {
        this.key = key;
    }

    public String name;
    public String icon;
    public boolean isGroup;
    public String groupId;
}
