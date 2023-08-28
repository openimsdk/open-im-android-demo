package io.openim.android.ouicontact.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivityAllFriendBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.ex.User;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.MultipleChoiceVM;
import io.openim.android.ouicore.vm.SocialityVM;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.CardElem;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.OfflinePushInfo;

@Route(path = Routes.Contact.ALL_FRIEND)
public class AllFriendActivity extends BaseActivity<SocialityVM, ActivityAllFriendBinding> {

    private RecyclerViewAdapter<ExUserInfo, RecyclerView.ViewHolder> adapter;
    //从聊天跳转过来
    private boolean formChat;
    //从推荐好友跳转过来 带的userinfo
    private User recommend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SocialityVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityAllFriendBinding.inflate(getLayoutInflater()));
        sink();
        formChat = getIntent().getBooleanExtra("formChat", false);
        recommend = (User) getIntent().getSerializableExtra("recommend");
        vm.getAllFriend();

        listener();
        initView();
    }

    private void initView() {
        view.scrollView.fullScroll(View.FOCUS_DOWN);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<ExUserInfo, RecyclerView.ViewHolder>() {
            private int STICKY = 1;
            private int ITEM = 2;

            private String lastSticky = "";

            @Override
            public void setItems(List<ExUserInfo> items) {
                lastSticky = items.get(0).sortLetter;
                items.add(0, getExUserInfo());
                for (int i = 0; i < items.size(); i++) {
                    ExUserInfo userInfo = items.get(i);
                    if (!lastSticky.equals(userInfo.sortLetter)) {
                        lastSticky = userInfo.sortLetter;
                        items.add(i, getExUserInfo());
                    }
                }
                super.setItems(items);
            }

            @NonNull
            private ExUserInfo getExUserInfo() {
                ExUserInfo exUserInfo = new ExUserInfo();
                exUserInfo.sortLetter = lastSticky;
                exUserInfo.isSticky = true;
                return exUserInfo;
            }

            @Override
            public int getItemViewType(int position) {
                return getItems().get(position).isSticky ? STICKY : ITEM;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == ITEM)
                    return new ViewHol.ItemViewHo(parent);

                return new ViewHol.StickyViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, ExUserInfo data, int position) {
                if (getItemViewType(position) == ITEM) {
                    ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                    FriendInfo friendInfo = data.userInfo.getFriendInfo();
                    itemViewHo.view.avatar.load(friendInfo.getFaceURL());
                    itemViewHo.view.nickName.setText(friendInfo.getNickname());
                    itemViewHo.view.select.setVisibility(View.GONE);
                    itemViewHo.view.getRoot().setOnClickListener(v -> {
                        if (null!=recommend) {
                            CommonDialog commonDialog = new CommonDialog(AllFriendActivity.this);
                            commonDialog.show();
                            LayoutCommonDialogBinding mainView = commonDialog.getMainView();
                            mainView.tips.setText(String.format(getString(io.openim.android.ouicore.R.string.recommend_who)
                                , friendInfo.getNickname()));
                            mainView.cancel.setOnClickListener(v1 -> commonDialog.dismiss());
                            mainView.confirm.setOnClickListener(v1 -> {
                                commonDialog.dismiss();
                                sendCardMessage(friendInfo);
                            });
                            return;
                        }
                        if (formChat) {
                            sendChatWindow(friendInfo);
                            return;
                        }
                        ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                            .withString(Constant.K_ID, friendInfo.getUserID()).navigation(AllFriendActivity.this, 1001);
                    });
                } else {
                    ViewHol.StickyViewHo stickyViewHo = (ViewHol.StickyViewHo) holder;
                    stickyViewHo.view.title.setText(data.sortLetter);
                }
            }
        };
        view.recyclerView.setAdapter(adapter);
    }

    private void sendChatWindow(FriendInfo friendInfo) {
      try {
          MultipleChoiceVM multipleChoiceVM = Easy.find(MultipleChoiceVM.class);
          if (null != multipleChoiceVM) {
              multipleChoiceVM.addMetaData(friendInfo.getUserID(),
                  friendInfo.getNickname(), friendInfo.getFaceURL());
              multipleChoiceVM.shareCard();
          }
      }catch (Exception ignored){}
    }

    private void sendCardMessage(FriendInfo friendInfo) {
        CardElem cardElem=new CardElem();
        cardElem.setUserID(recommend.key);
        cardElem.setNickname(recommend.getName());
        cardElem.setFaceURL(recommend.getFaceUrl());
        Message message = OpenIMClient.getInstance().messageManager.createCardMessage(cardElem);
        OfflinePushInfo offlinePushInfo = new OfflinePushInfo(); // 离线推送的消息备注；不为null
        OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
            @Override
            public void onError(int code, String error) {
                toast(error + code);
            }

            @Override
            public void onProgress(long progress) {
            }

            @Override
            public void onSuccess(Message message) {
                toast(AllFriendActivity.this.
                    getString(io.openim.android.ouicore.R.string.send_succ));
                finish();
            }
        }, message, friendInfo.getUserID(), null, offlinePushInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) {
            vm.getAllFriend();
        }
    }

    private ActivityResultLauncher<Intent> searchFriendLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        result -> {
        try {
            if (result.getResultCode()!=RESULT_OK)return;

            String uid = result.getData().getStringExtra(Constant.K_ID);
            if (formChat){
                for (ExUserInfo item : adapter.getItems()) {
                    if (null!=item.userInfo&&item.userInfo.getUserID().equals(uid)){
                        sendChatWindow(item.userInfo.getFriendInfo());
                        return;
                    }
                }
            }
            ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                .withString(Constant.K_ID, uid)
                .navigation();
        } catch (Exception ignored) {

        }
    });

    private void listener() {
        view.searchView.setOnClickListener(v ->
            {
                Postcard postcard = ARouter.getInstance().build(Routes.Contact.SEARCH_FRIENDS);
                LogisticsCenter.completion(postcard);
                searchFriendLauncher.launch(new Intent(this, postcard.getDestination()));
            }
        );
        vm.letters.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            StringBuilder letters = new StringBuilder();
            for (String s : v) {
                letters.append(s);
            }
            view.sortView.setLetters(letters.toString());
        });


        vm.exUserInfo.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            List<ExUserInfo> exUserInfos = new ArrayList<>(v);
            adapter.setItems(exUserInfos);
        });

        view.sortView.setOnLetterChangedListener((letter, position) -> {
            for (int i = 0; i < adapter.getItems().size(); i++) {
                ExUserInfo exUserInfo = adapter.getItems().get(i);
                if (!exUserInfo.isSticky)
                    continue;
                if (exUserInfo.sortLetter.equalsIgnoreCase(letter)) {
                    View viewByPosition = view.recyclerView.getLayoutManager().findViewByPosition(i);
                    if (viewByPosition != null) {
                        view.scrollView.smoothScrollTo(0, viewByPosition.getTop());
                    }
                    return;
                }
            }
        });

    }
}
