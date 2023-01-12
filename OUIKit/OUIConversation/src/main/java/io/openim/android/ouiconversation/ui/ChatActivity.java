package io.openim.android.ouiconversation.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.databinding.ActivityChatBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.widget.BottomInputCote;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.widget.CustomItemAnimator;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SignalingInfo;

@Route(path = Routes.Conversation.CHAT)
public class ChatActivity extends BaseActivity<ChatVM, ActivityChatBinding> implements ChatVM.ViewAction, Observer {


    private MessageAdapter messageAdapter;
    private BottomInputCote bottomInputCote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initVM();
        super.onCreate(savedInstanceState);
        vm.init();

        bindViewDataBinding(ActivityChatBinding.inflate(getLayoutInflater()));
        sink();
        view.setChatVM(vm);

        initView();
        listener();
        setTouchClearFocus(false);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    private void initVM() {
        String userId = getIntent().getStringExtra(Constant.K_ID);
        String groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        boolean fromChatHistory = getIntent().getBooleanExtra(Constant.K_FROM, false);
        NotificationMsg notificationMsg = (NotificationMsg) getIntent().getSerializableExtra(Constant.K_NOTICE);

        bindVM(ChatVM.class, !fromChatHistory);
        if (null != userId) vm.otherSideID = userId;
        if (null != groupId) {
            vm.isSingleChat = false;
            vm.groupID = groupId;
        }
        vm.fromChatHistory = fromChatHistory;
        if (null != notificationMsg) vm.notificationMsg.setValue(notificationMsg);

        if (fromChatHistory) {
            ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
            vm.startMsg = chatVM.startMsg;
            vm.otherSideID = chatVM.otherSideID;
            vm.isSingleChat = chatVM.isSingleChat;
            vm.groupID = chatVM.groupID;
            vm.notificationMsg.setValue(chatVM.notificationMsg.getValue());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        release();
    }

    private void release() {
        if (isFinishing()) {
            if (!vm.fromChatHistory) removeCacheVM();

            N.clearDispose(this);
            view.waterMark.onDestroy();
            Obs.inst().deleteObserver(this);
            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    @Override
    protected void onResume() {
        if (vm.viewPause) {
            //从Pause 到 Resume  把当前显示的msg 标记为已读
            LinearLayoutMg linearLayoutManager = (LinearLayoutMg) view.recyclerView.getLayoutManager();
            if (null == linearLayoutManager) return;
            int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            vm.sendMsgReadReceipt(firstVisiblePosition, lastVisiblePosition);
        }
        super.onResume();
    }

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    private void initView() {
        bottomInputCote = new BottomInputCote(this, view.layoutInputCote);
        bottomInputCote.setChatVM(vm);
        if (vm.fromChatHistory) {
            view.layoutInputCote.getRoot().setVisibility(View.GONE);
            view.call.setVisibility(View.GONE);
            view.more.setVisibility(View.GONE);
        }

        LinearLayoutMg linearLayoutManager = new LinearLayoutMg(this);
        //倒叙
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        view.recyclerView.setItemAnimator(new CustomItemAnimator());
        view.recyclerView.setLayoutManager(linearLayoutManager);
        view.recyclerView.addItemDecoration(new DefaultItemDecoration(this.getResources().getColor(android.R.color.transparent), 1, Common.dp2px(16)));
        messageAdapter = new MessageAdapter();
        messageAdapter.bindRecyclerView(view.recyclerView);

        vm.setMessageAdapter(messageAdapter);
        view.recyclerView.setAdapter(messageAdapter);
        vm.messages.observe(this, v -> {
            if (null == v) return;
            messageAdapter.setMessages(v);
            messageAdapter.notifyDataSetChanged();
            if (!vm.fromChatHistory) scrollToPosition(0);
        });
        view.recyclerView.setOnTouchListener((v, event) -> {
            bottomInputCote.clearFocus();
            Common.hideKeyboard(this, v);
            bottomInputCote.setExpandHide();
            return false;
        });

        view.recyclerView.addOnLayoutChangeListener((v, i, i1, i2, i3, i4, i5, i6, i7) -> {
            if (i3 < i7) { // bottom < oldBottom
                scrollToPosition(0);
            }
        });
        String chatBg = SharedPreferencesUtil.get(this).getString(Constant.K_SET_BACKGROUND + (vm.isSingleChat ? vm.otherSideID : vm.groupID));
        if (!chatBg.isEmpty()) Glide.with(this).load(chatBg).into(view.chatBg);


        if (vm.isSingleChat) {
            vm.getUserOnlineStatus(onlineStatus -> {
                boolean isOnline = onlineStatus.status.equals("online");
                view.leftBg.setVisibility(View.VISIBLE);
                if (isOnline) {
                    view.leftBg.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_max_10cc64);
                    view.onlineStatus.setText(String.format(getString(io.openim.android.ouicore.R.string.online), vm.handlePlatformCode(onlineStatus.detailPlatformStatus)));
                } else {
                    view.leftBg.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_max_ff999999);
                    view.onlineStatus.setText(io.openim.android.ouicore.R.string.offline);
                }
            });
        }
        view.waterMark.setText(BaseApp.inst().loginCertificate.nickname);
    }

    //记录原始窗口高度
    private int mWindowHeight = 0;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = () -> {
        Rect r = new Rect();
        //获取当前窗口实际的可见区域
        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        int height = r.height();
        if (mWindowHeight == 0) {
            //一般情况下，这是原始的窗口高度
            mWindowHeight = height;
        } else {
            RelativeLayout.LayoutParams inputLayoutParams = (RelativeLayout.LayoutParams) view.layoutInputCote.getRoot().getLayoutParams();
            if (mWindowHeight == height) {
                inputLayoutParams.bottomMargin = 0;
            } else {
                //两次窗口高度相减，就是软键盘高度
                int softKeyboardHeight = mWindowHeight - height;
                inputLayoutParams.bottomMargin = softKeyboardHeight;
            }
            view.layoutInputCote.getRoot().setLayoutParams(inputLayoutParams);
        }
    };

    private void listener() {
        Obs.inst().addObserver(this);
        view.call.setOnClickListener(v -> {
            if (null == callingService) return;
            IMUtil.showBottomPopMenu(this, (v1, keyCode, event) -> {
                vm.isVideoCall = keyCode != 1;
                if (vm.isSingleChat) {
                    List<String> ids = new ArrayList<>();
                    ids.add(vm.otherSideID);
                    SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(vm.isVideoCall, vm.isSingleChat, ids, null);
                    callingService.call(signalingInfo);
                } else {
                    toSelectMember();
                }
                return false;
            });
        });

        view.delete.setOnClickListener(v -> {
            List<Message> selectMsg = getSelectMsg();
            for (Message message : selectMsg) {
                vm.deleteMessageFromLocalStorage(message);
            }
        });
        view.mergeForward.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Contact.FORWARD).navigation(this, Constant.Event.FORWARD);
            Common.UIHandler.postDelayed(() -> vm.enableMultipleSelect.setValue(false), 300);
        });
        vm.enableMultipleSelect.observe(this, o -> {
            if (null == o) return;
            int px = Common.dp2px(22);
            if (o) {
                view.choiceMenu.setVisibility(View.VISIBLE);
                view.layoutInputCote.getRoot().setVisibility(View.INVISIBLE);
                view.cancel.setVisibility(View.VISIBLE);
                view.back.setVisibility(View.GONE);
                view.recyclerView.setPadding(0, 0, px, 0);
            } else {
                view.choiceMenu.setVisibility(View.GONE);
                view.layoutInputCote.getRoot().setVisibility(View.VISIBLE);
                view.cancel.setVisibility(View.GONE);
                view.back.setVisibility(View.VISIBLE);
                view.recyclerView.setPadding(px, 0, px, 0);
                messageAdapter.notifyDataSetChanged();
            }
        });
        view.cancel.setOnClickListener(v -> {
            vm.enableMultipleSelect.setValue(false);
            for (Message message : vm.messages.getValue()) {
                MsgExpand msgExpand = (MsgExpand) message.getExt();
                if (null != msgExpand) msgExpand.isChoice = false;
            }
        });


        view.notice.setOnClickListener(v -> ARouter.getInstance().build(Routes.Group.NOTICE_DETAIL).withSerializable(Constant.K_NOTICE, vm.notificationMsg.getValue()).navigation());

        view.back.setOnClickListener(v -> finish());

        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutMg linearLayoutManager = (LinearLayoutMg) view.recyclerView.getLayoutManager();
                int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == vm.messages.getValue().size() - 1 && vm.messages.getValue().size() >= vm.count) {
                    vm.loadHistoryMessage();
                }
