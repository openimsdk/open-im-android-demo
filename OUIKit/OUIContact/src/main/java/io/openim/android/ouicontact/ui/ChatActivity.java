package io.openim.android.ouicontact.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import io.openim.android.ouicontact.adapter.MessageAdapter;
import io.openim.android.ouicontact.databinding.ActivityChatBinding;
import io.openim.android.ouicontact.utils.Constant;
import io.openim.android.ouicontact.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;

@Route(path = Routes.Contact.CHAT)
public class ChatActivity extends BaseActivity<ChatVM> implements  ChatVM.ViewAction {

    private ActivityChatBinding view;

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view = ActivityChatBinding.inflate(getLayoutInflater());
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
        bindVM(ChatVM.class);
        view.setChatVM(vm);
        setContentView(view.getRoot());

        String userId = getIntent().getStringExtra(Constant.K_USER_ID);
        String name = getIntent().getStringExtra(Constant.K_NAME);
        if (null != userId)
            vm.otherSideID = userId;

        initView(name);
        listener();

        super.onCreate(savedInstanceState);

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
        view.nickName.setText(name);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //倒叙
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setReverseLayout(true);

        view.recyclerView.setLayoutManager(linearLayoutManager);
        view.recyclerView.addItemDecoration(new DefaultItemDecoration(this.getResources().getColor(android.R.color.transparent), 1, Common.dp2px(16)));
        messageAdapter = new MessageAdapter();
        vm.setMessageAdapter(messageAdapter);
        view.recyclerView.setAdapter(messageAdapter);
        vm.messages.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            messageAdapter.setMessages(v);
            messageAdapter.notifyDataSetChanged();
        });
        view.recyclerView.setOnTouchListener((v, event) -> {
            view.input.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            return false;
        });

        view.send.setOnClickListener(v->{
            vm.sendMsg();
            view.input.setText("");
        });

    }


    //记录原始窗口高度
    private int mWindowHeight = 0;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
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
                vm.sendMsgReadReceipt(firstVisiblePosition, lastVisiblePosition);
            }
        });
    }


    @Override
    public void scrollToPosition(int position) {
        view.recyclerView.scrollToPosition(position);
    }
}
