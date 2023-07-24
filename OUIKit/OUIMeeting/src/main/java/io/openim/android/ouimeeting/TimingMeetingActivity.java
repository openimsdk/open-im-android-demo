package io.openim.android.ouimeeting;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimeeting.databinding.ActivityTimingMeetingBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.sdk.OpenIMClient;

//预约/修改 会议
public class TimingMeetingActivity extends BaseActivity<MeetingVM, ActivityTimingMeetingBinding> implements MeetingVM.Interaction {


    private WaitDialog waitDialog;
    //是否是修改会议信息
    private boolean isUpdateInfo = false;

    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {

    }

    @Override
    public void onSuccess(Object body) {
        waitDialog.dismiss();
        vm.getMeetingInfoList();
        toast(getString(isUpdateInfo ? io.openim.android.ouicore.R.string.edit_succ :
            io.openim.android.ouicore.R.string.successful_agreement));
        finish();
    }

    @Override
    public void onError(String error) {
        waitDialog.dismiss();
        toast(error);
    }


    private BottomPopDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vm=Easy.find(MeetingVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityTimingMeetingBinding.inflate(getLayoutInflater()));
        isUpdateInfo = getIntent().getBooleanExtra(Constant.K_RESULT, false);
        init();
        initView();
        listener();
    }

    void init() {
        vm.timingParameter = new MeetingVM.TimingParameter();
        if (isUpdateInfo) {
            vm.timingParameter.meetingTheme.setValue(vm.selectMeetingInfo.getMeetingName());
            vm.timingParameter.startTime.setValue(vm.selectMeetingInfo.getStartTime());
            vm.timingParameter.duration.setValue((int) (vm.selectMeetingInfo.getEndTime() - vm.selectMeetingInfo.getStartTime()));
        }
    }

    private void initView() {
        view.setTimingParameter(vm.timingParameter);
        waitDialog = new WaitDialog(this);
        view.title.setText(isUpdateInfo ? io.openim.android.ouicore.R.string.update_meeting_info
            : io.openim.android.ouicore.R.string.timing_meeting);
        view.submit.setText(isUpdateInfo ?
            io.openim.android.ouicore.R.string.confirm_modification :
            io.openim.android.ouicore.R.string.timing_meeting);
    }

    private void listener() {
        vm.timingParameter.startTime.observe(this, time -> {
            if (time == 0) return;
            vm.timingParameter.startTimeStr.setValue(TimeUtil.getTime(time * 1000,
                TimeUtil.yearTimeFormat));
        });
        vm.timingParameter.duration.observe(this, duration -> {
            if (duration == 0) return;
            vm.timingParameter.durationStr.setValue((BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(3600))) + getString(io.openim.android.ouicore.R.string.hour));
        });

        view.durationLy.setOnClickListener(v -> {
            if (null == dialog) dialog = new BottomPopDialog(this);
            dialog.getMainView().menu3.setOnClickListener(v1 -> dialog.dismiss());
            dialog.getMainView().menu1.setText("0.5" + getString(io.openim.android.ouicore.R.string.hour));
            dialog.getMainView().menu2.setText("1" + getString(io.openim.android.ouicore.R.string.hour));
            dialog.getMainView().menu4.setVisibility(View.VISIBLE);
            dialog.getMainView().menu4.setText("1.5" + getString(io.openim.android.ouicore.R.string.hour));
            dialog.getMainView().menu5.setVisibility(View.VISIBLE);
            dialog.getMainView().menu5.setText("2" + getString(io.openim.android.ouicore.R.string.hour));
            dialog.getMainView().menu1.setOnClickListener(v1 -> setDuration(0.5));
            dialog.getMainView().menu2.setOnClickListener(v1 -> setDuration(1.0));
            dialog.getMainView().menu4.setOnClickListener(v1 -> setDuration(1.5));
            dialog.getMainView().menu5.setOnClickListener(v1 -> setDuration(2.0));
            dialog.show();
        });
        view.startTimeLy.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 5);
            TimePickerView pvTime = new TimePickerBuilder(this, (date, v1) -> {
                long time = date.getTime() / 1000;
                vm.timingParameter.startTime.setValue(time);

            }).setType(new boolean[]{true, true, true, true, true, false}).setRangDate(Calendar.getInstance(), calendar).build();
            pvTime.show(v);
        });
        view.submit.setOnClickListener(v -> {
            String themeValue = vm.timingParameter.meetingTheme.getValue();
            long startTime = vm.timingParameter.startTime.getValue();
            int duration = vm.timingParameter.duration.getValue();

            if (TextUtils.isEmpty(themeValue)) {
                toast(getString(io.openim.android.ouicore.R.string.timing_tips1));
                return;
            }
            if (startTime == 0) {
                toast(getString(io.openim.android.ouicore.R.string.timing_tips2));
                return;
            }
            if (duration == 0) {
                toast(getString(io.openim.android.ouicore.R.string.timing_tips3));
                return;
            }
            waitDialog.show();
            if (isUpdateInfo) {
                vm.selectMeetingInfo.setMeetingName(themeValue);
                vm.selectMeetingInfo.setStartTime(startTime);
                vm.selectMeetingInfo.setEndTime(startTime + duration);


                Map<String, Object> map =
                    JSONObject.parseObject(GsonHel.toJson(vm.selectMeetingInfo), Map.class);
                map.put("roomID", vm.selectMeetingInfo.getMeetingID());

                vm.updateMeetingInfo(map, this::onSuccess);
            } else vm.createMeeting(themeValue, startTime, duration);
        });
    }

    private void setDuration(double hour) {
        dialog.dismiss();
        vm.timingParameter.duration.setValue((int) (hour * 3600));
    }
}
