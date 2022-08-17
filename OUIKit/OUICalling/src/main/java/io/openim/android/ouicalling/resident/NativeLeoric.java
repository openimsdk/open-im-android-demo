
package io.openim.android.ouicalling.resident;

/* package */class NativeLeoric {

    static {
        try {
            System.loadLibrary("leoric");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public native void doDaemon(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath);

    public void onDaemonDead() {
        ILeoricProcess.Fetcher.fetchStrategy().onDaemonDead();
    }
}
