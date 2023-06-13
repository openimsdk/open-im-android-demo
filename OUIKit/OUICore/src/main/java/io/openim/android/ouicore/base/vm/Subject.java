package io.openim.android.ouicore.base.vm;

import java.util.Objects;

public class Subject {
    public String key;
    public Object value;

    public Subject(String key) {
        this.key = key;
    }

    public Subject(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Subject) {
            Subject subject = (Subject) o;
            return Objects.equals(key, subject.key);
        }
        return this.key == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
