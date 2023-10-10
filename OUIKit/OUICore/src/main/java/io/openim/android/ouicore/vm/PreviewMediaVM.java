package io.openim.android.ouicore.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.openim.android.ouicore.base.vm.injection.BaseVM;

public class PreviewMediaVM extends BaseVM {
    public List<MediaData> mediaDataList = new ArrayList<>();
    public int currentIndex; //当前选择

    public  void previewSingle(MediaData mediaData){
        mediaDataList.add(mediaData);
    }
    public  void previewMultiple(List<MediaData> list,String currentUrl){
        mediaDataList=list;
        int index=mediaDataList.indexOf(new MediaData(currentUrl));
        if (index>=0)
            currentIndex=index;
    }

    public static class MediaData {
        public boolean isVideo;
        public String mediaUrl;
        public String thumbnail; //缩略图或第一帧

        public MediaData(String mediaUrl) {
            this.mediaUrl = mediaUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MediaData)) return false;
            MediaData mediaData = (MediaData) o;
            return Objects.equals(mediaUrl, mediaData.mediaUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mediaUrl);
        }
    }
}
