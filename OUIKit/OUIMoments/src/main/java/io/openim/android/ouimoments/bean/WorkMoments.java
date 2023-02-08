package io.openim.android.ouimoments.bean;

import java.util.List;

public class WorkMoments {
    public String workMomentID;
    public String userID;
    public String content;
    public MomentsContent momentsContents;
    public List<MomentsUser> likeUsers;
    public List<Comment> comments;
    public String faceURL;
    public String userName;
    public List<String> atUsers;
    public List<MomentsUser> permissionUsers;
    public long createTime;
    public int permission;
}
