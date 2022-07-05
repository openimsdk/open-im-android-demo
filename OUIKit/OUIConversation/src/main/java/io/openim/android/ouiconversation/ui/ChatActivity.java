package io.openim.android.ouiconversation.ui;

import static io.openim.android.ouicore.utils.Constant.GROUP_ID;
import static io.openim.android.ouicore.utils.Constant.ID;
import static io.openim.android.ouicore.utils.Constant.NOTICE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.databinding.ActivityChatBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.widget.BottomInputCote;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;

@Route(path = Routes.Conversation.CHAT)
public class ChatActivity extends BaseActivity<ChatVM, ActivityChatBinding> implements ChatVM.ViewAction {

    private MessageAdapter messageAdapter;
    private BottomInputCote bottomInputCote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //userId 与 GROUP_ID 互斥
        String userId = getIntent().getStringExtra(ID);
        String groupId = getIntent().getStringExtra(GROUP_ID);
        String name = getIntent().getStringExtra(io.openim.android.ouicore.utils.Constant.K_NAME);
        NotificationMsg notificationMsg = (NotificationMsg) getIntent().getSerializableExtra(NOTICE);
        bindVM(ChatVM.class);
        if (null != userId)
            vm.otherSideID = userId;
        if (null != groupId) {
            vm.isSingleChat = false;
            vm.groupID = groupId;
        }
        if (null != notificationMsg)
            vm.notificationMsg.setValue(notificationMsg);
        super.onCreate(savedInstanceState);

        bindViewDataBinding(ActivityChatBinding.inflate(getLayoutInflater()));
        sink();
        view.setChatVM(vm);

        initView(name);
        listener();

        setTouchClearFocus(false);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(String name) {
        bottomInputCote = new BottomInputCote(this, view.layoutInputCote);
        bottomInputCote.setChatVM(vm);

        view.nickName.setText(name);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //倒叙
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

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
        });
        view.recyclerView.setOnTouchListener((v, event) -> {
            bottomInputCote.clearFocus();
            Common.hideKeyboard(this, v);
            bottomInputCote.setExpandHide();
            return false;
        });
        view.recyclerView.post(() -> scrollToPosition(0));
        view.recyclerView.addOnLayoutChangeListener((v, i, i1, i2, i3, i4, i5, i6, i7) -> {
            if (i3 < i7) { // bottom < oldBottom
                scrollToPosition(0);
            }
        });

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
        view.notice.setOnClickListener(v -> ARouter.getInstance().build(Routes.Group.NOTICE_DETAIL)
            .withSerializable(NOTICE, vm.notificationMsg.getValue()).navigation());

        view.back.setOnClickListener(v -> finish());

        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerView.getLayoutManager();
                int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == vm.messages.getValue().size() - 1
                    && vm.messages.getValue().size() >= vm.count) {
                    vm.loadHistoryMessage();
                }
                if (vm.isSingleChat)
                    vm.sendMsgReadReceipt(firstVisiblePosition, lastVisiblePosition);
            }
        });

        view.more.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                if (vm.isSingleChat) {

                } else {
                    ARouter.getInstance().build(Routes.Group.MATERIAL)
                        .withString(io.openim.android.ouicore.utils.Constant.GROUP_ID, vm.groupID).navigation();
                }
            }
        });
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
}
