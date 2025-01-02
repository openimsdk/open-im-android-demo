package io.openim.android.ouicore.entity;

import io.openim.android.ouicore.utils.L;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CallHistory extends RealmObject {
    @PrimaryKey
    private String id;
    private String userID;
    private String nickname;
    private String faceURL;
    private String type;
    private boolean success;
    /**
     * Client's platform. Only accepted the calling invitation can be used. Default is Android, if didn't call setPlatform method.
     * <p>
     * 0: Android
     * 1: Others
     */
    private int platform;
    /**
     * Failed state code.
     * <p>
     * 0: Connection
     * 1: Canceled
     * 2: Refused by invitor
     * 3: Refused by us
     * 4: Timeout
     */
    private int failedState;
    private boolean incomingCall;
    private long date;
    private int duration;


    public CallHistory() {
    }

    public CallHistory(String id, String userID, String nickname, String faceURL, String type, boolean success, int failedState, boolean incomingCall, long date, int duration) {
        this.id = id;
        this.userID = userID;
        this.nickname = nickname;
        this.faceURL = faceURL;
        this.type = type;
        this.success = success;
        this.failedState = failedState;
        this.incomingCall = incomingCall;
        this.date = date;
        this.duration = duration;
        this.platform = 0;
    }

    public int getFailedState() {
        return failedState;
    }

    /**
     * Modified state code when the call failed
     * <p>
     * 0: Connection
     * 1: Canceled
     * 2: Refused by invitor
     * 3: Refused by us
     * 4: Timeout
     * @param failedState failed state code
     */
    public void setFailedState(int failedState) {
        this.failedState = failedState;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFaceURL() {
        return faceURL;
    }

    public void setFaceURL(String faceURL) {
        this.faceURL = faceURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isIncomingCall() {
        return incomingCall;
    }

    public void setIncomingCall(boolean incomingCall) {
        this.incomingCall = incomingCall;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        L.e("======================="+duration);
        this.duration = duration;
    }

    /**
     * Client's platform
     * <p>
     * 0: Android
     * 1: Others
     * @param platform
     */
    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public int getPlatform() {
        return platform;
    }
}
