package io.openim.android.ouiconversation.ui;


import static io.openim.android.ouicore.utils.Constants.ActivityResult.DELETE_FRIEND;
import static io.openim.android.ouicore.utils.Constants.ActivityResult.SET_REMARK;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.databinding.ActivityChatBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.vm.CustomEmojiVM;
import io.openim.android.ouiconversation.widget.BottomInputCote;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.ex.AtUser;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.vm.ForwardVM;
import io.openim.android.ouicore.vm.GroupMemberVM;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.CustomItemAnimator;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.PublicUserInfo;
import io.openim.android.sdk.models.QuoteElem;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.TextElem;
import io.openim.android.sdk.models.UserInfo;
import io.openim.android.sdk.models.UsersOnlineStatus;
import io.reactivex.observers.DisposableObserver;

@Route(path = Routes.Conversation.CHAT)
public class ChatActivity extends BaseActivity<ChatVM, ActivityChatBinding> implements ChatVM.ViewAction, Observer {

    private CallingService callingService;
    private MessageAdapter messageAdapter;
    private BottomInputCote bottomInputCote;
    private boolean screenWasOff = false;
    private PowerManager powerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initVM();
        super.onCreate(savedInstanceState);
        vm.init();

        bindViewDataBinding(ActivityChatBinding.inflate(getLayoutInflater()));
        sink();
        view.setChatVM(vm);
        callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();

