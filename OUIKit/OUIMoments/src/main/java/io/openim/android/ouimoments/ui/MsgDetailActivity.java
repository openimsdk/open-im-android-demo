package io.openim.android.ouimoments.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.adapter.viewholder.MsgDetailViewHolder;
import io.openim.android.ouimoments.bean.EXWorkMomentsInfo;
import io.openim.android.ouimoments.databinding.ActivityMsgDetailBinding;
import io.openim.android.ouimoments.mvp.contract.CircleContract;
import io.openim.android.ouimoments.mvp.presenter.MsgDetailVM;
import io.openim.android.ouimoments.utils.DatasUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.WorkMomentsInfo;

public class MsgDetailActivity extends BaseActivity<MsgDetailVM, ActivityMsgDetailBinding> {

    private RecyclerViewAdapter<EXWorkMomentsInfo, MsgDetailViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(MsgDetailVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMsgDetailBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        listener();
    }

    private void listener() {
        vm.workMomentsInfo.observe(this, workMomentsInfos -> {
            adapter.setItems(workMomentsInfos);
        });
        view.clear.setOnClickListener(v -> {
            CommonDialog commonDialog =new CommonDialog(this).atShow();
            commonDialog.getMainView().tips.setText("确认清空消息？");
            commonDialog.getMainView().cancel.setOnClickListener(v2 -> {
                commonDialog.dismiss();
            });
            commonDialog.getMainView().confirm.setOnClickListener(v2 -> {
                commonDialog.dismiss();
                vm.clearMsg();
            });

        });
    }

    private void initView() {
        vm.getWorkMomentsNotification();

        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter=new RecyclerViewAdapter<EXWorkMomentsInfo,
            MsgDetailViewHolder>(MsgDetailViewHolder.class) {

            @Override
            public void onBindView(@NonNull MsgDetailViewHolder holder, EXWorkMomentsInfo data,
                                   int position) {
                holder.view.getRoot().setOnClickListener(v -> {
                    startActivity(new Intent(MsgDetailActivity.this,MomentsDetailActivity.class).putExtra(Constant.K_ID,
                        data.workMomentsInfo.getWorkMomentID()));
                });
                holder.view.avatar.load(data.workMomentsInfo
                    .getFaceURL());
                holder.view.nickName.setText(data.workMomentsInfo.getUserName());
                holder.view.star.setVisibility(View.GONE);
                if (data.workMomentsInfo.getNotificationMsgType() == 0) {
                    if (TextUtils.isEmpty(data.workMomentsInfo.getReplyUserID()))
                        holder.view.action.setText(io.openim.android.ouicore.R.string.star_tips2 + ":" + data.workMomentsInfo.getContent());
                    else {
                        String targetUser =
                            DatasUtil.curUser.getId().equals(data.workMomentsInfo.getReplyUserID()) ?
                                DatasUtil.curUser.getName() :
                                data.workMomentsInfo.getReplyUserName();
                        Common.stringBindForegroundColorSpan(holder.view.action,
                            getString(io.openim.android.ouicore.R.string.reply)
                                + targetUser + ":" + data.workMomentsInfo.getContent(),
                            targetUser);
                    }
                }
                holder.view.time.setText(TimeUtil.getTime(data.workMomentsInfo.getCreateTime() * 1000,
                    TimeUtil.monthTimeFormat));
                if (data.workMomentsInfo.getNotificationMsgType() == 1) {
                    holder.view.star.setVisibility(View.VISIBLE);
                    holder.view.action.setText(io.openim.android.ouicore.R.string.star_tips);
                }
                if (data.workMomentsInfo.getNotificationMsgType() == 2) {
                    holder.view.action.setText(io.openim.android.ouicore.R.string.about_you);
                }
                if (data.contentBean.metas.isEmpty()){
                    holder.view.content.setVisibility(View.VISIBLE);
                    holder.view.media.setVisibility(View.GONE);
                    holder.view.content.setText(data.contentBean.text);
                }else {
                    holder.view.media.setVisibility(View.VISIBLE);
                    holder.view.content.setVisibility(View.GONE);
                    Glide.with(MsgDetailActivity.this).load(data.contentBean.metas.get(0).thumb)
                        .into(holder.view.img);
                    holder.view.play.setVisibility(data.contentBean.type != 0 ? View.VISIBLE :
                        View.GONE);
                }
            }
        });
    }


}
