package io.openim.android.ouimeeting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimeeting.databinding.ActivityMeetingDetailBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

public class MeetingDetailActivity extends BaseActivity<MeetingVM, ActivityMeetingDetailBinding> implements MeetingVM.Interaction {

    private BottomPopDialog popDialog;
    private WaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(MeetingVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMeetingDetailBinding.inflate(getLayoutInflater()));
        initView();
        listener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == Constant.Event.FORWARD && null != data) {
            //在这里转发
            String id = data.getStringExtra(Constant.K_ID);
            String otherSideNickName = data.getStringExtra(Constant.K_NAME);
            String groupId = data.getStringExtra(Constant.K_GROUP_ID);
            vm.inviterUser(id, groupId);
        }
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

    private void listener() {
        view.join.setOnClickListener(v -> {
            createWait();
            vm.joinMeeting(vm.selectMeetingInfo.getMeetingID());
        });
        view.forward.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Contact.FORWARD).navigation(this,
                Constant.Event.FORWARD);
        });
        view.more.setOnClickListener(v -> {
            if (null == popDialog) {
                popDialog = new BottomPopDialog(this);
                popDialog.show();
            } else {
                popDialog.show();
                return;
            }
            popDialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.update_meeting_info);
            popDialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.cancel_meeting);
            popDialog.getMainView().menu3.setOnClickListener(v1 -> popDialog.dismiss());

            popDialog.getMainView().menu1.setOnClickListener(v1 -> {
                popDialog.dismiss();
                finish();
                startActivity(new Intent(this, TimingMeetingActivity.class).putExtra(Constant.K_RESULT, true));
            });
            popDialog.getMainView().menu2.setOnClickListener(v1 -> {
                CommonDialog commonDialog = new CommonDialog(this).atShow();
                commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.exit_meeting_tips2);
                commonDialog.getMainView().cancel.setOnClickListener(v2 -> commonDialog.dismiss());
                commonDialog.getMainView().confirm.setOnClickListener(v2 -> {
                    commonDialog.dismiss();
                    vm.finishMeeting(vm.selectMeetingInfo.getMeetingID());
                    finish();
                });
            });
        });
    }

    private void createWait() {
        waitDialog = new WaitDialog(this);
        waitDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        view.setMeetingVM(vm);
        String startTime1 = TimeUtil.getTime(vm.selectMeetingInfo.getStartTime() * 1000,
            TimeUtil.hourTimeFormat);
        String startTime2 = TimeUtil.getTime(vm.selectMeetingInfo.getStartTime() * 1000,
            TimeUtil.yearMonthDayFormat);
        view.startTime.setText(startTime1);
        view.startTime2.setText(startTime2);

        boolean isStart =
            vm.selectMeetingInfo.getCreateTime() < (System.currentTimeMillis() / 1000L);
        view.status.setText(getString(isStart ? io.openim.android.ouicore.R.string.have_begun :
            io.openim.android.ouicore.R.string.not_started));
        view.status.setBackgroundResource(isStart ?
            io.openim.android.ouicore.R.drawable.sty_radius_3_ffffb300 :
            io.openim.android.ouicore.R.drawable.sty_radius_3_ff0089ff);
        view.duration.setText((BigDecimal.valueOf(vm.selectMeetingInfo.getEndTime()
            - vm.selectMeetingInfo.getStartTime()).divide(BigDecimal.valueOf(3600),
            1, BigDecimal.ROUND_HALF_DOWN)) + getString(io.openim.android.ouicore.R.string.hour));

        String endTime1 = TimeUtil.getTime(vm.selectMeetingInfo.getEndTime() * 1000,
            TimeUtil.hourTimeFormat);
        String endTime2 = TimeUtil.getTime(vm.selectMeetingInfo.getEndTime() * 1000,
            TimeUtil.yearMonthDayFormat);
        view.endTime.setText(endTime1);
        view.endTime2.setText(endTime2);

        view.meetingNum.setText(getString(io.openim.android.ouicore.R.string.meeting_num) + "：" + vm.selectMeetingInfo.getMeetingID());
        List<String> ids = new ArrayList<>();
        ids.add(vm.selectMeetingInfo.getHostUserID());
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error);
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                view.initiator.setText(String.format(getString(io.openim.android.ouicore.R.string.initiator), data.get(0).getNickname()));
            }
        }, ids);
    }

    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {

    }
}
