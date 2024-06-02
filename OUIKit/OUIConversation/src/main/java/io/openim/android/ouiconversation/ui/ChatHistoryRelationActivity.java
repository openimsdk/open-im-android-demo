package io.openim.android.ouiconversation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouiconversation.databinding.ActivityChatHistoryRelationBinding;
import io.openim.android.ouiconversation.vm.ChatHistoryRelationVM;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotificationElem;

public class ChatHistoryRelationActivity extends BasicActivity<ActivityChatHistoryRelationBinding> {

    private ChatHistoryRelationVM relationVM;
    private final Handler handler = new Handler();
    private RecyclerViewAdapter<Message, ViewHol.ContactItemHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding(ActivityChatHistoryRelationBinding.inflate(getLayoutInflater()));
        relationVM = Easy.find(ChatHistoryRelationVM.class);
        relationVM.searchContent(relationVM.searchContent);

        initView();
        click();
        listener();
    }

    private void listener() {
        relationVM.searchResultItem.observe(this, searchResultItem -> {
            adapter.setItems(searchResultItem.getMessageList());

            view.avatar.load(relationVM.searchResultItem.val().getFaceURL(), true,
                relationVM.searchResultItem.val().getShowName());
            view.groupName.setText(relationVM.searchResultItem.val().getShowName());
        });

        view.searchView.getEditText().addTextChangedListener(new TextWatcher() {
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
                    relationVM.searchContent = s.toString();
                    relationVM.page = 1;
                    relationVM.searchContent(relationVM.searchContent);
                }, 500);
            }
        });
        adapter.setOnLoadMoreListener(view.recyclerView, relationVM.count, () -> {
            relationVM.page++;
            relationVM.searchContent(view.searchView.getEditText().toString());
        });
    }

    private void click() {
        view.groupInfo.setOnClickListener(new OnDedrepClickListener() {
            private String groupID;
            private String userID;

            @Override
            public void click(View v) {

                Message message = relationVM.searchResultItem.val().getMessageList().get(0);
                boolean isSingleChat =
                    message.getSessionType() == ConversationType.SINGLE_CHAT;
                if (isSingleChat) {
                    userID = message.getRecvID();
                    if (message.getRecvID()
                        .equals(BaseApp.inst().loginCertificate.userID))
                        //表示是对方发送的消息
                        userID = message.getSendID();
                } else
                    groupID = message.getGroupID();

                ARouter.getInstance().build(Routes.Conversation.CHAT)
                    .withString(Constants.K_GROUP_ID, groupID)
                    .withString(Constants.K_ID, userID)
                    .navigation();
            }
        });
    }

    private void initView() {
        view.searchView.getEditText().setText(relationVM.searchContent);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView
            .setAdapter(adapter =
                new RecyclerViewAdapter<Message, ViewHol.ContactItemHolder>(ViewHol.ContactItemHolder.class) {


                    @Override
                    public void onBindView(@NonNull ViewHol.ContactItemHolder holder, Message data,
                                           int position) {
                        holder.viewBinding.avatar.load(data.getSenderFaceUrl(),
                            data.getSenderNickname());
                        holder.viewBinding.nickName.setText(data.getSenderNickname());
                        try {
                            CharSequence content = IMUtil.getMsgParse(data);
                            holder.viewBinding.lastMsg.setText(content);
                            holder.viewBinding.lastMsg.setText(IMUtil.buildClickAndColorSpannable(new
                                SpannableStringBuilder(content), relationVM.searchContent, null));
                        } catch (Exception ignore) {
                        }

                        holder.viewBinding.getRoot().setOnClickListener(new OnDedrepClickListener() {
                            @Override
                            public void click(View v) {
                                toChatHistory(data);
                            }

                            private void toChatHistory(Message message) {
                                ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
                                if (null == chatVM) {
                                    chatVM = new ChatVM();
                                    BaseApp.inst().putVM(chatVM);
                                }
                                chatVM.startMsg = message;
                                chatVM.isSingleChat =
                                    message.getSessionType() == ConversationType.SINGLE_CHAT;
                                if (chatVM.isSingleChat) {
                                    chatVM.userID = message.getRecvID();
                                    if (message.getRecvID()
                                        .equals(BaseApp.inst().loginCertificate.userID))
                                        //表示是对方发送的消息
                                        chatVM.userID = message.getSendID();
                                } else
                                    chatVM.groupID = message.getGroupID();
                                NotificationElem notificationElem = message.getNotificationElem();
                                if (null != notificationElem) {
                                    NotificationMsg notificationMsg =
                                        GsonHel.fromJson(notificationElem.getDetail(),
                                            NotificationMsg.class);
                                    chatVM.notificationMsg.setValue(notificationMsg);
                                }
                                startActivity(new Intent(ChatHistoryRelationActivity.this,
                                    ChatActivity.class).putExtra(Constants.K_FROM, true));
                            }
                        });
                    }
                });
    }


    @Override
    protected void recycle() {
        super.recycle();
        Easy.delete(ChatHistoryRelationVM.class);
    }
}
