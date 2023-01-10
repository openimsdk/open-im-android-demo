package io.openim.android.ouicore.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CallHistory extends RealmObject {
    @PrimaryKey
    private String roomID;
    private String userID;
    private String nickname;
    private String faceURL;
    private String type;
    private boolean success;
    //失败状态 0连接中 1取消 2对方拒绝
    private int failedState;
    private boolean incomingCall;
    private long date;
    private int duration;


    public CallHistory() {
    }

    public CallHistory(String roomID, String userID, String nickname, String faceURL, String type, boolean success, int failedState, boolean incomingCall, long date, int duration) {
        this.roomID = roomID;
        this.userID = userID;
        this.nickname = nickname;
        this.faceURL = faceURL;
        this.type = type;
        this.success = success;
        this.failedState = failedState;
        this.incomingCall = incomingCall;
        this.date = date;
        this.duration = duration;
    }

    public int getFailedState() {
        return failedState;
    }

    /**
     *
     * @param failedState 失败状态 0连接中 1取消 2对方拒绝
     */
    public void setFailedState(int failedState) {
        this.failedState = failedState;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
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
        this.duration = duration;
    }
}
