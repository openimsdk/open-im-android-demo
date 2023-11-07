package io.openim.android.ouicore.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.ex.Ex;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Common;

public class PreviewMediaVM extends BaseVM {
    public List<MediaData> mediaDataList = new ArrayList<>();
    public int currentIndex; //当前选择

    public  void previewSingle(MediaData mediaData){
        mediaDataList.add(mediaData);
    }
    public  void previewMultiple(List<MediaData> list,String key){
        mediaDataList=list;
        int index=mediaDataList.indexOf(new MediaData(key));
        if (index>=0)
            currentIndex=index;
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
