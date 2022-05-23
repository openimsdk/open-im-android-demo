package io.openim.android.ouicore.utils;

import java.util.Observable;
import java.util.Observer;

/**
 * 观察者
 */
public class Obs extends Observable {
    private static Obs observer = null;

    public synchronized static Obs getInstance() {
        if (observer == null) {
            observer = new Obs();
        }
        return observer;
    }

    public static void newMessage(int tag) {
        getInstance().setMessage(new Message(tag));
    }

    public static void newMessage(int tag, Object object) {
        getInstance().setMessage(new Message(tag, object));
    }

    private void setMessage(Message message) {
        observer.setChanged();
        observer.notifyObservers(message);
    }

    public static class Message {
        public int tag;
        public Object object;

        public Message(int tag) {
            this.tag = tag;
        }

        public Message(int tag, Object object) {
            this.tag = tag;
            this.object = object;
        }
    }

}

