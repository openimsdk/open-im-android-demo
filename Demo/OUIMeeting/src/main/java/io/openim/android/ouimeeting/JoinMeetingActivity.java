package io.openim.android.ouimeeting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimeeting.databinding.ActivityJoinMeetingBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;

public class JoinMeetingActivity extends BaseActivity<MeetingVM, ActivityJoinMeetingBinding> implements MeetingVM.Interaction {

    private WaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(MeetingVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityJoinMeetingBinding.inflate(getLayoutInflater()));
        listener();
    }

    private void listener() {
        view.join.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                String num = view.meetingNum.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    toast(getString(io.openim.android.ouicore.R.string.please_input_meeting_num));
                    return;
                }
                waitDialog = new WaitDialog(JoinMeetingActivity.this);
                waitDialog.show();
                vm.joinMeeting(num);
            }
        });
    }

    @Override
    public void onError(String error) {
        waitDialog.dismiss();
        toast(error);
    }


    @Override
    public void onSuccess(Object body) {
        waitDialog.dismiss();
        finish();
        startActivity(new Intent(this, MeetingHomeActivity.class));
    }

    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {

    }
}
