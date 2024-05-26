package io.openim.android.ouimeeting;

import static io.openim.android.ouimeeting.vm.CallViewModelKt.getIdentity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;
import com.hjq.window.EasyWindow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.livekit.android.events.DisconnectReason;
import io.livekit.android.events.RoomEvent;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.ViewRecyclerViewBinding;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.CustomPositionDrawableSpan;
import io.openim.android.ouicore.widget.GridSpaceItemDecoration;
import io.openim.android.ouimeeting.databinding.ActivityMeetingHomeBinding;
import io.openim.android.ouimeeting.databinding.LayoutMeetingInfoDialogBinding;
import io.openim.android.ouimeeting.databinding.LayoutMemberDialogBinding;
import io.openim.android.ouimeeting.databinding.LayoutSettingDialogBinding;
import io.openim.android.ouimeeting.databinding.MeetingIietmMemberBinding;
import io.openim.android.ouimeeting.databinding.MenuUserSettingBinding;
import io.openim.android.ouimeeting.databinding.ViewMeetingFloatBinding;
import io.openim.android.ouimeeting.entity.RoomMetadata;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.ouimeeting.widget.SingleTextureView;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Meeting.HOME)
public class MeetingHomeActivity extends BaseActivity<MeetingVM, ActivityMeetingHomeBinding> implements MeetingVM.Interaction {

    private PageAdapter adapter;
    private BottomPopDialog bottomPopDialog, settingPopDialog, meetingInfoPopDialog, exitPopDialog;
    private RecyclerViewAdapter<Participant, MemberItemViewHolder> memberAdapter;
    //触发横屏 决定当前绑定的MeetingVM 是否释放
    private boolean triggerLandscape = false;
    private List<Participant> memberParticipants = new ArrayList<>();
    private Participant activeSpeaker;
    //每页显示多少Participant
    private final int pageShow = 4;
    private List<View> guideViews = new ArrayList<>();
    private EasyWindow<?> easyWindow;
    private Observer<? super Boolean> isAppBackgroundListener;
    //恢复当前页面
    boolean isRecover = false;
    private HasPermissions shoot, hasSystemAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vm = Easy.find(MeetingVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMeetingHomeBinding.inflate(getLayoutInflater()));
        shoot = new HasPermissions(this, Permission.RECORD_AUDIO, Permission.CAMERA);
        hasSystemAlert = new HasPermissions(this, Permission.SYSTEM_ALERT_WINDOW);

