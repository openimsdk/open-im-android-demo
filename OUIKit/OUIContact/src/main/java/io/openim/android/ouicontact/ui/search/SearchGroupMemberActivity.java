package io.openim.android.ouicontact.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouicontact.databinding.ActivityOftenSerchBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.GroupMemberVM;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.sdk.models.GroupMembersInfo;
@Route(path = Routes.Contact.SEARCH_GROUP_MEMBER)
public class SearchGroupMemberActivity extends BaseActivity<SearchVM, ActivityOftenSerchBinding> {

    private final Handler handler = new Handler();


    private RecyclerViewAdapter adapter;
    private GroupMemberVM memberVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        memberVM = Easy.find(GroupMemberVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityOftenSerchBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        listener();
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter<GroupMembersInfo,
            ViewHol.ItemViewHo>(ViewHol.ItemViewHo.class) {
            @Override
            public void onBindView(@NonNull ViewHol.ItemViewHo holder, GroupMembersInfo data,
                                   int position) {
                holder.view.select.setVisibility(memberVM.isSearchSingle?View.GONE
                    :(memberVM.isAt() || memberVM.isMultiple()
                    ? View.VISIBLE
                    : View.GONE));

                int index =
                    memberVM.choiceList.val().indexOf(new MultipleChoice(data.getUserID()));
                boolean isChecked = index != -1;
                holder.view.select.setChecked(isChecked);
                boolean isEnabled=true;
                if (isChecked)
                    isEnabled= memberVM.choiceList.val().get(index).isEnabled;
                holder.view.getRoot().setIntercept(!isEnabled);
                holder.view.getRoot().setAlpha(isEnabled  ? 1f : 0.3f);

                holder.view.avatar.load(data.getFaceURL());
                Common.stringBindForegroundColorSpan(holder.view.nickName, data.getNickname(),
                    vm.searchContent.getValue());

                holder.view.item.setOnClickListener(v -> {
                   boolean isSingle= memberVM.isSingle();
                    if (memberVM.isSearchSingle)
                        isSingle=true;

                    if (memberVM.isCheck() || isSingle) {
                        MultipleChoice choice =
                            new MultipleChoice(data.getUserID());
                        choice.name = data.getNickname();
                        choice.icon = data.getFaceURL();
                        memberVM.addChoice(choice);
                        if (memberVM.isCheck()){
                            memberVM.onFinish(SearchGroupMemberActivity.this);
                        }else {
                            memberVM.choiceList.update();
                            finish();
                        }
                        return;
                    }
                    boolean isSelect = !isChecked;
                    if (isSelect) {
                        if (memberVM.choiceList.val().size() >= memberVM.maxNum) {
                            toast(String.format(getString(io.openim.android.ouicore.R.string.select_tips), memberVM.maxNum));
                            return;
                        }
                        MultipleChoice choice =
                            new MultipleChoice(data.getUserID());
                        choice.name = data.getNickname();
                        choice.icon = data.getFaceURL();
                        memberVM.addChoice(choice);
                    } else {
                        memberVM.removeChoice(data.getUserID());
                    }
                    memberVM.choiceList.update();
                    notifyItemChanged(getItems().indexOf(data));
                });
            }
        });
            adapter.setItems(vm.groupMembersInfo.getValue());
    }

    private void listener() {
        view.cancel.setOnClickListener(v -> finish());
        vm.groupMembersInfo.observe(this, groupMembersInfos->{
            if (memberVM.isAt())
                memberVM.removeSelf(groupMembersInfos);
            adapter.notifyDataSetChanged();
        });
        vm.userInfo.observe(this, friendInfos -> adapter.notifyDataSetChanged());

        vm.searchContent.observe(this, s -> {
            vm.groupMembersInfo.getValue()
                .clear();
            adapter.notifyDataSetChanged();

            if (!s.isEmpty())
                loadMore();
        });

        view.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty())
                    vm.searchContent.setValue("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String input = s.toString();
                    vm.page = 0;
                    vm.searchContent.setValue(input);
                }, 500);
            }
        });


            adapter.setOnLoadMoreListener(view.recyclerview, vm.pageSize, () -> {
                vm.page++;
                loadMore();
            });
    }

    private void loadMore() {
        vm.searchGroupMemberByNickname(memberVM.groupId, vm.searchContent.getValue());
    }
}
