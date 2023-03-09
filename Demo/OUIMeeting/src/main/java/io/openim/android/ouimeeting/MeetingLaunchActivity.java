package io.openim.android.ouimeeting;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouimeeting.databinding.ActivityMeetingHomeBinding;
import io.openim.android.ouimeeting.databinding.ActivityMeetingLaunchBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.sdk.models.SignalingCertificate;

public class MeetingLaunchActivity extends BaseActivity<MeetingVM, ActivityMeetingLaunchBinding>implements MeetingVM.Interaction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(MeetingVM.class,true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMeetingLaunchBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    private void listener() {
        view.timely.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                vm.fastMeeting();
            }
        });
        view.join.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                vm.joinMeeting("2486652218");
            }
        });
    }

    @Override
    public void onSuccess(Object body) {
        startActivity(new Intent(MeetingLaunchActivity.this,
            MeetingHomeActivity.class));
    }

    private void initView() {
        view.recyclerView.setAdapter(new RecyclerViewAdapter() {
            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, Object data,
                                   int position) {

            }
        });
    }

    void init() {

    }

    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {

    }
}
