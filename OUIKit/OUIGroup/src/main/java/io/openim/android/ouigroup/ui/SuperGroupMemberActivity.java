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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.databinding.LayoutMemberActionBinding;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouigroup.databinding.ActivitySuperGroupMemberBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Group.SUPER_GROUP_MEMBER)
public class SuperGroupMemberActivity extends BaseActivity<GroupVM, ActivitySuperGroupMemberBinding> {
    private RecyclerViewAdapter adapter;
    //转让群主权限
    private boolean isTransferPermission;
    //选择群成员
    private boolean isSelectGroupMember;
    private int maxNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySuperGroupMemberBinding.inflate(getLayoutInflater()));
        sink();

        init();
        initView();
        listener();
    }

    private ActivityResultLauncher<Intent> searchFriendLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        try {
            String uid = result.getData().getStringExtra(Constant.K_ID);
            ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                .withString(Constant.K_ID, uid)
                .withString(Constant.K_GROUP_ID, vm.groupId)
                .navigation();
        } catch (Exception ignored) {

        }
    });

    void init() {
        if (vm.superGroupMembers.getValue().size() >
            Constant.SUPER_GROUP_LIMIT) {
            vm.superGroupMembers.getValue().clear();
            vm.page = 0;
            vm.pageSize = 20;
        }
        isTransferPermission = getIntent().getBooleanExtra(Constant.K_FROM, false);
        isSelectGroupMember = getIntent().getBooleanExtra("isSelectMember", false);
        maxNum = getIntent().getIntExtra("maxNum", 9);
    }

    private void listener() {
        view.searchView.setOnClickListener(v -> {
            Postcard postcard = ARouter.getInstance().build(Routes.Contact.SEARCH_FRIENDS);
            LogisticsCenter.completion(postcard);
            searchFriendLauncher.launch(new Intent(this, postcard.getDestination())
                .putExtra(Constant.K_GROUP_ID, vm.groupId));
        });
        view.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerview.getLayoutManager();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == adapter.getItems().size() - 1
                    && adapter.getItems().size() >= vm.pageSize) {
                    vm.page++;
                    loadMember();
                }
            }
        });
        view.more.setOnClickListener(v -> {
            PopupWindow popupWindow = new PopupWindow(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutMemberActionBinding view = LayoutMemberActionBinding.inflate(getLayoutInflater());
            view.deleteFriend.setVisibility(vm.isOwner() ? View.VISIBLE : View.GONE);
            view.addFriend.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, InitiateGroupActivity.class)
                    .putExtra(InitiateGroupActivity.IS_INVITE_TO_GROUP, true));
            });
            view.deleteFriend.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, InitiateGroupActivity.class)
                    .putExtra(InitiateGroupActivity.IS_REMOVE_GROUP, true));
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
            adapter.notifyItemRangeInserted(vm.superGroupMembers.getValue().size() - vm.pageSize,
                vm.superGroupMembers.getValue().size());
        });
    }

    private void loadMember() {
        vm.getSuperGroupMemberList();
    }

    private void initView() {
        view.more.setVisibility(isTransferPermission ? View.GONE : View.VISIBLE);
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<ExGroupMemberInfo, RecyclerView.ViewHolder>() {


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHol.ItemViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, ExGroupMemberInfo data, int position) {
                ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                itemViewHo.view.select.setVisibility(isSelectGroupMember ? View.VISIBLE :View.GONE);
                itemViewHo.view.avatar.load(data.groupMembersInfo.getFaceURL());
                itemViewHo.view.nickName.setText(data.groupMembersInfo.getNickname());
                if (data.groupMembersInfo.getRoleLevel() == 2) {
                    itemViewHo.view.identity.setVisibility(View.VISIBLE);
                    itemViewHo.view.identity.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_8_fddfa1);
                    itemViewHo.view.identity.setText(io.openim.android.ouicore.R.string.lord);
                    itemViewHo.view.identity.setTextColor(Color.parseColor("#ffff8c00"));
                } else if (data.groupMembersInfo.getRoleLevel() == 3) {
                    itemViewHo.view.identity.setVisibility(View.VISIBLE);
                    itemViewHo.view.identity.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_8_a2c9f8);
                    itemViewHo.view.identity.setText(io.openim.android.ouicore.R.string.lord);
                    itemViewHo.view.identity.setTextColor(Color.parseColor("#2691ED"));
                } else
                    itemViewHo.view.identity.setVisibility(View.GONE);



                itemViewHo.view.getRoot().setOnClickListener(v -> {
                    if (isTransferPermission) {
                        if (data.groupMembersInfo.getRoleLevel() == 2)
                            toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.repeat_group_manager));
                        else {
                            CommonDialog commonDialog = new CommonDialog(SuperGroupMemberActivity.this);
                            commonDialog.getMainView().tips
                                .setText(String.format(BaseApp.inst().getString(io.openim.
                                    android.ouicore.R.string.transfer_permission), data.groupMembersInfo.getNickname()));
                            commonDialog.getMainView().cancel.setOnClickListener(v2 -> {
                                commonDialog.dismiss();
                            });
                            commonDialog.getMainView().confirm.setOnClickListener(v2 -> {
                                commonDialog.dismiss();
                                vm.transferGroupOwner(data.groupMembersInfo.getUserID(), data1 -> {
                                    toast(getString(io.openim.android.ouicore.R.string.transfer_succ));
                                    finish();
                                });
                            });
                            commonDialog.show();
                        }
                    } else {
                        ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                            .withString(Constant.K_ID, data.groupMembersInfo.getUserID())
                            .withString(Constant.K_GROUP_ID, vm.groupId)
                            .navigation();
                    }
                });


            }
        };
        adapter.setItems(vm.superGroupMembers.getValue());
        view.recyclerview.setAdapter(adapter);

        loadMember();
    }

}