        shoot.safeGo(new HasPermissions.OnGrantedListener() {
            @Override
            public void onGranted() {
                initView();
                if (vm.isInit) {
                    if (vm.isLandscape)
                        toast(getString(io.openim.android.ouicore.R.string.double_tap_tips));
                    connectRoomSuccess(vm.callViewModel.getVideoTrack(vm.callViewModel.getRoom().getLocalParticipant()));
                } else init();

                bindVM();
                listener();
                registerHomeKey(MeetingHomeActivity.this);
            }

            @Override
            public void onDeniedPart(@NonNull List<String> permissions, boolean doNotAskAgain) {
                HasPermissions.OnGrantedListener.super.onDeniedPart(permissions, doNotAskAgain);
                finish();
            }
        });
    }

    //监听Home键
    private HomeWatcherReceiver mHomeKeyReceiver;

    public void registerHomeKey(Context context) {
        //注册Home监听广播
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    private class HomeWatcherReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    isRecover = true;
                }
            }
        }
    }


    @Override
    protected void requestedOrientation() {
    }

    @Override
    public void onBackPressed() {
    }

    //分享屏幕
    private ActivityResultLauncher<Intent> screenCaptureIntentLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        Intent data = result.getData();
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        vm.startShareScreen(data);
        toast(getString(io.openim.android.ouicore.R.string.share_screen));
    });


    private static void moveTaskToFront(int taskId) {
        android.app.ActivityManager manager =
            (android.app.ActivityManager) BaseApp.inst().getSystemService(ACTIVITY_SERVICE);
        manager.moveTaskToFront(taskId, android.app.ActivityManager.MOVE_TASK_WITH_HOME);
        L.e("---moveTaskToFront----=" + taskId);
    }


    private void showFloatView() {
        // 传入 Activity 对象表示设置成局部的，不需要有悬浮窗权限
        // 传入 Application 对象表示设置成全局的，但需要有悬浮窗权限
        if (null == easyWindow) {
            ViewMeetingFloatBinding floatView =
                ViewMeetingFloatBinding.inflate(getLayoutInflater());
            easyWindow =
                new EasyWindow<>(BaseApp.inst()).setContentView(floatView.getRoot()).setWidth(Common.dp2px(107)).setHeight(Common.dp2px(160)).setGravity(Gravity.RIGHT | Gravity.TOP)
                // 设置成可拖拽的
                .setDraggable().setOnClickListener(floatView.getRoot().getId(), (window, view) -> {
                    Postcard postcard = ARouter.getInstance().build(Routes.Meeting.HOME);
                    LogisticsCenter.completion(postcard);
                    Activity activity = ActivityManager.isExist(postcard.getDestination());
                    if (null != activity) {
                        moveTaskToFront(activity.getTaskId());
                        easyWindow.cancel();
                    }
                });
        }
        if (!easyWindow.isShowing()) easyWindow.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        EasyWindow.cancelAll();
    }


    private boolean getViewBooleanTag(View v) {
        Object tag = v.getTag();
        if (null == tag) tag = false;
        return (boolean) tag;
    }

    private void exit(Boolean isFinishMeeting) {
        CommonDialog commonDialog = new CommonDialog(this).atShow();
        commonDialog.getMainView().tips.setText(isFinishMeeting ?
            io.openim.android.ouicore.R.string.exit_meeting_tips2 :
            io.openim.android.ouicore.R.string.exit_meeting_tips);
        commonDialog.getMainView().cancel.setOnClickListener(v1 -> commonDialog.dismiss());
        commonDialog.getMainView().confirm.setOnClickListener(v1 -> {
            commonDialog.dismiss();
            if (isFinishMeeting) {
                vm.finishMeeting(vm.roomMetadata.val().roomID);
            }
            finish();
        });
    }


    private View buildMeetingInfoPopDialogView() {
        LayoutMeetingInfoDialogBinding v =
            LayoutMeetingInfoDialogBinding.inflate(getLayoutInflater());
        RoomMetadata roomMetadata = vm.roomMetadata.getValue();
        v.title.setText(roomMetadata.meetingName);
        List<String> ids = new ArrayList<>();
        ids.add(roomMetadata.hostUserID);
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                BigDecimal bigDecimal =
                    (BigDecimal.valueOf(roomMetadata.endTime - roomMetadata.startTime).divide(BigDecimal.valueOf(3600), 1, BigDecimal.ROUND_HALF_DOWN));
                String durationStr =
                    bigDecimal.toString() + BaseApp.inst().getString(io.openim.android.ouicore.R.string.hour);

                String meetingNum=getString(io.openim.android.ouicore.R.string.meeting_num) + "：" + roomMetadata.roomID + "\t";
                String main="\n" + getString(io.openim.android.ouicore.R.string.emcee)
                    + "：" + data.get(0).getNickname()
                    + "\n" + getString(io.openim.android.ouicore.R.string.start_time)
                    + "：" + TimeUtil.getTime(roomMetadata.startTime * 1000, TimeUtil.yearMonthDayFormat)
                    + "\t\t" + TimeUtil.getTime(roomMetadata.startTime * 1000, TimeUtil.hourTimeFormat)
                    + "\n" + getString(io.openim.android.ouicore.R.string.meeting_duration) + "：" + durationStr;
                SpannableStringBuilder spannableString = new SpannableStringBuilder(meetingNum+main);
                Drawable drawable = ResourcesCompat.getDrawable(getResources(),io.openim.android.ouicore.R.mipmap.ic__copy,null);
                CustomPositionDrawableSpan imageSpan =
                    new CustomPositionDrawableSpan(drawable,Common.dp2px(10),-Common.dp2px(10));
                spannableString.setSpan(imageSpan,meetingNum.length()-1,meetingNum.length(),
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                v.description.setText(spannableString);

                v.description.setOnClickListener(new OnDedrepClickListener() {
                    @Override
                    public void click(View v) {
                        Common.copy(roomMetadata.roomID);
                        toast(getString(io.openim.android.ouicore.R.string.copy_succ));
                    }
                });

            }
        }, ids);
        return v.getRoot();
    }

    //设置弹窗
    private View buildSettingPopView() {
        LayoutSettingDialogBinding v = LayoutSettingDialogBinding.inflate(getLayoutInflater());
        RoomMetadata roomMetadata = vm.roomMetadata.getValue();

        AtomicBoolean participantCanUnmuteSelf =
            new AtomicBoolean(roomMetadata.participantCanUnmuteSelf);
        AtomicBoolean participantCanEnableVideo =
            new AtomicBoolean(roomMetadata.participantCanEnableVideo);
        AtomicBoolean onlyHostInviteUser = new AtomicBoolean(roomMetadata.onlyHostInviteUser);
        AtomicBoolean onlyHostShareScreen = new AtomicBoolean(roomMetadata.onlyHostShareScreen);
        AtomicBoolean joinDisableMicrophone = new AtomicBoolean(roomMetadata.joinDisableMicrophone);

        v.allowCancelMute.setCheckedWithAnimation(participantCanUnmuteSelf.get());
        v.allowOpenCamera.setCheckedWithAnimation(participantCanEnableVideo.get());
        v.onlyHostShare.setCheckedWithAnimation(onlyHostShareScreen.get());
        v.onlyHostInvite.setCheckedWithAnimation(onlyHostInviteUser.get());
        v.joinMute.setCheckedWithAnimation(joinDisableMicrophone.get());

        v.allowCancelMute.setOnSlideButtonClickListener(isChecked -> participantCanUnmuteSelf.set(isChecked));
        v.allowOpenCamera.setOnSlideButtonClickListener(isChecked -> participantCanEnableVideo.set(isChecked));
        v.onlyHostShare.setOnSlideButtonClickListener(isChecked -> onlyHostShareScreen.set(isChecked));
        v.onlyHostInvite.setOnSlideButtonClickListener(isChecked -> onlyHostInviteUser.set(isChecked));
        v.joinMute.setOnSlideButtonClickListener(isChecked -> joinDisableMicrophone.set(isChecked));
        v.sure.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                Map<String, Object> map = new HashMap<>();
                map.put("roomID", vm.signalingCertificate.getRoomID());
                map.put("participantCanUnmuteSelf", participantCanUnmuteSelf.get());
                map.put("participantCanEnableVideo", participantCanEnableVideo.get());
                map.put("onlyHostInviteUser", onlyHostInviteUser.get());
                map.put("onlyHostShareScreen", onlyHostShareScreen.get());
                map.put("joinDisableMicrophone", joinDisableMicrophone.get());

                vm.updateMeetingInfo(map, data -> {
                    roomMetadata.participantCanUnmuteSelf = participantCanUnmuteSelf.get();
                    roomMetadata.participantCanEnableVideo = participantCanEnableVideo.get();
                    roomMetadata.onlyHostInviteUser = onlyHostInviteUser.get();
                    roomMetadata.onlyHostShareScreen = onlyHostShareScreen.get();
                    roomMetadata.joinDisableMicrophone = joinDisableMicrophone.get();

                    vm.roomMetadata.setValue(roomMetadata);
                    settingPopDialog.dismiss();
                });
            }
        });
        return v.getRoot();
    }


    //成员弹窗
    private View buildPopView() {
        LayoutMemberDialogBinding v = LayoutMemberDialogBinding.inflate(getLayoutInflater());
        v.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        v.close.setOnClickListener(v1 -> bottomPopDialog.dismiss());
        v.recyclerView.setAdapter(memberAdapter = new RecyclerViewAdapter<Participant,
            MemberItemViewHolder>(MemberItemViewHolder.class) {

            @Override
            public void onBindView(@NonNull MemberItemViewHolder holder, Participant data,
                                   int position) {
                try {
                    ParticipantMeta participantMeta = GsonHel.fromJson(data.getMetadata(),
                        ParticipantMeta.class);
                    holder.view.avatar.load(participantMeta.userInfo.getFaceURL());
                    holder.view.name.setText(vm.getMetaUserName(participantMeta));

                    if (vm.isSelfHostUser.val()) {
                        holder.view.mic.setVisibility(View.VISIBLE);
                        holder.view.camera.setVisibility(View.VISIBLE);
                        holder.view.mic.setChecked(data.isMicrophoneEnabled());
                        holder.view.camera.setEnabled(!data.isScreenShareEnabled());
                        holder.view.camera.setAlpha(data.isScreenShareEnabled() ? 0.5F : 1);
                        holder.view.camera.setChecked(data.isCameraEnabled());
                        holder.view.allSeeIv.setVisibility(vm.isAllSeeHe(data) ? View.VISIBLE :
                            View.GONE);

                        holder.view.mic.setOnClickListener(new OnDedrepClickListener() {
                            @Override
                            public void click(View v) {
                                vm.muteMic(getIdentity(data), !holder.view.mic.isChecked());
                            }
                        });
                        holder.view.camera.setOnClickListener(new OnDedrepClickListener() {
                            @Override
                            public void click(View v) {
                                vm.muteCamera(getIdentity(data), !holder.view.camera.isChecked());
                            }
                        });
                        holder.view.more.setOnClickListener(v -> showPopupWindow(v, data,
                            participantMeta));
                    } else {
                        holder.view.mic.setVisibility(View.GONE);
                        holder.view.camera.setVisibility(View.GONE);
                        holder.view.more.setVisibility(View.GONE);
                        holder.view.allSeeIv.setVisibility(View.GONE);
                    }
                    holder.view.angleMark.setVisibility(participantMeta.setTop ? View.VISIBLE :
                        View.GONE);
                } catch (Exception ignored) {
                }
            }

            private void showPopupWindow(View v, Participant data, ParticipantMeta meta) {
                PopupWindow popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
                MenuUserSettingBinding view = MenuUserSettingBinding.inflate(getLayoutInflater());
                view.setTop.setText(getText(meta.setTop ?
                    io.openim.android.ouicore.R.string.cancel_top :
                    io.openim.android.ouicore.R.string.set_top));
                view.allSee.setText(getString(vm.isAllSeeHe(data) ?
                    io.openim.android.ouicore.R.string.cancle_see_he :
                    io.openim.android.ouicore.R.string.all_see_the));

                view.setTop.setOnClickListener(v1 -> {
                    Map<String, Object> map = new HashMap<>();
                    List<String> ids = new ArrayList<>();
                    ids.add(getIdentity(data));
                    map.put("roomID", vm.signalingCertificate.getRoomID());
                    if (meta.setTop) map.put("reducePinedUserIDList", ids);
                    else map.put("addPinedUserIDList", ids);

                    vm.updateMeetingInfo(map, data1 -> {
                        ParticipantMeta participantMeta = GsonHel.fromJson(data.getMetadata(),
                            ParticipantMeta.class);
                        meta.setTop = participantMeta.setTop = !meta.setTop;
                        data.setMetadata(GsonHel.toJson(participantMeta));
                        memberAdapter.getItems().remove(data);
                        if (meta.setTop) {
                            memberAdapter.getItems().add(0, data);
                        } else {
                            memberAdapter.getItems().add(data);
                        }
                        memberAdapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    });
                });
                view.allSee.setOnClickListener(v1 -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("roomID", vm.signalingCertificate.getRoomID());
                    List<String> ids = new ArrayList<>();
                    if (!vm.isAllSeeHe(data)) {
                        ids.add(getIdentity(data));
                    }
                    map.put("beWatchedUserIDList", ids);

                    vm.updateMeetingInfo(map, data1 -> {
                        vm.roomMetadata.val().beWatchedUserIDList = ids;
                        vm.roomMetadata.update();

                        memberAdapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    });
                });

                //设置PopupWindow的视图内容
                popupWindow.setContentView(view.getRoot());
                //点击空白区域PopupWindow消失，这里必须先设置setBackgroundDrawable，否则点击无反应
                popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                popupWindow.setOutsideTouchable(true);
                //PopupWindow在targetView下方弹出
                popupWindow.showAsDropDown(v, 0, Common.dp2px(-20));
            }
        });

        v.invite.setVisibility(vm.memberPermission.getValue() ? View.VISIBLE : View.GONE);
        v.invite.setOnClickListener(v1 -> {
            ARouter.getInstance().build(Routes.Contact.FORWARD).navigation(this,
                Constants.Event.FORWARD);
        });
        v.allMute.setVisibility(vm.isSelfHostUser.getValue() ? View.VISIBLE : View.GONE);
        v.unAllMute.setVisibility(vm.isSelfHostUser.getValue() ? View.VISIBLE : View.GONE);
