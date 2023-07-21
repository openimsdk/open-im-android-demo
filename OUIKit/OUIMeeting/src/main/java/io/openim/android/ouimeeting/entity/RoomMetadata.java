package io.openim.android.ouimeeting.entity;

import java.util.List;

public class RoomMetadata {
    public String roomID;
    public String meetingName;
    public String ex;
    public String hostUserID;
    public List<String> inviteeUserIDList;
    public long createTime;
    public long startTime;
    public long endTime;
    public boolean participantCanUnmuteSelf;
    public boolean participantCanEnableVideo;
    public boolean onlyHostInviteUser;
    public boolean onlyHostShareScreen;
    public boolean joinDisableMicrophone;
    public boolean joinDisableVideo;
    public boolean isMuteAllVideo;
    public boolean isMuteAllMicrophone;

    public List<String> canScreenUserIDList;
    //   暂不使用
    public List<String> disableMicrophoneUserIDList;
    public List<String> disableVideoUserIDList;
    //    ------
    public List<String> pinedUserIDList;
    public List<String> beWatchedUserIDList;
}
