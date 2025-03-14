package io.openim.android.ouicore.utils;

import java.util.LinkedList;

public class FixSizeLinkedList<T> extends LinkedList<T> {

    private int capacity;

    public FixSizeLinkedList(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        if (size() > capacity) {
            super.removeFirst();
        }
    }

    @Override
    public boolean add(T t) {
        if (size() + 1 > capacity) {
            super.removeFirst();
        }
        return super.add(t);
    }
}
