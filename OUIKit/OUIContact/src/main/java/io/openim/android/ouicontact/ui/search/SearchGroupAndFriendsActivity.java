package io.openim.android.ouicontact.ui.search;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.openim.android.ouicontact.databinding.ActivityOftenSerchBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Contact.SEARCH_FRIENDS_GROUP)
public class SearchGroupAndFriendsActivity extends BaseActivity<SearchVM,
    ActivityOftenSerchBinding> {

    private final Handler handler = new Handler();

    private static final int TITLE = 0;
    private static final int CONTACT_ITEM = 1;
    private static final int GROUP_ITEM = 2;
    private static final int ORG_ITEM = 3;
    private RecyclerViewAdapter adapter;

    private Set<MultipleChoice> result = new HashSet<>();
    //只选择联系人
    private boolean isOnlyFriend;
    //不为空表示多选
    private List<MultipleChoice> choices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityOftenSerchBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    public boolean isMultipleSelect() {
        return null != choices;
    }

    @Override
    public void onBackPressed() {
        setResult();
        super.onBackPressed();
    }

    private void setResult() {
        if (!result.isEmpty()) {
            setResult(RESULT_OK, new Intent().putExtra(Constants.K_RESULT, (Serializable) result));

        }
    }

    private void listener() {
        view.cancel.setOnClickListener(v -> {
            setResult();
            finish();
        });
        vm.searchContent.observe(this, s -> {
            clearData();
            if (s.isEmpty()) return;
            vm.page = 1;

            if (isOnlyFriend) {
                vm.searchFriendV2();
                return;
            }
            vm.searchFriendV2();
            vm.searchGroupV2();
        });

        view.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) vm.searchContent.setValue("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String input = s.toString();
                    vm.page = 1;
                    vm.searchContent.setValue(input);
                }, 500);

            }
        });

        vm.userInfo.observe(this, userInfos -> {
            addNape(getString(io.openim.android.ouicore.R.string.contact), userInfos);
        });
        vm.groupsInfo.observe(this, groupInfos -> {
            addNape(getString(io.openim.android.ouicore.R.string.group), groupInfos);
        });
    }

    private void addNape(String title, List data) {
        if (data.isEmpty()) return;
        int start = adapter.getItems().size();
        adapter.getItems().add(title);//包含1表示有查看更多
        adapter.getItems().addAll(data);
        adapter.notifyItemRangeInserted(start, adapter.getItems().size());
    }

    private void clearData() {
        vm.clearData();
        adapter.getItems().clear();
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter() {

            @Override
            public int getItemViewType(int position) {
                Object o = getItems().get(position);
                if (o instanceof String) return TITLE;
                if (o instanceof UserInfo) return CONTACT_ITEM;
                if (o instanceof GroupInfo) return GROUP_ITEM;
                return super.getItemViewType(position);
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {
                if (viewType == TITLE) return new ViewHol.TitleViewHolder(parent);
                if (viewType == CONTACT_ITEM || viewType == GROUP_ITEM || viewType == ORG_ITEM)
                    return new ViewHol.ItemViewHo(parent);

                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, Object data,
                                   int position) {
                if (getItemViewType(position) == TITLE) {
                    String title = (String) data;
                    ViewHol.TitleViewHolder titleViewHolder = (ViewHol.TitleViewHolder) holder;
                    titleViewHolder.view.title.setText(title);
                } else {
                    ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                    itemViewHo.view.select.setVisibility(isMultipleSelect() ? View.VISIBLE :
                        View.GONE);

                    final Intent intent = new Intent();

                    MultipleChoice multipleChoice = new MultipleChoice();
                    String id = "";
                    if (data instanceof UserInfo) {
                        //联系人
                        UserInfo da = (UserInfo) data;
                        itemViewHo.view.avatar.load(da.getFaceURL());
                        itemViewHo.view.nickName.setText(da.getNickname());

                        intent.putExtra(Constants.K_ID, id = da.getUserID());
                        intent.putExtra(Constants.K_NAME, da.getNickname());
                        multipleChoice.isGroup = false;
                        multipleChoice.name = da.getNickname();
                        multipleChoice.icon = da.getFaceURL();
                    }
                    if ((data instanceof GroupInfo)) {
                        //群组
                        GroupInfo groupInfo = (GroupInfo) data;
                        itemViewHo.view.avatar.load(groupInfo.getFaceURL(), true);
                        itemViewHo.view.nickName.setText(groupInfo.getGroupName());

                        intent.putExtra(Constants.K_GROUP_ID, id = groupInfo.getGroupID());
                        intent.putExtra(Constants.K_NAME, groupInfo.getGroupName());
                        multipleChoice.isGroup = true;
                        multipleChoice.name = groupInfo.getGroupName();
                        multipleChoice.icon = groupInfo.getFaceURL();
                    }
                    multipleChoice.key = id;
                    MultipleChoice target = null;
                    itemViewHo.view.select.setChecked(false);
                    if (isMultipleSelect()) {
                        int index = choices.indexOf(new MultipleChoice(id));
                        if (index != -1) {
                            itemViewHo.view.select.setChecked(true);
                            target = choices.get(index);
                            itemViewHo.view.select.setEnabled(target.isEnabled);
                            itemViewHo.view.select.setAlpha(target.isEnabled ? 1f : 0.5f);
                        }
                    }

                    MultipleChoice finalTarget = target;
                    itemViewHo.view.getRoot().setOnClickListener(v -> {
                        if (null != finalTarget && !finalTarget.isEnabled) return;

                        if (isMultipleSelect()) {
                            itemViewHo.view.select.setChecked(!itemViewHo.view.select.isChecked());
                            multipleChoice.isSelect = itemViewHo.view.select.isChecked();
                            result.add(multipleChoice);

                            if (multipleChoice.isSelect)
                                choices.add(multipleChoice);
                            else
                                choices.remove(multipleChoice);
                            return;
                        }

                        try {
                            SelectTargetVM selectTargetVM = Easy.find(SelectTargetVM.class);
                            if (null != selectTargetVM) {
                                if (selectTargetVM.isSingleSelect()) {
                                    if (null != selectTargetVM.metaData.val()
                                        && !selectTargetVM.metaData.val().isEmpty()) {
                                        selectTargetVM.removeMetaData(selectTargetVM.metaData.val().get(0).key);
                                    }
                                    selectTargetVM.addMetaData(multipleChoice.key,
                                        multipleChoice.name, multipleChoice.icon);

                                } else if (selectTargetVM.isJumpDetail()) {
                                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                                        .withString(Constants.K_ID, multipleChoice.key)
                                        .navigation();
                                }
                                selectTargetVM.finishIntention();
                            }
                            return;
                        } catch (Exception ignore) {
                        }


                        setResult(RESULT_OK, intent);
                        finish();
                    });

                }
            }
        });
        adapter.setItems(new ArrayList());
    }

    void init() {
        choices = (List<MultipleChoice>) getIntent().getSerializableExtra(Constants.K_RESULT);
        isOnlyFriend = getIntent().getBooleanExtra(Constants.IS_SELECT_FRIEND, false);
    }
}