//        v.allMute.setText(isMuteAllMicrophone ?
//            io.openim.android.ouicore.R.string.cancel_all_mute :
//            io.openim.android.ouicore.R.string.all_mute);

        v.allMute.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v1) {
                allMute(true);
            }
        });
        v.unAllMute.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v1) {
                allMute(false);
            }
        });
        return v.getRoot();
    }

    private void allMute(boolean isMute) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomID", vm.signalingCertificate.getRoomID());
        map.put("isMuteAllMicrophone", isMute);

        vm.updateMeetingInfo(map, data -> {
            vm.roomMetadata.val().isMuteAllMicrophone = isMute;
            bottomPopDialog.dismiss();
        });
    }

    private void requestMediaProjection() {
        MediaProjectionManager mediaProjectionManager =
            (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        screenCaptureIntentLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
    }

    private void updateGuideView(int startPos, int pageNum) {
        guideViews.clear();
        MeetingHomeActivity.this.view.guideGroup.removeAllViews();
        for (int i = 0; i < pageNum; i++) {
            View view = new View(this);
            view.setBackgroundResource(io.openim.android.ouicore.R.drawable.selector_guide_bg);
            view.setSelected(startPos == i);
            LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(Common.dp2px(6), Common.dp2px(6));
            layoutParams.setMargins(10, 0, 0, 0);
            MeetingHomeActivity.this.view.guideGroup.addView(view, layoutParams);
            guideViews.add(view);
        }
    }

    private void initView() {
        view.pager.setAdapter(adapter = new PageAdapter(getLayoutInflater(), vm, gestureDetector));

        view.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < guideViews.size(); i++) {
                    guideViews.get(i).setSelected(i == position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    void init() {
        vm.init();
        vm.connectRoom();
    }

    private void bindVM() {
        view.setMeetingVM(vm);
        view.setCallViewModel(vm.callViewModel);
    }


    @Override
    protected void setLightStatus() {

    }


    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {
        view.landscape.setVisibility(View.VISIBLE);

        vm.callViewModel.subscribe(vm.callViewModel.getAllParticipants(), (v) -> {
            if (v.isEmpty()) return null;
            memberParticipants = vm.handleParticipants(v);

            vm.buildMetaData(memberParticipants);
            List<Object> data = new ArrayList<>();

            int pageNum = memberParticipants.size() / pageShow;
            if (pageNum == 0) {
                data.add(new ArrayList<>(memberParticipants));
            } else for (int i = 0; i < pageNum; i++) {
                List<Participant> participants =
                    new ArrayList<>(memberParticipants.subList(i * pageShow,
                        i * pageShow + pageShow));
                data.add(participants);
            }
            if (null == activeSpeaker) activeSpeaker = v.get(0);
            else {
                boolean isExist = false;
                for (Participant participant : v) {
                    isExist = Objects.equals(getIdentity(participant), getIdentity(activeSpeaker));
                }
                if (!isExist) activeSpeaker = v.get(0);
            }
            data.add(0, activeSpeaker);
            adapter.setList(data);

            updateGuideView(0, data.size());
            return null;
        });


        vm.callViewModel.subscribe(vm.callViewModel.getActiveSpeakersFlow(), (v) -> {
            if (v.isEmpty() || null != vm.allWatchedUser.val()) return null;
            showPageFirst(v.get(0), false);
            updateGuideView(0, adapter.getCount());
            return null;
        });

        vm.callViewModel.subscribe(vm.callViewModel.getRoom().getEvents().getEvents(), (v) -> {
            if (v instanceof RoomEvent.Disconnected) {
                RoomEvent.Disconnected disconnected = (RoomEvent.Disconnected) v;
                if (disconnected.getReason() == DisconnectReason.ROOM_DELETED) {
                    if (!vm.isSelfHostUser.val()) {
                        showHostExitDialog();
                    }
                }
            }
            return null;
        });
    }

    private final GestureDetector gestureDetector = new GestureDetector(BaseApp.inst(),
        new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Object obj = view.pager.getTag();
            boolean fixScreen = false;
            if (obj instanceof Boolean) fixScreen = (Boolean) obj;

            view.bottomMenu.setVisibility(fixScreen ? View.VISIBLE : View.GONE);
            view.topTitle.setVisibility(fixScreen ? View.VISIBLE : View.GONE);

            view.pager.setTag(!fixScreen);
            return super.onDoubleTap(e);
        }
    });
    private void listener() {
        view.pager.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        BaseApp.inst().isAppBackground.observeForever(isAppBackgroundListener = v -> {
            if (isRecover && !v) {
                moveTaskToFront(getTaskId());
                isRecover = false;
                return;
            }
            if (null != easyWindow) {
                if (v) easyWindow.cancel();
                else easyWindow.show();
            }
        });

        vm.allWatchedUser.observe(this, v -> {
            if (null == v) return;
            showPageFirst(v, true);
            updateGuideView(0, adapter.getCount());
        });
        view.landscape.setOnClickListener(v -> {
            triggerLandscape = true;
            vm.isInit = true;
            setRequestedOrientation(vm.isLandscape ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            vm.isLandscape = !vm.isLandscape;
        });

        View.OnClickListener clickListener = v -> {
            if (null == meetingInfoPopDialog)
                meetingInfoPopDialog = new BottomPopDialog(this, buildMeetingInfoPopDialogView());
            meetingInfoPopDialog.show();
        };
        view.topCenter.setOnClickListener(clickListener);
        view.down.setOnClickListener(clickListener);
        view.end.setOnClickListener(v -> {
            if (!vm.isSelfHostUser.val()) {
                exit(false);
                return;
            }
            if (null == exitPopDialog) exitPopDialog = new BottomPopDialog(this);
            exitPopDialog.show();
            exitPopDialog.getMainView().menu3.setOnClickListener(v1 -> exitPopDialog.dismiss());
            exitPopDialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.exit_meeting);
            exitPopDialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.finish_meeting);
            exitPopDialog.getMainView().menu2.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            exitPopDialog.getMainView().menu1.setOnClickListener(v1 -> {
                exit(false);
                exitPopDialog.dismiss();
            });
            exitPopDialog.getMainView().menu2.setOnClickListener(v1 -> {
                exit(true);
                exitPopDialog.dismiss();
            });
        });

        view.mic.setOnClickListener(v -> vm.callViewModel.setMicEnabled(view.mic.isChecked()));
        view.camera.setOnClickListener(v -> vm.callViewModel.setCameraEnabled(view.camera.isChecked()));
        view.shareScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Boolean.FALSE.equals(vm.callViewModel.getScreenshareEnabled().getValue())) {
                    requestMediaProjection();
                }
            } else {
                vm.stopShareScreen();
            }
        });
        view.member.setOnClickListener(v -> {
            if (null == vm.roomMetadata.val()) return;
            bottomPopDialog = new BottomPopDialog(this, buildPopView());

            List<Participant> participants = new ArrayList<>(memberParticipants);
            Collections.sort(participants, (o1, o2) -> {
                ParticipantMeta participantMeta = GsonHel.fromJson(o1.getMetadata(),
                    ParticipantMeta.class);
                if (participantMeta.setTop) return -1;
                return 0;
            });
            memberAdapter.setItems(participants);
            bottomPopDialog.show();
        });
        view.setting.setOnClickListener(v -> {
            if (null == settingPopDialog)
                settingPopDialog = new BottomPopDialog(this, buildSettingPopView());
            settingPopDialog.show();
        });

        vm.callViewModel.subscribe(vm.callViewModel.getRoom().getLocalParticipant().getEvents().getEvents(), (v) -> {
            boolean isMicrophoneEnabled = v.getParticipant().isMicrophoneEnabled();
            boolean isCameraEnabled = v.getParticipant().isCameraEnabled();
            view.mic.setChecked(isMicrophoneEnabled);
            view.mic.setText(isMicrophoneEnabled ?
                getString(io.openim.android.ouicore.R.string.mute) :
                getString(io.openim.android.ouicore.R.string.cancel_mute));
            view.camera.setChecked(isCameraEnabled);
            view.camera.setText(isCameraEnabled ?
                getString(io.openim.android.ouicore.R.string.close_camera) :
                getString(io.openim.android.ouicore.R.string.start_camera));
            return null;
        }, vm.callViewModel.buildScope());

        view.horn.setOnClickListener(v -> {
            boolean isHorn = getViewBooleanTag(v);
            v.setTag(!isHorn);
            vm.isReceiver.setValue(!isHorn);
        });
        vm.isReceiver.observe(this, aBoolean -> {
            vm.setSpeakerphoneOn(aBoolean);
            view.horn.setImageResource(aBoolean ? R.mipmap.ic_m_horn : R.mipmap.ic_m_receiver);
        });
        view.zoomOut.setOnClickListener(v -> {
            hasSystemAlert.safeGo(() -> {
                Postcard postcard = ARouter.getInstance().build(Routes.Main.HOME);
                LogisticsCenter.completion(postcard);
                Activity activity = ActivityManager.isExist(postcard.getDestination());
                if (null != activity) {
                    moveTaskToFront(activity.getTaskId());
                    showFloatView();
                }
            });
        });

        vm.roomMetadata.observe(this, meta -> {
            try {
                if (null == meta || null == memberAdapter || memberAdapter.getItems().isEmpty())
                    return;
                vm.buildMetaData(memberParticipants);
                vm.allSeeHe();
            } catch (Exception ignored) {
            }
        });
    }

    private void showHostExitDialog() {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.host_exit_tips);
        commonDialog.getMainView().cancel.setOnClickListener(view1 -> commonDialog.dismiss());
        commonDialog.getMainView().confirm.setOnClickListener(view1 -> {
            commonDialog.dismiss();
            finish();
        });
        commonDialog.show();
    }

    /**
     * 在说话用户/都看他
     *
     * @param first
     */
    private void showPageFirst(Participant first, boolean isJump) {
        if (null != activeSpeaker && Objects.equals(getIdentity(first), getIdentity(activeSpeaker)))
            return;
        adapter.getList().remove(activeSpeaker);
        activeSpeaker = first;
        adapter.getList().add(0, activeSpeaker);
        adapter.notifyDataSetChanged();
        if (isJump) view.pager.setCurrentItem(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == Constants.Event.FORWARD && null != data) {
            //在这里转发
            String id = data.getStringExtra(Constants.K_ID);
            String otherSideNickName = data.getStringExtra(Constants.K_NAME);
            String groupId = data.getStringExtra(Constants.K_GROUP_ID);
            vm.inviterUser(id, groupId);
        }
    }


    public static class MemberItemViewHolder extends RecyclerView.ViewHolder {
        public final MeetingIietmMemberBinding view;

        public MemberItemViewHolder(@NonNull View itemView) {
            super(MeetingIietmMemberBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = MeetingIietmMemberBinding.bind(this.itemView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHomeKey(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterHomeKey();
        isRecover=true;
        release();
    }

    private void unregisterHomeKey() {
        if (null != mHomeKeyReceiver) unregisterReceiver(mHomeKeyReceiver);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApp.inst().isAppBackground.removeObserver(isAppBackgroundListener);
        release();
    }

    private void release() {
        if (isFinishing() && !triggerLandscape) {
            vm.setSpeakerphoneOn(true);
            vm.onCleared();
            removeCacheVM();
            if (null != easyWindow) {
                easyWindow.cancel();
                easyWindow.recycle();
                easyWindow = null;
            }
        }
    }

    @Override
    public void finish() {
        Postcard postcard = ARouter.getInstance().build(Routes.Main.HOME);
        LogisticsCenter.completion(postcard);
        Activity activity = ActivityManager.isExist(postcard.getDestination());
        if (null != activity) {
            moveTaskToFront(activity.getTaskId());
        }
        super.finish();
    }

    private static class PageAdapter extends PagerAdapter {
        private final LayoutInflater inflater;
        private List<Object> list = new ArrayList<>();
        private final MeetingVM vm;
        private final GestureDetector gestureDetector;

        public PageAdapter(LayoutInflater inflater, MeetingVM vm, GestureDetector gestureDetector) {
            this.inflater = inflater;
            this.vm = vm;
            this.gestureDetector = gestureDetector;
        }

        public List<Object> getList() {
            return list;
        }

        public void setList(List list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public int getItemPosition(Object object) {
            //  notifyDataSetChanged() 页面不刷新问题的方法
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Object ob = list.get(position);
            View view_ = null;
            if (ob instanceof Participant) {
                Participant participant = (Participant) ob;

                SingleTextureView singleTextureView = new SingleTextureView(container.getContext());
                singleTextureView.bindData(vm, participant);
                view_ = singleTextureView;
            }
            if (ob instanceof List) {
                List<Participant> participants = (List<Participant>) ob;
                ViewRecyclerViewBinding view = ViewRecyclerViewBinding.inflate(inflater);
                view.recyclerView.setLayoutManager(new GridLayoutManager(container.getContext(),
                    2));
                RecyclerViewAdapter<Participant, UserStreamViewHolder> adapter;
                GridSpaceItemDecoration divItemDecoration =
                    new GridSpaceItemDecoration(Common.dp2px(1));
                view.recyclerView.addItemDecoration(divItemDecoration);
                view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<Participant,
                    UserStreamViewHolder>(UserStreamViewHolder.class) {

                    @Override
                    public void onBindView(@NonNull UserStreamViewHolder holder, Participant data
                        , int position) {
                        holder.setItemSize(-1, view.recyclerView.getHeight() / 2);
                        holder.view.bindData(vm, data);
                    }
                });
                view.recyclerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

                adapter.setItems(participants);
                view_ = view.getRoot();
            }
            if (null != view_) {
                container.addView(view_);
                return view_;
            }
            return container;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        public static class UserStreamViewHolder extends RecyclerView.ViewHolder {
            public final SingleTextureView view;

            public UserStreamViewHolder(@NonNull View itemView) {
                super(new SingleTextureView(itemView.getContext()));
                view = (SingleTextureView) this.itemView;
            }

            /**
             * MATCH_PARENT -1 WRAP_CONTENT -2
             *
             * @param wh
             */
            public void setItemSize(int... wh) {
                View childAt = view.getChildAt(0);
                if (null != childAt) {
                    ViewGroup.LayoutParams params = childAt.getLayoutParams();
                    params.width = wh[0];
                    params.height = wh[1];
                }
            }
        }
    }

}
