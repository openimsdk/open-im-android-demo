package io.openim.android.ouimoments.bean;

import io.openim.android.sdk.models.WorkMomentsInfo;

//扩展
public class EXWorkMomentsInfo   {
    public EXWorkMomentsInfo(MomentsContent contentBean, WorkMomentsInfo workMomentsInfo) {
        this.contentBean = contentBean;
        this.workMomentsInfo = workMomentsInfo;
    }

    public MomentsContent contentBean;
    public WorkMomentsInfo workMomentsInfo;
}
