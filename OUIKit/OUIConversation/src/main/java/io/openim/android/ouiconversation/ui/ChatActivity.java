package io.openim.android.ouiconversation.ui;

import static io.openim.android.ouicore.utils.Constant.GROUP_ID;
import static io.openim.android.ouicore.utils.Constant.ID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.databinding.ActivityChatBinding;
import io.openim.android.ouiconversation.utils.Constant;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;

@Route(path = Routes.Conversation.CHAT)
public class ChatActivity extends BaseActivity<ChatVM, ActivityChatBinding> implements ChatVM.ViewAction {

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //userId 与 GROUP_ID 互斥
        String userId = getIntent().getStringExtra(ID);
        String groupId = getIntent().getStringExtra(GROUP_ID);
        String name = getIntent().getStringExtra(io.openim.android.ouicore.utils.Constant.K_NAME);
        bindVM(ChatVM.class);
        if (null != userId)
            vm.otherSideID = userId;
        if (null != groupId) {
            vm.isSingleChat = false;
            vm.groupID = groupId;
        }
        super.onCreate(savedInstanceState);

        bindViewDataBinding(ActivityChatBinding.inflate(getLayoutInflater()));
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
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
        view.inputGroup.setChatVM(vm);

        view.nickName.setText(name);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //倒叙
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setReverseLayout(true);

        view.recyclerView.setLayoutManager(linearLayoutManager);
        view.recyclerView.addItemDecoration(new DefaultItemDecoration(this.getResources().getColor(android.R.color.transparent), 1, Common.dp2px(16)));
        messageAdapter = new MessageAdapter();
        messageAdapter.bindRecyclerView(view.recyclerView);

        vm.setMessageAdapter(messageAdapter);
        view.recyclerView.setAdapter(messageAdapter);
        vm.messages.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            messageAdapter.setMessages(v);
            messageAdapter.notifyDataSetChanged();
        });
        view.recyclerView.setOnTouchListener((v, event) -> {
            view.inputGroup.clearFocus();
            Common.hideKeyboard(this,v);
            view.inputGroup.setExpandHide();
            return false;
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
            RelativeLayout.LayoutParams inputLayoutParams = (RelativeLayout.LayoutParams) view.inputGroup.getLayoutParams();
            if (mWindowHeight == height) {
                inputLayoutParams.bottomMargin = 0;
            } else {
                //两次窗口高度相减，就是软键盘高度
                int softKeyboardHeight = mWindowHeight - height;
                inputLayoutParams.bottomMargin = softKeyboardHeight;
            }
            view.inputGroup.setLayoutParams(inputLayoutParams);
        }
    };

    private void listener() {
        view.back.setOnClickListener(v -> finish());

        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerView.getLayoutManager();
                int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == vm.messages.getValue().size() - 1) {
                    vm.loadHistoryMessage();
                }
                if (vm.isSingleChat)
                    vm.sendMsgReadReceipt(firstVisiblePosition, lastVisiblePosition);
            }
        });
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        view.inputGroup.dispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void scrollToPosition(int position) {
        view.recyclerView.scrollToPosition(position);
    }
}
