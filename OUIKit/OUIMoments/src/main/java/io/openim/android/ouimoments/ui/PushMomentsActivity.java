package io.openim.android.ouimoments.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.SpacesItemDecoration;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.databinding.ActivityPushMomentsBinding;
import io.openim.android.ouimoments.databinding.ItemRoundImgBinding;
import io.openim.android.ouimoments.mvp.presenter.PushMomentsVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;

public class PushMomentsActivity extends BaseActivity<PushMomentsVM, ActivityPushMomentsBinding> {

    private RecyclerViewAdapter<Object, RoundIMGItem> adapter;
    private PhotographAlbumDialog albumDialog;
    private WaitDialog waitDialog;
    GroupVM groupVM = new GroupVM();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PushMomentsVM.class, true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPushMomentsBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        listener();
        vm.init();
    }
    private ActivityResultLauncher<Intent> ruleDataLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) return;
            List<SelectDataActivity.RuleData> selectedRuleDataList =
                (List<SelectDataActivity.RuleData>) result.getData().getSerializableExtra(Constant.K_RESULT);
            view.reminderWho.setText(vm.getRuleDataNames(selectedRuleDataList));

            List<PushMomentsVM.UserOrGroup>  userOrGroups=new ArrayList<>();
            for (SelectDataActivity.RuleData ruleData : selectedRuleDataList) {
                PushMomentsVM.UserOrGroup userOrGroup=new PushMomentsVM.UserOrGroup();
                userOrGroup.userID= ruleData.id;
                userOrGroup.groupName= ruleData.name;
                userOrGroups.add(userOrGroup);
            }
            vm.param.getValue().atUserList=userOrGroups;
        });
    private ActivityResultLauncher<Intent> resultLauncher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
       if ( result.getResultCode()==RESULT_OK){
           List<SelectDataActivity.RuleData> ruleDataList =new ArrayList<>();
           if (null!=vm.selectedGroupRuleDataList)
           ruleDataList.addAll(vm.selectedGroupRuleDataList);
           if (null!=vm.selectedUserRuleDataList)
           ruleDataList.addAll(vm.selectedUserRuleDataList);
           view.whoSee.setText(vm.getRuleDataNames(ruleDataList));
       }
    });
    private OnDedrepClickListener selectUserClick = new OnDedrepClickListener() {
        @Override
        public void click(View v) {
            if (groupVM.exUserInfo.getValue().isEmpty()) {
                waitDialog.show();
                groupVM.getAllFriend();
            } else jumpSelectUser(groupVM.exUserInfo.getValue());
        }
    };

    private void listener() {
        groupVM.exUserInfo.observe(this, exUserInfos -> {
            waitDialog.dismiss();
            jumpSelectUser(exUserInfos);
        });

        view.whoSeeLy.setOnClickListener(v -> {
            resultLauncher.launch(new Intent(this, WhoSeeActivity.class));
        });
        view.reminderWhoLy.setOnClickListener(selectUserClick);

        vm.selectMedia.observe(this, objects -> {
            adapter.setItems(objects);
        });
        view.content
            .addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    view.restriction.setText(s.length() + "/500");
                }
            });
    }

    private void jumpSelectUser(List<ExUserInfo> exUserInfo) {
        if (exUserInfo.isEmpty())return;
        List<SelectDataActivity.RuleData> ruleDataList = vm.buildUserRuleData(exUserInfo);
        ruleDataLauncher.launch(new Intent(this, SelectDataActivity.class).putExtra(Constant.K_NAME, getString(io.openim.android.ouicore.R.string.select_user)).putExtra(Constant.K_RESULT, (Serializable) ruleDataList).putExtra(Constant.K_FROM,
            false).putExtra(Constant.K_SIZE, 20));
    }

    private void initView() {
        waitDialog = new WaitDialog(this);
        albumDialog = new PhotographAlbumDialog(PushMomentsActivity.this);
        view.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        view.recyclerView.setAdapter(adapter =
            new RecyclerViewAdapter<Object, RoundIMGItem>(RoundIMGItem.class) {

                @Override
                public void onBindView(@NonNull RoundIMGItem holder, Object data, int position) {
                    if (data instanceof Integer) {
                        //表示添加按钮
                        holder.view.roundImageView.setOnClickListener(v -> {
                            albumDialog.show();
                            albumDialog.setMaxSelectable(9);
                            albumDialog.setToCrop(false);
                            albumDialog.setOnSelectResultListener(paths -> {
                                albumDialog.dismiss();
                                for (String path : paths) {
                                    vm.addRes(path);
                                }
                            });
                        });
                        holder.view.delete.setVisibility(View.INVISIBLE);
                    } else {
                        holder.view.delete.setVisibility(View.VISIBLE);
                        holder.view.delete.setOnClickListener(v -> {
                            vm.removeRes(position);
                        });
                        holder.view.roundImageView.setOnClickListener(v -> {
                            List<String> photoUrls = new ArrayList<>();
                            for (Object o : vm.selectMedia.getValue()) {
                                if (o instanceof String) photoUrls.add((String) o);
                            }
                            ImagePagerActivity.startImagePagerActivity(PushMomentsActivity.this,
                                photoUrls, position, null);
                        });
                    }
                    Glide.with(PushMomentsActivity.this).load(data).centerInside().into(holder.view.roundImageView);
                }
            });
        adapter.setItems(vm.selectMedia.getValue());
    }

    public static class RoundIMGItem extends RecyclerView.ViewHolder {

        private final ItemRoundImgBinding view;

        public RoundIMGItem(@NonNull View itemView) {
            super(ItemRoundImgBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = ItemRoundImgBinding.bind(this.itemView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }

}
