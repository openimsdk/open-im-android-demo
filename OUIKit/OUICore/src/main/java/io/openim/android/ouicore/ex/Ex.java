package io.openim.android.ouicore.ex;

import androidx.annotation.Nullable;

import java.util.Objects;

public class Ex {
    public String key; //Id

    public Ex(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (null != obj) {
            if (hashCode() ==
                obj.hashCode()) return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
