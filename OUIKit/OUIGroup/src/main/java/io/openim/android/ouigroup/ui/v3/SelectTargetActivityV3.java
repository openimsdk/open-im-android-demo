package io.openim.android.ouigroup.ui.v3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutPopSelectedFriendsBinding;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.vm.MultipleChoiceVM;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityCreateGroupV3Binding;
import io.openim.android.ouigroup.ui.AllGroupActivity;
import io.openim.android.sdk.enums.ConversationType;

@Route(path = Routes.Group.SELECT_TARGET)
public class SelectTargetActivityV3 extends BaseActivity<BaseViewModel,
    ActivityCreateGroupV3Binding> {

    @NotNull("multipleChoiceVM cannot be empty")
    MultipleChoiceVM multipleChoiceVM;
    RecyclerViewAdapter<MsgConversation, ViewHol.ItemViewHo> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityCreateGroupV3Binding.inflate(getLayoutInflater()));
        multipleChoiceVM = Easy.find(MultipleChoiceVM.class);
        multipleChoiceVM.bindDataToView(view.bottom);
        multipleChoiceVM.showPopAllSelectFriends(view.bottom,
            LayoutPopSelectedFriendsBinding.inflate(getLayoutInflater()));
        multipleChoiceVM.submitTap(view.bottom.submit);

        init();
        initView();
        click();
        listener();


    }

    private void click() {
        view.myFriends.setOnClickListener(v -> {
            if (multipleChoiceVM.isShareCard) {
                ARouter.getInstance().build(Routes.Contact.ALL_FRIEND)
                    .withBoolean("formChat", true).navigation();
            } else {
                ARouter.getInstance().build(Routes.Group.CREATE_GROUP)
                    .withBoolean(Constant.IS_SELECT_FRIEND, true)
                    .withString(Constant.K_NAME, getString(io.openim.android.ouicore.R.string.my_good_friend))
                    .navigation();
            }
        });

        view.group.setOnClickListener(v -> {
            startActivity(new Intent(this, AllGroupActivity.class));
        });
        view.searchView.setOnClickListener(v -> {
            if (multipleChoiceVM.isShareCard){
                ARouter.getInstance().build(Routes.Contact.SEARCH_FRIENDS_GROUP)
                    .withBoolean(Constant.IS_SELECT_FRIEND,true).navigation();
                return;
            }

            List<String> ids = new ArrayList<>();
            for (MultipleChoice multipleChoice : multipleChoiceVM.metaData.val()) {
                ids.add(multipleChoice.key);
            }
            Postcard postcard = ARouter.getInstance().build(Routes.Contact.SEARCH_FRIENDS_GROUP);
            LogisticsCenter.completion(postcard);
            launcher.launch(new Intent(this, postcard.getDestination())
                .putExtra(Constant.K_RESULT, (Serializable) ids)
                .putExtra(Constant.IS_SELECT_FRIEND, multipleChoiceVM.isCreateGroup));
        });
    }

    private void listener() {
        multipleChoiceVM.metaData.observe(this, v -> {
            if (null != adapter) adapter.notifyDataSetChanged();
        });


    }

    private void initView() {
        if (multipleChoiceVM.isShareCard) {
            view.recentContact.setVisibility(View.GONE);
            view.group.setVisibility(View.GONE);
            view.myFriends.setVisibility(View.VISIBLE);
            view.bottom.getRoot().setVisibility(View.GONE);
        } else {
            view.divider2.getRoot().setVisibility(!multipleChoiceVM.isCreateGroup || !multipleChoiceVM.invite ? View.GONE : View.VISIBLE);
            //创建群、邀请入群都不显示group
            view.group.setVisibility(multipleChoiceVM.isCreateGroup || multipleChoiceVM.invite ?
                View.GONE : View.VISIBLE);

            view.recentContact.setVisibility(View.VISIBLE);
            view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<MsgConversation,
                ViewHol.ItemViewHo>(ViewHol.ItemViewHo.class) {

                @Override
                public void onBindView(@NonNull ViewHol.ItemViewHo holder, MsgConversation data,
                                       int position) {
                    boolean isGroup =
                        data.conversationInfo.getConversationType() != ConversationType.SINGLE_CHAT;
                    String id = isGroup ? data.conversationInfo.getGroupID() :
                        data.conversationInfo.getUserID();
                    String faceURL = data.conversationInfo.getFaceURL();
                    String name = data.conversationInfo.getShowName();
                    holder.view.avatar.load(faceURL, isGroup, isGroup ? null : name);
                    holder.view.nickName.setText(name);

                    holder.view.select.setVisibility(View.VISIBLE);

                    holder.view.select.setChecked(multipleChoiceVM.contains(new MultipleChoice(id)));
                    for (MultipleChoice choice : multipleChoiceVM.metaData.val()) {
                        if (choice.key.equals(id)) {
                            holder.view.select.setEnabled(choice.isEnabled);
                        }
                    }

                    holder.view.getRoot().setOnClickListener(v -> {
                        holder.view.select.setChecked(!holder.view.select.isChecked());

                        if (holder.view.select.isChecked()) {
                            MultipleChoice meta = new MultipleChoice(id);
                            meta.isGroup = isGroup;
                            meta.name = name;
                            meta.icon = faceURL;
                            multipleChoiceVM.metaData.val().add(meta);
                            multipleChoiceVM.metaData.update();
                        } else {
                            multipleChoiceVM.removeMetaData(id);
                        }
                    });
                }
            });
            ContactListVM vmByCache = BaseApp.inst().getVMByCache(ContactListVM.class);
            List<MsgConversation> conversations = new ArrayList<>();
            if (multipleChoiceVM.invite || multipleChoiceVM.isCreateGroup) {
                //只保留单聊
                for (MsgConversation msgConversation : vmByCache.conversations.getValue()) {
                    if (msgConversation.conversationInfo.getConversationType() == ConversationType.SINGLE_CHAT)
                        conversations.add(msgConversation);
                }
            } else {
                conversations.addAll(vmByCache.conversations.getValue());
            }
            adapter.setItems(conversations);
        }
    }

    private ActivityResultLauncher<Intent> launcher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), v -> {
            if (v.getResultCode() != RESULT_OK) return;
            Intent intent = v.getData();
            Set<MultipleChoice> set =
                (Set<MultipleChoice>) intent.getSerializableExtra(Constant.K_RESULT);
            for (MultipleChoice data : set) {
                if (data.isSelect) {
                    if (!multipleChoiceVM.contains(data)) {
                        multipleChoiceVM.metaData.val().add(data);
                        multipleChoiceVM.metaData.update();
                    }
                } else {
                    multipleChoiceVM.removeMetaData(data.key);
                }
            }
        });

    void init() {
    }

    @Override
    protected void fasterDestroy() {
        super.fasterDestroy();
        Easy.delete(MultipleChoiceVM.class);
    }
}
