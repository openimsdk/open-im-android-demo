package io.openim.android.ouigroup.ui;

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
import io.openim.android.sdk.enums.ConversationType;

@Route(path = Routes.Group.SELECT_TARGET)
public class SelectTargetActivityV3 extends BaseActivity<BaseViewModel,
    ActivityCreateGroupV3Binding> {
    MultipleChoiceVM multipleChoiceVM;
    RecyclerViewAdapter<MsgConversation, ViewHol.ItemViewHo> adapter;

    /**
     * 发起群聊
     * true 隐藏最近会话、隐藏群，只显示好友
     */
    private boolean isCreateGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityCreateGroupV3Binding.inflate(getLayoutInflater()));
        multipleChoiceVM = Easy.installVM(MultipleChoiceVM.class);
        multipleChoiceVM.bindDataToView(view.bottom);
        multipleChoiceVM.showPopAllSelectFriends(view.bottom,
            LayoutPopSelectedFriendsBinding.inflate(getLayoutInflater()));
        multipleChoiceVM.submitTap(view.bottom.submit);

        init();
        initView();
        listener();

        view.myFriends.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Group.CREATE_GROUP).withBoolean(Constant.K_RESULT,
                true).navigation();
        });

        view.group.setOnClickListener(v -> {
            startActivity(new Intent(this, AllGroupActivity.class));
        });
        view.searchView.setOnClickListener(v -> {
            List<String> ids = new ArrayList<>();
            for (MultipleChoice multipleChoice : multipleChoiceVM.metaData.val()) {
                ids.add(multipleChoice.key);
            }
            Postcard postcard = ARouter.getInstance().build(Routes.Contact.SEARCH_FRIENDS_GROUP);
            LogisticsCenter.completion(postcard);
            launcher.launch(new Intent(this, postcard.getDestination()).putExtra(Constant.K_RESULT,
                (Serializable) ids).putExtra(Constant.IS_SELECT_FRIEND, isCreateGroup));
        });
    }

    private void listener() {
        multipleChoiceVM.metaData.observe(this, v -> {
            if (null != adapter)
                adapter.notifyDataSetChanged();
        });
    }

    private void initView() {
        int visibility = isCreateGroup ? View.GONE : View.VISIBLE;

        view.divider2.getRoot().setVisibility(isCreateGroup?View.VISIBLE:View.GONE);
        view.group.setVisibility(visibility);
        view.recentContact.setVisibility(visibility);

        if (!isCreateGroup) {
            view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<MsgConversation,
                ViewHol.ItemViewHo>
                (ViewHol.ItemViewHo.class) {

                @Override
                public void onBindView(@NonNull ViewHol.ItemViewHo holder, MsgConversation data,
                                       int position) {
                    boolean isGroup =
                        data.conversationInfo.getConversationType() != ConversationType.SINGLE_CHAT;
                    String id = isGroup ? data.conversationInfo.getGroupID()
                        : data.conversationInfo.getUserID();
                    String faceURL = data.conversationInfo.getFaceURL();
                    String name = data.conversationInfo.getShowName();
                    holder.view.avatar.load(faceURL,
                        isGroup,
                        isGroup ? null : name);
                    holder.view.nickName.setText(name);

                    holder.view.select.setVisibility(View.VISIBLE);
                    holder.view.select.setChecked(multipleChoiceVM.contains(new MultipleChoice(id)));
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
            adapter.setItems(vmByCache.conversations.getValue());
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
        multipleChoiceVM.isCreateGroup = isCreateGroup =
            getIntent().getBooleanExtra(Constant.K_RESULT, false);
    }

    @Override
    protected void fasterDestroy() {
        super.fasterDestroy();
        Easy.delete(MultipleChoiceVM.class);
    }
}
