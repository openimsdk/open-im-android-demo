package io.openim.android.ouimoments.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouimoments.adapter.viewholder.MsgDetailViewHolder;
import io.openim.android.ouimoments.bean.Comment;
import io.openim.android.ouimoments.bean.MomentsUser;
import io.openim.android.ouimoments.bean.WorkMoments;
import io.openim.android.ouimoments.databinding.ActivityMsgDetailBinding;
import io.openim.android.ouimoments.mvp.presenter.MsgDetailVM;
import io.openim.android.ouimoments.utils.DatasUtil;

public class MsgDetailActivity extends BaseActivity<MsgDetailVM, ActivityMsgDetailBinding> {

    private RecyclerViewAdapter<WorkMoments, MsgDetailViewHolder> adapter;

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
        vm.workMomentsInfo.observe(this, workMoments -> {
            adapter.setItems(workMoments);
        });
        view.clear.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this).atShow();
            commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.clear_msg_sure);
            commonDialog.getMainView().cancel.setOnClickListener(v2 -> {
                commonDialog.dismiss();
            });
            commonDialog.getMainView().confirm.setOnClickListener(v2 -> {
                commonDialog.dismiss();
                vm.clearMsg(MsgDetailVM.msg_list);
            });

        });
    }

    private void initView() {
        vm.getWorkMomentsNotification();

        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<WorkMoments,
            MsgDetailViewHolder>(MsgDetailViewHolder.class) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindView(@NonNull MsgDetailViewHolder holder, WorkMoments data,
                                   int position) {
                holder.view.getRoot().setOnClickListener(v -> {
                    startActivity(new Intent(MsgDetailActivity.this, MomentsDetailActivity.class)
                        .putExtra(Constant.K_ID,
                            data.workMomentID));
                });
                holder.view.star.setVisibility(View.GONE);
                String nickName = "", faceURL = "";
                if (data.type == MsgDetailVM.WorkMomentLogTypeComment) {
                    Comment comment = data.comments.get(0);
                    nickName = comment.nickname;
                    faceURL = comment.faceURL;
                    if (TextUtils.isEmpty(comment.replyUserID)) {
                        holder.view.action.setText(getText(io.openim.android.ouicore.R.string.star_tips2)
                            + data.nickname + ":" + comment.content);
                    } else {
                        String targetUser = comment.replyNickname;
                        holder.view.action.setText(getString(io.openim.android.ouicore.R.string.reply)
                            + targetUser + ":" + comment.content);
                    }
                }
                holder.view.time.setText(TimeUtil.getTime(data.createTime,
                    TimeUtil.monthTimeFormat));
                if (data.type == MsgDetailVM.WorkMomentLogTypeLike) {
                    holder.view.star.setVisibility(View.VISIBLE);
                    MomentsUser user = data.likeUsers.get(0);
                    nickName = user.nickname;
                    faceURL = user.faceURL;

                    String tips =
                        getString(io.openim.android.ouicore.R.string.star_tips) + data.nickname;
                    holder.view.action.setText(tips);
                }
                if (data.type == MsgDetailVM.WorkMomentLogTypeAt) {
                    nickName = data.nickname;
                    faceURL = data.faceURL;
                    holder.view.action.setText(getString(io.openim.android.ouicore.R.string.about_you)
                        + DatasUtil.curUser.getName());
                }
                holder.view.avatar.load(faceURL);
                holder.view.nickName.setText(nickName);

                if (data.content.metas.isEmpty()) {
                    holder.view.content.setVisibility(View.VISIBLE);
                    holder.view.media.setVisibility(View.GONE);
                    holder.view.content.setText(data.content.text);
                } else {
                    holder.view.media.setVisibility(View.VISIBLE);
                    holder.view.content.setVisibility(View.GONE);
                    String pictureUrl = data.content.metas.get(0).thumb;
                    if (TextUtils.isEmpty(pictureUrl))
                        pictureUrl = data.content.metas.get(0).original;
                    Glide.with(MsgDetailActivity.this).load(pictureUrl)
                        .into(holder.view.img);
                    holder.view.play.setVisibility(data.content.type != 0 ? View.VISIBLE :
                        View.GONE);
                }
            }
        });
    }


}
