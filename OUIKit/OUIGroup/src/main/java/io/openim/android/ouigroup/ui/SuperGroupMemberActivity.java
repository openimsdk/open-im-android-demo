package io.openim.android.ouigroup.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.ISubscribe;
import io.openim.android.ouicore.base.vm.Subject;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutMemberActionBinding;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.GroupMemberVM;
import io.openim.android.ouigroup.databinding.ActivitySuperGroupMemberBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.enums.GroupRole;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Group.SUPER_GROUP_MEMBER)
public class SuperGroupMemberActivity extends BasicActivity<ActivitySuperGroupMemberBinding> {
    private RecyclerViewAdapter adapter;
    private GroupMemberVM vm;
    private GroupVM groupVM;
    private String vmTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vmTag = getIntent().getStringExtra(Constant.K_RESULT);
        vm = Easy.find(GroupMemberVM.class, vmTag);
        try {
            groupVM = Easy.find(GroupVM.class);
        } catch (Exception ignore) {
        }
        super.onCreate(savedInstanceState);
        viewBinding(ActivitySuperGroupMemberBinding.inflate(getLayoutInflater()));

        initView();
        listener();
    }


    private void listener() {
        if (null != groupVM) {
            groupVM.subscribe(this, subject -> {
                if (Objects.equals(subject.key,
                    Constant.Event.UPDATE_GROUP_INFO + "")) {
                    update();
                }
            });
        }
        view.atAll.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                MultipleChoice multipleChoice = new MultipleChoice(IMUtil.AT_ALL);
                multipleChoice.name = getString(io.openim.android.ouicore.R.string.all_person);
                vm.addChoice(multipleChoice);
                vm.onFinish(SuperGroupMemberActivity.this);
                finish();
            }
        });
        view.bottomLayout.submit.setOnClickListener(v -> {
            vm.onFinish(this);
            finish();
        });
        view.searchView.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Contact.SEARCH_GROUP_MEMBER).navigation();
        });
        adapter.setOnLoadMoreListener(view.recyclerview, vm.pageSize - 1, //有可能移除了自己
            () -> {
                vm.page++;
                loadMember();
            });

        view.more.setOnClickListener(v -> {
            PopupWindow popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutMemberActionBinding view = LayoutMemberActionBinding.inflate(getLayoutInflater());
            view.deleteFriend.setVisibility(vm.isOwnerOrAdmin ? View.VISIBLE : View.GONE);
            view.addFriend.setOnClickListener(v1 -> {
                GroupMaterialActivity.inviteIntoGroup(this, Easy.find(GroupVM.class));
                popupWindow.dismiss();
            });
            view.deleteFriend.setOnClickListener(v1 -> {
                String tag = "SuperGroupMemberActivity&deleteFriend";
                GroupMemberVM memberVM = Easy.installVM(GroupMemberVM.class,
                    tag);
                memberVM.groupId = vm.groupId;
                memberVM.setIntention(GroupMemberVM.Intention.AT);
                memberVM.setOnFinishListener(activity -> {
                    List<String> ids = new ArrayList<>();
                    for (MultipleChoice choice : memberVM.choiceList.val()) {
                        ids.add(choice.key);
                    }
                    if (null != groupVM)
                        groupVM.kickGroupMember(ids);
                    activity.finish();
                });
                startActivity(new Intent(this, SuperGroupMemberActivity.class).putExtra(Constant.K_RESULT, tag));
                popupWindow.dismiss();
            });
            //设置PopupWindow的视图内容
            popupWindow.setContentView(view.getRoot());
            //点击空白区域PopupWindow消失，这里必须先设置setBackgroundDrawable，否则点击无反应
            popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            popupWindow.setOutsideTouchable(true);

            //设置PopupWindow消失监听
            popupWindow.setOnDismissListener(() -> {

            });
            //PopupWindow在targetView下方弹出
            popupWindow.showAsDropDown(v);
        });

        vm.superGroupMembers.observe(this, v -> {
            if (v.isEmpty()) return;
            if (vm.isAt()) {
                vm.removeSelf(v);
            }
            List<ExGroupMemberInfo> exGroupMemberInfos = new ArrayList<>();
            for (GroupMembersInfo groupMembersInfo : v) {
                ExGroupMemberInfo exGroupMemberInfo = new ExGroupMemberInfo();
                exGroupMemberInfo.groupMembersInfo = groupMembersInfo;
                exGroupMemberInfos.add(exGroupMemberInfo);
            }
            adapter.setItems(exGroupMemberInfos);
        });

        vm.choiceList.observe(this, choices -> {
            int selectNum = choices.size();
            view.bottomLayout.selectNum.setText(String.format(getString(io.openim.android.ouicore.R.string.selected_tips), selectNum));
            view.bottomLayout.submit.setEnabled(selectNum > 0);

            adapter.notifyDataSetChanged();
        });
    }


    private void loadMember() {
        vm.getSuperGroupMemberList();
    }

    private void initView() {
        view.atAll.setVisibility(vm.isAt() && vm.isOwnerOrAdmin ? View.VISIBLE : View.GONE);
        view.more.setVisibility(vm.isCheck() ? View.VISIBLE : View.GONE);
        if (vm.isMultiple() || vm.isAt()) {
            view.bottomLayout.getRoot().setVisibility(View.VISIBLE);
            view.bottomLayout.more2.setVisibility(View.VISIBLE);
            view.bottomLayout.selectLy.setOnClickListener(v1 -> {
                if (vm.choiceList.val().size() > 0) {
                    startActivity(new Intent(SuperGroupMemberActivity.this,
                        SelectedMemberActivity.class));
                }
            });
        } else view.bottomLayout.getRoot().setVisibility(View.GONE);

        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<ExGroupMemberInfo, RecyclerView.ViewHolder>() {


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {
                return new ViewHol.ItemViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder,
                                   ExGroupMemberInfo data, int position) {
                ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                itemViewHo.view.select.setVisibility(vm.isAt() || vm.isMultiple() ? View.VISIBLE
                    : View.GONE);

                int index =
                    vm.choiceList.val().indexOf(new MultipleChoice(data.groupMembersInfo.getUserID()));
                boolean isChecked = index != -1;
                itemViewHo.view.select.setChecked(isChecked);
                boolean isEnabled = true;
                if (isChecked) isEnabled = vm.choiceList.val().get(index).isEnabled;
                itemViewHo.view.getRoot().setIntercept(!isEnabled);
                itemViewHo.view.getRoot().setAlpha(isEnabled ? 1f : 0.3f);

                itemViewHo.view.avatar.load(data.groupMembersInfo.getFaceURL());
                itemViewHo.view.nickName.setText(data.groupMembersInfo.getNickname());
                if (data.groupMembersInfo.getRoleLevel() == GroupRole.OWNER) {
                    itemViewHo.view.identity.setVisibility(View.VISIBLE);
                    itemViewHo.view.identity.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_8_fddfa1);
                    itemViewHo.view.identity.setText(io.openim.android.ouicore.R.string.lord);
                    itemViewHo.view.identity.setTextColor(Color.parseColor("#ffff8c00"));
                } else if (data.groupMembersInfo.getRoleLevel() == GroupRole.ADMIN) {
                    itemViewHo.view.identity.setVisibility(View.VISIBLE);
                    itemViewHo.view.identity.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_8_a2c9f8);
                    itemViewHo.view.identity.setText(io.openim.android.ouicore.R.string.administrator);
                    itemViewHo.view.identity.setTextColor(Color.parseColor("#2691ED"));
                } else itemViewHo.view.identity.setVisibility(View.GONE);


                itemViewHo.view.item.setOnClickListener(v -> {
                    if (vm.isCheck() || vm.isSingle()) {
                        MultipleChoice choice =
                            new MultipleChoice(data.groupMembersInfo.getUserID());
                        choice.name = data.groupMembersInfo.getNickname();
                        choice.icon = data.groupMembersInfo.getFaceURL();
                        vm.addChoice(choice);
                        vm.onFinish(SuperGroupMemberActivity.this);
                        return;
                    }
                    boolean isSelect = !isChecked;
                    if (isSelect) {
                        if (vm.choiceList.val().size() >= vm.maxNum) {
                            toast(String.format(getString(io.openim.android.ouicore.R.string.select_tips), vm.maxNum));
                            return;
                        }
                        MultipleChoice choice =
                            new MultipleChoice(data.groupMembersInfo.getUserID());
                        choice.name = data.groupMembersInfo.getNickname();
                        choice.icon = data.groupMembersInfo.getFaceURL();
                        vm.addChoice(choice);
                    } else {
                        vm.removeChoice(data.groupMembersInfo.getUserID());
                    }
                    vm.choiceList.update();
                    notifyItemChanged(getItems().indexOf(data));
                });
            }
        };
        view.recyclerview.setAdapter(adapter);

        update();
    }

    private void update() {
        vm.page = 0;
        loadMember();
    }

    @Override
    protected void recycle() {
        Easy.delete(GroupMemberVM.class, vmTag);
    }
}
