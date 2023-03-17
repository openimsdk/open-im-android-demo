package io.openim.android.ouicore.services;

import com.alibaba.android.arouter.facade.template.IProvider;

public interface IMeetingBridge extends IProvider {

    public void joinMeeting(String roomID);

}
