package io.openim.android.ouiconversation.ui;

import android.os.Bundle;

import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.databinding.ActivityNotificationBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;

public class NotificationActivity extends BaseActivity<ChatVM, ActivityNotificationBinding> implements ChatVM.ViewAction {

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityNotificationBinding.inflate(getLayoutInflater()));
        sink();

        initView();
        init();
        listener();
        if (vm.conversationInfo.val() != null && vm.conversationInfo.val().getUnreadCount() > 0)
            vm.markRead();
    }

    private void listener() {
        vm.messages.observe(this, v -> {
            if (null == v) return;
            messageAdapter.setMessages(v);
            messageAdapter.notifyDataSetChanged();
        });
    }

    private void initView() {
        ChatActivity.LinearLayoutMg linearLayoutManager = new ChatActivity.LinearLayoutMg(this);

        view.recyclerView.setLayoutManager(linearLayoutManager);
        view.recyclerView.addItemDecoration(new DefaultItemDecoration(this.getResources().getColor(android.R.color.transparent), 1, Common.dp2px(16)));
        messageAdapter = new MessageAdapter();
        messageAdapter.bindRecyclerView(view.recyclerView);
        vm.setMessageAdapter(messageAdapter);
        view.recyclerView.setAdapter(messageAdapter);
    }

    void init() {
        String name = getIntent().getStringExtra(Constants.K_NAME);
        String id = getIntent().getStringExtra(Constants.K_ID);
        view.title.setText(name);
        vm.conversationID = id;
        vm.loadHistoryMessage();
    }


    @Override
    public void scrollToPosition(int position) {

    }

    @Override
    public void closePage() {

    }
}