        initView();
        listener();
        setTouchClearFocus(false);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        try {
            vm.mTypingState.observe(this, isTyping -> {
                if (!TextUtils.isEmpty(vm.conversationID)) {
                    OpenIMClient.getInstance().conversationManager.changeInputStates(new OnBase<String>() {
                        @Override
                        public void onError(int code, String error) {
                            String data = code + error;
                        }

                        @Override
                        public void onSuccess(String data) {
                            TextUtils.isEmpty(data);
                        }
                    }, vm.conversationID, isTyping);
                }
            });
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        } catch (Exception e) {
        }
    }

    private final ActivityResultLauncher<Intent> personDetailLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            switch (result.getResultCode()) {
                case DELETE_FRIEND:
                    setResult(DELETE_FRIEND);
                    showWaiting();
                    Common.UIHandler.post(() -> {
                        cancelWaiting();
                        finish();
                    });
                    break;
                case SET_REMARK:
                    setResult(SET_REMARK);
                    showWaiting();
                    Common.UIHandler.post(this::cancelWaiting);
                    break;
            }
        });

    private void initVM() {
        Easy.installVM(CustomEmojiVM.class);
        Easy.installVM(ForwardVM.class);

        String userId = getIntent().getStringExtra(Constants.K_ID);
        String groupId = getIntent().getStringExtra(Constants.K_GROUP_ID);
        boolean fromChatHistory = getIntent().getBooleanExtra(Constants.K_FROM, false);
        NotificationMsg notificationMsg =
            (NotificationMsg) getIntent().getSerializableExtra(Constants.K_NOTICE);

        bindVM(ChatVM.class, !fromChatHistory);
        if (null != userId) vm.userID = userId;
        if (null != groupId) {
            vm.isSingleChat = false;
            vm.groupID = groupId;
        }
        vm.fromChatHistory = fromChatHistory;
        if (null != notificationMsg) vm.notificationMsg.setValue(notificationMsg);

        if (fromChatHistory) {
            ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
            vm.startMsg = chatVM.startMsg;
            vm.userID = chatVM.userID;
            vm.isSingleChat = chatVM.isSingleChat;
            vm.groupID = chatVM.groupID;
            vm.notificationMsg.setValue(chatVM.notificationMsg.getValue());
        }
    }

    @Override
    protected void fasterDestroy() {
        if (vm.conversationInfo.val() != null && vm.conversationInfo.val().getUnreadCount() > 0)
            vm.markRead();

        if (!vm.fromChatHistory) removeCacheVM();
        Easy.delete(CustomEmojiVM.class);
        Easy.delete(ForwardVM.class);

        BaseApp.inst().removeCacheVM(GroupVM.class);
        N.clearDispose(this);
        view.waterMark.onDestroy();
        Obs.inst().deleteObserver(this);
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        try {
            SPlayer.instance().stop();
        } catch (Exception ignore) {
        }

        ContactListVM contactListVM = BaseApp.inst().getVMByCache(ContactListVM.class);
        if (contactListVM != null) {
            contactListVM.updateConversation();
        }
    }

    @Override
    protected void onResume() {
        if (vm.viewPause) {
            //从Pause 到 Resume  把当前显示的msg 标记为已读
            LinearLayoutMg linearLayoutManager =
                (LinearLayoutMg) view.recyclerView.getLayoutManager();
            if (null == linearLayoutManager) return;
            int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            vm.sendMsgReadReceipt(firstVisiblePosition, lastVisiblePosition);
            messageAdapter.notifyDataSetChanged();
        }
        super.onResume();
        if (screenWasOff) {
            vm.getConversationInfo();
            screenWasOff = false;
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    private void initView() {
        bottomInputCote = new BottomInputCote(this, view.layoutInputCote);
        bottomInputCote.setChatVM(vm);
        if (vm.fromChatHistory) {
            view.layoutInputCote.getRoot().setVisibility(View.GONE);
            view.more.setVisibility(View.GONE);
        }

        LinearLayoutMg linearLayoutManager = new LinearLayoutMg(this);
        //倒叙
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        view.recyclerView.setItemAnimator(new CustomItemAnimator());
        view.recyclerView.setLayoutManager(linearLayoutManager);
        view.recyclerView.addItemDecoration(new DefaultItemDecoration(this.getResources().getColor(android.R.color.transparent), 1, Common.dp2px(16)));
        view.recyclerView.setItemAnimator(null);
        messageAdapter = new MessageAdapter();
        messageAdapter.bindRecyclerView(view.recyclerView);

        vm.setMessageAdapter(messageAdapter);
        view.recyclerView.setAdapter(messageAdapter);
        vm.messages.observe(this, v -> {
            if (null == v) return;
            // save origin avatar
            if (vm.isSingleChat && !v.isEmpty() && !TextUtils.isEmpty(v.get(0).getSenderFaceUrl()))
                vm.userOriginAvatar = v.get(0).getSenderFaceUrl();
            if (!v.isEmpty()) vm.startMsg = v.get(0);
            messageAdapter.setMessages(v);
            messageAdapter.notifyDataSetChanged();
            if (!vm.fromChatHistory) scrollToPosition(0);
        });
        view.recyclerView.setOnTouchListener((v, event) -> {
            bottomInputCote.clearFocus();
            vm.mTypingState.postValue(false);
            Common.hideKeyboard(this, v);
            bottomInputCote.setExpandHide(true);
            return false;
        });
        view.recyclerView.addOnLayoutChangeListener((v, i, i1, i2, i3, i4, i5, i6, i7) -> {
            if (i3 < i7) { // bottom < oldBottom
                scrollToPosition(0);
            }
        });


        String chatBg =
            SharedPreferencesUtil.get(this).getString(Constants.K_SET_BACKGROUND + (vm.isSingleChat ? vm.userID : vm.groupID));
        if (!chatBg.isEmpty()) Glide.with(this).load(chatBg).into(view.chatBg);


        if (vm.isSingleChat) {
            vm.getUserOnlineStatus(this::showOnlineStatus);
        }
        try {
            view.waterMark.setText(BaseApp.inst().loginCertificate.nickname);
        } catch (Exception ignored) {
        }
    }

    private void showOnlineStatus(UsersOnlineStatus onlineStatus) {
        boolean isOnline = onlineStatus.status == 1;
        view.leftBg.setVisibility(View.VISIBLE);
        if (isOnline) {
            view.leftBg.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_max_10cc64);
            view.onlineStatus.setText(String.format(getString(io.openim.android.ouicore.R.string.online), vm.handlePlatformCode(onlineStatus.platformIDs)));
        } else {
            view.leftBg.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_max_ff999999);
            view.onlineStatus.setText(io.openim.android.ouicore.R.string.offline);
        }
    }

    //记录原始窗口高度
    private int mWindowHeight = 0;

    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = () -> {
        Rect r = new Rect();
        //获取当前窗口实际的可见区域
        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        int height = r.height();
        if (mWindowHeight == 0) {
            //一般情况下，这是原始的窗口高度
            mWindowHeight = height;
        } else {
            RelativeLayout.LayoutParams inputLayoutParams =
                (RelativeLayout.LayoutParams) view.layoutInputCote.getRoot().getLayoutParams();
            if (mWindowHeight == height) {
                if (!(view.layoutInputCote.fragmentContainer.getVisibility() == View.GONE) && inputLayoutParams.bottomMargin != 0) {
                    bottomInputCote.setExpandHide(true);
                    bottomInputCote.clearFocus();
                }
                inputLayoutParams.bottomMargin = 0;
            } else {
                //两次窗口高度相减，就是软键盘高度
                inputLayoutParams.bottomMargin =
                    mWindowHeight - height - bottomInputCote.view.fragmentContainer.getHeight();
            }
            view.layoutInputCote.getRoot().setLayoutParams(inputLayoutParams);
        }
    };

    private void listener() {
        Obs.inst().addObserver(this);
        view.delete.setOnClickListener(v -> {
            List<Message> selectMsg = getSelectMsg();
            for (Message message : selectMsg) {
                vm.deleteMessageFromLocalStorage(message);
            }
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

        view.back.setOnClickListener(v -> finish());

        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutMg linearLayoutManager =
                    (LinearLayoutMg) view.recyclerView.getLayoutManager();
                int firstVisiblePosition =
                    linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisiblePosition =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == vm.messages.getValue().size() - 1 && !vm.isNoData.val()) {
                    vm.loadHistoryMessage();
                }
                vm.sendMsgReadReceipt(firstVisiblePosition, lastVisiblePosition);
            }
        });

        view.more.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                if (vm.isSingleChat) {
                    Postcard postcard = ARouter.getInstance().build(Routes.Main.PERSON_DETAIL);
                    LogisticsCenter.completion(postcard);
                    personDetailLauncher.launch(new Intent(ChatActivity.this, postcard.getDestination()).putExtra(Constants.K_ID, vm.userID).putExtra(Constants.K_RESULT, true));
                } else {
                    ARouter.getInstance().build(Routes.Group.MATERIAL).withString(Constants.K_ID,
                        vm.conversationID).withString(Constants.K_GROUP_ID, vm.groupID).navigation();
                }
            }
        });

        view.call.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                if (vm.isSingleChat && callingService != null) {
                    goToCall();
                }
            }
        });

        vm.groupInfo.observe(this, groupInfo -> {
            bindShowName();
        });
        vm.conversationInfo.observe(this, conversationInfo -> {
            bindShowName();
            vm.groupNotificationOperator(conversationInfo);
            vm.getGroupsInfo(vm.groupID, data -> {
                if (data.isEmpty()) return;
                vm.groupInfo.setValue(data.get(0));
            });
            if (conversationInfo.getUnreadCount() > 0) {
                vm.markRead();
            }
        });
        vm.userAvatar.observe(this, newAvatar -> {
            if (TextUtils.isEmpty(newAvatar)) return;
            List<Message> newMsg = new ArrayList<>();
            for (Message msg : vm.messages.val()) {
                msg.setSenderFaceUrl(newAvatar);
                newMsg.add(msg);
            }
            vm.messages.setValue(newMsg);
        });

        vm.subscribe(this, subject -> {
            if (subject.equals(ChatVM.REEDIT_MSG)) {
                Message reeditMsg = (Message) subject.value;
                if (reeditMsg == null) return;
                TextElem reeditText = reeditMsg.getTextElem();
                QuoteElem reeditQuote = reeditMsg.getQuoteElem();
                Common.pushKeyboard(this);
                if (reeditText != null) {
                    view.layoutInputCote.chatInput.requestFocus();
                    view.layoutInputCote.chatInput.setText(reeditText.getContent());
                    view.layoutInputCote.chatInput.setSelection(reeditText.getContent().length());
                    return;
                }
                if (reeditQuote != null) {
                    view.layoutInputCote.chatInput.requestFocus();
                    view.layoutInputCote.chatInput.setText(reeditQuote.getText());
                    view.layoutInputCote.chatInput.setSelection(reeditQuote.getText().length());
                    vm.replyMessage.setValue(reeditQuote.getQuoteMessage());
                }
            }
        });
    }

    public void goToCall() {
        IMUtil.showBottomCallsPopMenu(this, (v1, keyCode, event) -> {
            vm.isVideoCall = keyCode != 1;
            if (vm.isSingleChat) {
                List<String> ids = new ArrayList<>();
                ids.add(vm.userID);
                SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(vm.isVideoCall, ids);
                callingService.call(signalingInfo);
            }
            return false;
        });
    }

    private void bindShowName() {
        try {
            view.nickName.setText(vm.conversationInfo.getValue().getShowName());
            if (!vm.isSingleChat)
                view.groupMenberNum.setText("(" + vm.groupInfo.getValue().getMemberCount() + ")");
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
        if (resultCode != RESULT_OK || null == data) {
        }
    }

    private void forward(List<MultipleChoice> choices) {
        ForwardVM forwardVM = Easy.find(ForwardVM.class);
        for (MultipleChoice choice : choices) {
            aloneSendMsg(forwardVM.forwardMsg, choice);
        }
        vm.clearSelectMsg();
    }

    private void aloneSendMsg(Message msg, MultipleChoice choice) {
        if (choice.isGroup) vm.aloneSendMsg(msg, null, choice.key);
        else vm.aloneSendMsg(msg, choice.key, null);
    }

    @Override
    public void update(Observable observable, Object o) {
        try {
            Obs.Message message = (Obs.Message) o;
            if (message.tag == Constants.Event.SET_BACKGROUND) {
                String path = "";
                if (null != message.object) {
                    path = (String) message.object;
                } else {
                    path =
                        SharedPreferencesUtil.get(this).getString(Constants.K_SET_BACKGROUND + (vm.isSingleChat ? vm.userID : vm.groupID));
                }
                if (path.isEmpty()) view.chatBg.setVisibility(View.GONE);
                else Glide.with(this).load(path).into(view.chatBg);
            }
            if (message.tag == Constants.Event.INSERT_MSG) {
                vm.messages.getValue().clear();
                vm.startMsg = null;
                vm.loadHistoryMessage();
            }
            if (message.tag == Constants.Event.FORWARD) {
                List<MultipleChoice> choices = (List<MultipleChoice>) message.object;
                if (null == choices || choices.isEmpty()) return;
                forward(choices);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class LinearLayoutMg extends androidx.recyclerview.widget.LinearLayoutManager {
        private boolean canScrollVertically = true;

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


        @Override
        public boolean canScrollVertically() {
            return canScrollVertically;
        }

        public void setCanScrollVertically(boolean canScrollVertically) {
            this.canScrollVertically = canScrollVertically;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vm.mTypingState.postValue(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenWasOff = !powerManager.isScreenOn();
        vm.mTypingState.postValue(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        vm.mTypingState.postValue(false);
    }
}