//                if (vm.fromChatHistory && firstVisiblePosition < 2) {
//                    vm.loadHistoryMessageReverse();
//                }
                vm.sendMsgReadReceipt(firstVisiblePosition, lastVisiblePosition);
            }
        });

        view.more.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                if (vm.isSingleChat) {
                    startActivity(new Intent(ChatActivity.this, ChatSettingActivity.class));
                } else {
                    ARouter.getInstance().build(Routes.Group.MATERIAL).withString(Constant.K_GROUP_ID, vm.groupID).navigation();
                }
            }
        });

        vm.groupInfo.observe(this, groupInfo -> {
            bindShowName();
        });
        vm.conversationInfo.observe(this, conversationInfo -> {
            bindShowName();
        });
    }

    public void toSelectMember() {
            GroupVM groupVM = new GroupVM();
            groupVM.groupId=vm.groupID;
            BaseApp.inst().putVM(groupVM);
            ARouter.getInstance().build(Routes.Group.SUPER_GROUP_MEMBER)
                .withBoolean(Constant.IS_SELECT_MEMBER, true)
                .withInt(Constant.K_SIZE, 9)
                .navigation(this, Constant.Event.CALLING_REQUEST_CODE);
    }

    private void bindShowName() {
        try {
            if (vm.isSingleChat)
                view.nickName.setText(vm.conversationInfo.getValue().getShowName());
            else
                view.nickName.setText(vm.conversationInfo.getValue().getShowName() + "(" + vm.groupInfo.getValue().getMemberCount() + ")");
        } catch (Exception ignored) {
        }
    }


    @NonNull
    private List<Message> getSelectMsg() {
        List<Message> selectMsg = new ArrayList<>();
        for (Message message : messageAdapter.getMessages()) {
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            if (null != msgExpand && msgExpand.isChoice) selectMsg.add(message);
        }
        return selectMsg;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        bottomInputCote.dispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void scrollToPosition(int position) {
        view.recyclerView.scrollToPosition(position);
    }

    @Override
    public void closePage() {
        finish();
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

            Message forwardMsg;
            if (null == vm.forwardMsg)//表示合并转发
                forwardMsg = IMUtil.createMergerMessage(vm.isSingleChat, otherSideNickName, getSelectMsg());
            else forwardMsg = vm.forwardMsg;
            vm.aloneSendMsg(forwardMsg, id, groupId);
            vm.clearSelectMsg();
        }
        if (requestCode == Constant.Event.CALLING_REQUEST_CODE && null != data) {
            //发起群通话
            List<String> ids = data.getStringArrayListExtra(Constant.K_RESULT);
            //邀请列表中移除自己
            ids.remove(BaseApp.inst().loginCertificate.userID);
            SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(vm.isVideoCall, false, ids, vm.groupID);
            if (null == callingService) return;
            callingService.call(signalingInfo);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        try {
            Obs.Message message = (Obs.Message) o;
            if (message.tag == Constant.Event.SET_BACKGROUND) {
                String path = "";
                if (null != message.object) {
                    path = (String) message.object;
                } else {
                    path = SharedPreferencesUtil.get(this).getString(Constant.K_SET_BACKGROUND + (vm.isSingleChat ? vm.otherSideID : vm.groupID));
                }
                if (path.isEmpty()) view.chatBg.setVisibility(View.GONE);
                else Glide.with(this).load(path).into(view.chatBg);
            }
            if (message.tag==Constant.Event.INSERT_MSG){
                vm.messages.getValue().clear();
                vm.startMsg=null;
                vm.loadHistoryMessage();
            }
//            if (message.tag == Constant.Event.UPDATA_GROUP_INFO
//                || message.tag == Constant.Event.USER_INFO_UPDATA) {
//                vm.getOneConversation(data -> {
//                    vm.conversationInfo.setValue(data);
//                    bindShowName();
//                });
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class LinearLayoutMg extends androidx.recyclerview.widget.LinearLayoutManager {
        public LinearLayoutMg(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }
}
