package io.openim.android.ouimoments.ui;


import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouimoments.bean.MomentsUser;
import io.openim.android.ouimoments.databinding.ActivityPartSeeBinding;

public class PartSeeActivity extends BaseActivity<BaseViewModel, ActivityPartSeeBinding> {

    private RecyclerViewAdapter<MomentsUser, ViewHol.ItemViewHo> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPartSeeBinding.inflate(getLayoutInflater()));
        initView();
        String title = getIntent().getStringExtra(Constants.K_NAME);
        List<MomentsUser> momentsUsers =
            (List<MomentsUser>) getIntent().getSerializableExtra(Constants.K_RESULT);
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
                holder.view.nickName.setText(data.nickname);
                holder.view.select.setVisibility(View.GONE);

                holder.view.getRoot().setOnClickListener(v -> {
                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                        .withString(Constants.K_ID, data.userID)
                        .navigation();
                });
            }
        };
        view.recyclerView.setAdapter(adapter);

    }
}
