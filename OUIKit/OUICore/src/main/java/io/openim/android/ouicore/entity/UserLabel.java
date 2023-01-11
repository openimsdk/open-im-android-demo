package io.openim.android.ouicore.entity;

import java.util.List;

public class UserLabel {
    public UserLabel() {
    }

    private List<UserLabel> tags;

    private String tagID;

    private String tagName;

    private List<UserLabelChild> userList;

    public List<UserLabel> getTags() {
        return tags;
    }

    public void setTags(List<UserLabel> tags) {
        this.tags = tags;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public String getTagID() {
        return this.tagID;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return this.tagName;
    }

    public void setUserList(List<UserLabelChild> userList) {
        this.userList = userList;
    }

    public List<UserLabelChild> getUserList() {
        return this.userList;
    }
}
