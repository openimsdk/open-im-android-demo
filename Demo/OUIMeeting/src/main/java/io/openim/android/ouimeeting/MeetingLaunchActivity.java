package io.openim.android.ouimeeting;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.List;

import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimeeting.databinding.ActivityMeetingHomeBinding;
import io.openim.android.ouimeeting.databinding.ActivityMeetingLaunchBinding;
import io.openim.android.ouimeeting.databinding.MeetingHomeIietmMemberBinding;
import io.openim.android.ouimeeting.databinding.MeetingIietmMemberBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.MeetingInfo;
import io.openim.android.sdk.models.MeetingInfoList;
import io.openim.android.sdk.models.SignalingCertificate;

@Route(path = Routes.Meeting.HOME)
public class MeetingLaunchActivity extends BaseActivity<MeetingVM, ActivityMeetingLaunchBinding> implements MeetingVM.Interaction {

    private RecyclerViewAdapter<MeetingInfo, MeetingItemViewHolder> adapter;
    private WaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(MeetingVM.class, true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMeetingLaunchBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    private void listener() {
        view.timing.setOnClickListener(v -> {

        });
        view.timely.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                createWait();
                vm.fastMeeting();
            }
        });
        view.join.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                startActivity(new Intent(MeetingLaunchActivity.this, JoinMeetingActivity.class));
            }
        });
        vm.meetingInfoList.observe(this, meetingInfos -> {
            adapter.setItems(meetingInfos);
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
        vm.getMeetingInfoList();
        //这里有可能被释放 所以需要重新放入
        BaseApp.inst().putVM(vm);
        meetingHomeActivityCallBack.launch(new Intent(MeetingLaunchActivity.this,
            MeetingHomeActivity.class));
    }

    private ActivityResultLauncher<Intent> meetingHomeActivityCallBack =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                vm.getMeetingInfoList();
            }
        });

    private void initView() {
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<MeetingInfo,
            MeetingItemViewHolder>(MeetingItemViewHolder.class) {

            @Override
            public void onBindView(@NonNull MeetingItemViewHolder holder, MeetingInfo data,
                                   int position) {
                try {
                    holder.view.title.setText(data.getMeetingName());
                    boolean isStart = data.getCreateTime() < (System.currentTimeMillis() / 1000L);
                    holder.view.status.setText(getString(isStart ?
                        io.openim.android.ouicore.R.string.have_begun :
                        io.openim.android.ouicore.R.string.not_started));
                    holder.view.status.setBackgroundResource(isStart ?
                        io.openim.android.ouicore.R.drawable.sty_radius_3_ffffb300 :
                        io.openim.android.ouicore.R.drawable.sty_radius_3_ff0089ff);

                    holder.view.description.setText(TimeUtil.getTime(data.getCreateTime() * 1000,
                        TimeUtil.yearMonthDayFormat) + "\t\t\t" + TimeUtil.getTime(data.getStartTime() * 1000, TimeUtil.hourTimeFormat) + "-" + TimeUtil.getTime(data.getEndTime() * 1000, TimeUtil.hourTimeFormat) + "\t\t\t" + String.format(getString(io.openim.android.ouicore.R.string.initiator), vm.userInfos.get(position).getNickname()));


                    holder.view.join.setOnClickListener(v -> {
                        createWait();
                        vm.joinMeeting(data.getMeetingID());
                    });
                } catch (Exception ignored) {

                }
            }
        });

    }

    private void createWait() {
        waitDialog = new WaitDialog(MeetingLaunchActivity.this);
        waitDialog.show();
    }

    void init() {
        vm.getMeetingInfoList();
    }

    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }

    public static class MeetingItemViewHolder extends RecyclerView.ViewHolder {
        public final MeetingHomeIietmMemberBinding view;

        public MeetingItemViewHolder(@NonNull View itemView) {
            super(MeetingHomeIietmMemberBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = MeetingHomeIietmMemberBinding.bind(this.itemView);
        }
    }

}
