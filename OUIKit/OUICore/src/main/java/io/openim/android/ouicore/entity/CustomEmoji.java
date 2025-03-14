package io.openim.android.ouicore.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CustomEmoji extends RealmObject {
    @PrimaryKey
    private String sourceUrl;
    private String userID;
    private String thumbnailUrl;
    //尺寸
    private int thumbnailW;
    private int thumbnailH;
    private int sourceW;
    private int sourceH;

    public CustomEmoji() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public int getThumbnailW() {
        return thumbnailW;
    }

    public void setThumbnailW(int thumbnailW) {
        this.thumbnailW = thumbnailW;
    }

    public int getThumbnailH() {
        return thumbnailH;
    }

    public void setThumbnailH(int thumbnailH) {
        this.thumbnailH = thumbnailH;
    }

    public int getSourceW() {
        return sourceW;
    }

    public void setSourceW(int sourceW) {
        this.sourceW = sourceW;
    }

    public int getSourceH() {
        return sourceH;
    }

    public void setSourceH(int sourceH) {
        this.sourceH = sourceH;
    }
}
