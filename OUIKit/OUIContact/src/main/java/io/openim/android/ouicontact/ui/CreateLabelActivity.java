package io.openim.android.ouicontact.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson2.JSONObject;
import com.xiaofeng.flowlayoutmanager.Alignment;
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivityCreateLabelBinding;
import io.openim.android.ouicontact.vm.LabelVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.models.FriendInfo;

public class CreateLabelActivity extends BaseActivity<LabelVM, ActivityCreateLabelBinding> {

    private List<FriendInfo> friendInfos;
    private RecyclerViewAdapter<FriendInfo, ViewHol.LabelMemberItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(LabelVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityCreateLabelBinding.inflate(getLayoutInflater()));
        sink();
        init();
        listener();
    }

    private void listener() {
        view.addMember.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Group.CREATE_GROUP).withString(Constant.K_NAME,
                getString(io.openim.android.ouicore.R.string.selete_member)).withBoolean(Constant.IS_SELECT_FRIEND, true).navigation(this, Constant.Event.CALLING_REQUEST_CODE);
        });
        view.submit.setOnClickListener(v -> {
            String title;
            if (TextUtils.isEmpty(title=view.name.getText().toString())){
                toast(getString(io.openim.android.ouicore.R.string.create_label_tips2));
                return;
            }
            List<String> ids=new ArrayList<>();
            for (FriendInfo friendInfo : friendInfos) {
                ids.add(friendInfo.getUserID());
            }
            vm.createTag(title, ids, data -> {
                toast(getString(io.openim.android.ouicore.R.string.create_succ));
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    void init() {
        FlowLayoutManager flowLayoutManager = new FlowLayoutManager();
        flowLayoutManager.maxItemsPerLine(4);
        flowLayoutManager.setAutoMeasureEnabled(true);
        flowLayoutManager.setAlignment(Alignment.LEFT);

        view.recyclerView.setLayoutManager(flowLayoutManager);

        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<FriendInfo,
            ViewHol.LabelMemberItem>(ViewHol.LabelMemberItem.class) {

            @Override
            public void onBindView(@NonNull ViewHol.LabelMemberItem holder, FriendInfo data,
                                   int position) {
                holder.view.name.setText(data.getNickname());
                holder.view.layout.setOnClickListener(v -> {
                    int index=getItems().indexOf(data);

                    getItems().remove(index);
                    runOnUiThread(() -> adapter.notifyItemRemoved(index));
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        String fds = data.getStringExtra(Constant.K_RESULT);
        Type listType = new GsonHel.ParameterizedTypeImpl(List.class,
            new Class[]{FriendInfo.class});
        friendInfos = JSONObject.parseObject(fds, listType);
        adapter.setItems(friendInfos);
    }
}
