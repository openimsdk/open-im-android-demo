package io.openim.android.ouicore.vm;

import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.ex.Ex;

public class PreviewMediaVM extends BaseVM {
    public MediaData mediaData;
    public int currentIndex; //当前选择

    public  void preview(MediaData mediaData){
        this.mediaData = mediaData;
    }

    public static class MediaData extends Ex {
        public boolean isVideo;
        public String mediaUrl;
        public String thumbnail; //缩略图或第一帧

        public MediaData(String key) {
            super(key);
        }
    }
}
