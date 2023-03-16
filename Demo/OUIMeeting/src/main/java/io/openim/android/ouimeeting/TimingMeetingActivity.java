package io.openim.android.ouimeeting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouimeeting.databinding.ActivityTimingMeetingBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;

public class TimingMeetingActivity extends BaseActivity<MeetingVM, ActivityTimingMeetingBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(MeetingVM.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timing_meeting);
    }
}
