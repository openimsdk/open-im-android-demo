package io.openim.android.ouimoments.ui;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouimoments.bean.MomentsUser;
import io.openim.android.ouimoments.databinding.ActivityPartSeeBinding;
import io.openim.android.sdk.models.FriendInfo;

public class PartSeeActivity extends BaseActivity<BaseViewModel, ActivityPartSeeBinding> {

    private RecyclerViewAdapter<MomentsUser, ViewHol.ItemViewHo> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPartSeeBinding.inflate(getLayoutInflater()));
        initView();
        String title = getIntent().getStringExtra(Constant.K_NAME);
        List<MomentsUser> momentsUsers =
            (List<MomentsUser>) getIntent().getSerializableExtra(Constant.K_RESULT);
        view.title.setText(title);
        if (null != momentsUsers && !momentsUsers.isEmpty())
            adapter.setItems(momentsUsers);
    }

    private void initView() {
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter =
            new RecyclerViewAdapter<MomentsUser, ViewHol.ItemViewHo>(ViewHol.ItemViewHo.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ItemViewHo holder, MomentsUser data,
                                   int position) {
                holder.view.avatar.load(data.faceURL);
                holder.view.nickName.setText(data.userName);
                holder.view.select.setVisibility(View.GONE);
            }
        };
        view.recyclerView.setAdapter(adapter);

    }
}
