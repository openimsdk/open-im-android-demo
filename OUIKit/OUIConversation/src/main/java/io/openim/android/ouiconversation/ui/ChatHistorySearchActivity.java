package io.openim.android.ouiconversation.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityChatHistorySearchBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SearchResultItem;

@Route(path = Routes.Conversation.CHAT_HISTORY)
public class ChatHistorySearchActivity extends BaseActivity<ChatVM, ActivityChatHistorySearchBinding> implements ChatVM.ViewAction {
    private final Handler handler = new Handler();
    private int page = 1;
    public MutableLiveData<String> inputKey = new MutableLiveData<>("");
    private RecyclerViewAdapter adapter;
    boolean isStartSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityChatHistorySearchBinding.inflate(getLayoutInflater()));
        sink();
        init();
        listener();
    }

    private void listener() {
        view.picture.setOnClickListener(v -> startActivity(new Intent(this,
            MediaHistoryActivity.class).putExtra(Constant.K_RESULT, true)));
        view.video.setOnClickListener(v -> startActivity(new Intent(this,
            MediaHistoryActivity.class)));
        view.file.setOnClickListener(v -> startActivity(new Intent(this,
            FileHistoryActivity.class)));

        view.cancel.setOnClickListener(v -> finish());
        inputKey.observe(this, s -> {
            view.notFind.setVisibility(View.GONE);
            if (s.isEmpty()) {
                isStartSearch = false;
                view.recyclerview.setVisibility(View.GONE);
                view.searchMenu.setVisibility(View.VISIBLE);
                return;
            } else {
                view.searchMenu.setVisibility(View.GONE);
                view.recyclerview.setVisibility(View.VISIBLE);
            }
            isStartSearch = true;
            vm.searchLocalMessages(s, page);
        });
        vm.searchMessageItems.observe(this, list -> {
            if (list.isEmpty() && isStartSearch) {
                view.recyclerview.setVisibility(View.GONE);
                view.searchMenu.setVisibility(View.GONE);
                view.notFind.setVisibility(View.VISIBLE);
                return;
            }
            adapter.notifyDataSetChanged();
        });
    }

    void init() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<Message, ViewHol.ContactItemHolder>(ViewHol.ContactItemHolder.class) {


            @Override
            public void onBindView(@NonNull ViewHol.ContactItemHolder holder, Message data, int position) {
                holder.viewBinding.avatar.load(data.getSenderFaceUrl());
                holder.viewBinding.nickName.setText(data.getSenderNickname());
                holder.viewBinding.time.setText(TimeUtil.getTimeString(data.getSendTime()));

                String msg = IMUtil.getMsgParse(data);
                SpannableStringBuilder spannableString = new SpannableStringBuilder(msg);
                int index = msg.indexOf(inputKey.getValue());
                if (index != -1) {
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
                    spannableString.setSpan(colorSpan, index, index + inputKey.getValue().length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.viewBinding.lastMsg.setText(spannableString);
                }
                holder.viewBinding.getRoot().setOnClickListener(v -> {
                    vm.startMsg = data;
                    startActivity(new Intent(ChatHistorySearchActivity.this,
                        ChatActivity.class).putExtra(Constant.K_FROM, true));
                });
            }
        };
        view.recyclerview.setAdapter(adapter);
        adapter.setItems(vm.searchMessageItems.getValue());

        view.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerview.getLayoutManager();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == adapter.getItems().size() - 1
                    && adapter.getItems().size() >= vm.count) {
                    page++;
                    vm.searchLocalMessages(inputKey.getValue(), page);
                }
            }
        });
        view.input.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String input = s.toString();
                    page = 1;
                    inputKey.setValue(input);
                }, 500);
            }
        });
    }

    @Override
    public void scrollToPosition(int position) {

    }

    @Override
    public void closePage() {

    }
}
