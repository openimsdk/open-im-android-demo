package io.openim.android.ouimoments.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.CustomEmoji;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.AvatarImage;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.adapter.viewholder.TargetMomentsViewHolder;
import io.openim.android.ouimoments.mvp.presenter.MsgDetailVM;
import io.openim.android.ouimoments.ui.MomentsDetailActivity;
import io.openim.android.ouimoments.ui.MsgDetailActivity;
import io.openim.android.ouimoments.ui.PartSeeActivity;
import io.openim.android.ouimoments.ui.ToUserMomentsActivity;
import io.openim.android.ouimoments.ui.ImagePagerActivity;
import io.openim.android.ouimoments.adapter.viewholder.CircleViewHolder;
import io.openim.android.ouimoments.adapter.viewholder.ImageViewHolder;
import io.openim.android.ouimoments.adapter.viewholder.URLViewHolder;
import io.openim.android.ouimoments.adapter.viewholder.VideoViewHolder;
import io.openim.android.ouimoments.bean.ActionItem;
import io.openim.android.ouimoments.bean.CircleItem;
import io.openim.android.ouimoments.bean.CommentConfig;
import io.openim.android.ouimoments.bean.CommentItem;
import io.openim.android.ouimoments.bean.FavortItem;
import io.openim.android.ouimoments.bean.PhotoInfo;
import io.openim.android.ouimoments.mvp.presenter.CirclePresenter;
import io.openim.android.ouimoments.utils.DatasUtil;
import io.openim.android.ouimoments.utils.UrlUtils;
import io.openim.android.ouimoments.widgets.CircleVideoView;
import io.openim.android.ouimoments.widgets.CommentListView;
import io.openim.android.ouimoments.widgets.ExpandTextView;
import io.openim.android.ouimoments.widgets.MultiImageView;
import io.openim.android.ouimoments.widgets.PraiseListView;
import io.openim.android.ouimoments.widgets.SnsPopupWindow;
import io.openim.android.ouimoments.widgets.dialog.CommentDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;

public class CircleAdapter extends BaseRecycleViewAdapter {

    public final static int TYPE_HEAD = 0;

    private static final int STATE_IDLE = 0;
    private static final int STATE_ACTIVED = 1;
    private static final int STATE_DEACTIVED = 2;
    private int videoState = STATE_IDLE;
    public int HEADVIEW_SIZE = 1;

    int curPlayIndex = -1;

    private CirclePresenter presenter;
    private Context context;
    public String lastYear,lastMd;

    public void setCirclePresenter(CirclePresenter presenter) {
        this.presenter = presenter;
    }

    public CircleAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEAD;
        }

        int itemType = 0;
        CircleItem item = (CircleItem) datas.get(position - HEADVIEW_SIZE);
        if (CircleItem.TYPE_URL.equals(item.getType())) {
            itemType = CircleViewHolder.TYPE_URL;
        } else if (CircleItem.TYPE_IMG.equals(item.getType())) {
            itemType = CircleViewHolder.TYPE_IMAGE;
        } else if (CircleItem.TYPE_VIDEO.equals(item.getType())) {
            itemType = CircleViewHolder.TYPE_VIDEO;
        }
        return itemType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == TYPE_HEAD) {
            View headView = LayoutInflater.from(parent.getContext()).inflate(R.layout.head_circle
                , parent, false);
            viewHolder = new HeaderViewHolder(headView);
        } else {
            View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_circle_item,
                    parent, false);

            if (presenter.isSpecifiedUser()) {
                //创建指定用户的朋友圈视图
                return new TargetMomentsViewHolder(view);
            }

            if (viewType == CircleViewHolder.TYPE_URL) {
                viewHolder = new URLViewHolder(view);
            } else if (viewType == CircleViewHolder.TYPE_IMAGE) {
                viewHolder = new ImageViewHolder(view);
            } else if (viewType == CircleViewHolder.TYPE_VIDEO) {
                viewHolder = new VideoViewHolder(view);
            }
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (getItemViewType(position) == TYPE_HEAD) {
            try {
                HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
                if (presenter.isSpecifiedUser()) {
                    holder.headIv.load(presenter.user.getHeadUrl());
                    if (presenter.user.getId().equals(BaseApp.inst().loginCertificate.userID))
                        holder.nameTv.setText(BaseApp.inst().loginCertificate.nickname);
                    else holder.nameTv.setText(presenter.user.getName());
                } else {
                    holder.headIv.load(BaseApp.inst().loginCertificate.faceURL);
                    holder.nameTv.setText(BaseApp.inst().loginCertificate.nickname);
                    holder.headIv.setOnClickListener(v -> {
                        context.startActivity(new Intent(context, ToUserMomentsActivity.class).putExtra(Constant.K_RESULT, DatasUtil.curUser));
                    });

                    holder.newMsgTips.setVisibility(TextUtils.isEmpty(presenter.unReadCount) ?
                        View.GONE : View.VISIBLE);
                    holder.newMsgTips.setText(String.format(context.getString(io.openim.android.ouicore.R.string.new_msg_tips), presenter.unReadCount));
                    holder.newMsgTips.setOnClickListener(v -> {
                        presenter.unReadCount = null;
                        holder.newMsgTips.setVisibility(View.GONE);
                        //TODO JUMP
                        new MsgDetailVM().clearMsg(MsgDetailVM.clear_unread_num);
                        context.startActivity(new Intent(context, MsgDetailActivity.class));
                    });
                }

            } catch (Exception ignored) {
            }
        } else {
            final int circlePosition = position - HEADVIEW_SIZE;
            final CircleItem circleItem = (CircleItem) datas.get(circlePosition);
            if (presenter.isSpecifiedUser()) {
                //绑定指定用户的朋友圈视图的数据
                bindTargetMomentsView((TargetMomentsViewHolder) viewHolder, circlePosition,
                    circleItem);
                return;
            }

            final CircleViewHolder holder = (CircleViewHolder) viewHolder;
            final String circleId = circleItem.getId();
            String name = circleItem.getUser().getName();
            String headImg = circleItem.getUser().getHeadUrl();
            final String content = circleItem.getContent();
            String createTime = circleItem.getCreateTime();
            final List<FavortItem> favortDatas = circleItem.getFavorters();
            final List<CommentItem> commentsDatas = circleItem.getComments();
            boolean hasFavort = circleItem.hasFavort();
            boolean hasComment = circleItem.hasComment();

            holder.headIv.load(headImg);
            if (!presenter.isSpecifiedUser()) holder.headIv.setOnClickListener(v -> {
                context.startActivity(new Intent(context, ToUserMomentsActivity.class).putExtra(Constant.K_RESULT, circleItem.getUser()));
            });
            holder.nameTv.setText(name);
            holder.timeTv.setText(createTime);

            if (!TextUtils.isEmpty(content)) {
                holder.contentTv.setExpand(circleItem.isExpand());
                holder.contentTv.setExpandStatusListener(new ExpandTextView.ExpandStatusListener() {
                    @Override
                    public void statusChange(boolean isExpand) {
                        circleItem.setExpand(isExpand);
                    }
                });

                holder.contentTv.setText(UrlUtils.formatUrlString(content));
            }
            holder.contentTv.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
            holder.authorityLy.setVisibility(View.GONE);
            if (DatasUtil.curUser.getId().equals(circleItem.getUser().getId())) {
                holder.deleteBtn.setVisibility(View.VISIBLE);
                if (circleItem.getPermission() == 1) {
                    holder.authorityLy.setVisibility(View.VISIBLE);
                    holder.authorityLy.setOnClickListener(null);
                    holder.authorityIv.setImageResource(R.mipmap.ic_m_lock);
                    holder.authorityTv.setText(io.openim.android.ouicore.R.string.str_private_tips);
                }
                if (circleItem.getPermission() == 2 || circleItem.getPermission() == 3) {
                    holder.authorityLy.setVisibility(View.VISIBLE);
                    holder.authorityIv.setImageResource(R.mipmap.ic_m_friends);
                    holder.authorityTv.setText(io.openim.android.ouicore.R.string.part_see_tips2);
                    holder.authorityLy.setOnClickListener(v -> {
                        context.startActivity(new Intent(context, PartSeeActivity.class).putExtra(Constant.K_NAME, circleItem.getPermission() == 2 ? this.context.getString(io.openim.android.ouicore.R.string.part_see_tips2) : this.context.getString(io.openim.android.ouicore.R.string.who_invisible)).putExtra(Constant.K_RESULT, (Serializable) circleItem.getPermissionUsers()));
                    });
                }
            } else {
                holder.authorityLy.setVisibility(View.GONE);
                holder.deleteBtn.setVisibility(View.GONE);
            }
            holder.aboutWhoTv.setVisibility(TextUtils.isEmpty(circleItem.getAtUsers()) ?
                View.GONE : View.VISIBLE);
            holder.aboutWhoTv.setText(String.format(this.context.getString(io.openim.android.ouicore.R.string.about_who), circleItem.getAtUsers()));

            holder.deleteBtn.setOnClickListener(v -> {
                CommonDialog commonDialog = new CommonDialog(context).atShow();
                commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.delete_moments_tips);
                commonDialog.getMainView().cancel.setOnClickListener(v1 -> commonDialog.dismiss());
                commonDialog.getMainView().confirm.setOnClickListener(v1 -> {
                    //删除
                    if (presenter != null) {
                        presenter.deleteCircle(circleId);
                        commonDialog.dismiss();
                    }
                });
            });
            if (hasFavort || hasComment) {
                if (hasFavort) {//处理点赞列表
                    holder.praiseListView.setOnItemClickListener(new PraiseListView.OnItemClickListener() {
                        @Override
                        public void onClick(int position) {
                            String userName = favortDatas.get(position).getUser().getName();
                            String userId = favortDatas.get(position).getUser().getId();

                            ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constant.K_ID, userId).navigation();
                        }
                    });
                    holder.praiseListView.setDatas(favortDatas);
                    holder.praiseListView.setVisibility(View.VISIBLE);
                } else {
                    holder.praiseListView.setVisibility(View.GONE);
                }

                if (hasComment) {//处理评论列表
                    holder.commentList.setOnItemClickListener(new CommentListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(int commentPosition) {
                            CommentItem commentItem = commentsDatas.get(commentPosition);
                            if (DatasUtil.curUser.getId().equals(commentItem.getUser().getId())) {//复制或者删除自己的评论
                                CommentDialog dialog = new CommentDialog(context, presenter,
                                    commentItem, circleItem.getId(), circlePosition);
                                dialog.show();
                            } else {//回复别人的评论
                                if (presenter != null) {
                                    CommentConfig config = new CommentConfig();
                                    config.momentID = circleItem.getId();
                                    config.circlePosition = circlePosition;
                                    config.commentPosition = commentPosition;
                                    config.commentType = CommentConfig.Type.REPLY;
                                    config.replyUser = commentItem.getUser();
                                    presenter.showEditTextBody(config);
                                }
                            }
                        }
                    });
                    holder.commentList.setOnItemLongClickListener(new CommentListView.OnItemLongClickListener() {
                        @Override
                        public void onItemLongClick(int commentPosition) {
                            //长按进行复制或者删除
                            CommentItem commentItem = commentsDatas.get(commentPosition);
                            CommentDialog dialog = new CommentDialog(context, presenter,
                                commentItem, circleItem.getId(), circlePosition);
                            dialog.show();
                        }
                    });
                    holder.commentList.setDatas(commentsDatas);
                    holder.commentList.setVisibility(View.VISIBLE);

                } else {
                    holder.commentList.setVisibility(View.GONE);
                }
                holder.digCommentBody.setVisibility(View.VISIBLE);
            } else {
                holder.digCommentBody.setVisibility(View.GONE);
            }

            holder.digLine.setVisibility(hasFavort && hasComment ? View.VISIBLE : View.GONE);

            final SnsPopupWindow snsPopupWindow = holder.snsPopupWindow;
            //判断是否已点赞
            String curUserFavortId =
                circleItem.getCurUserFavortId(BaseApp.inst().loginCertificate.userID);
            if (!TextUtils.isEmpty(curUserFavortId)) {
                snsPopupWindow.getmActionItems().get(0).mTitle =
                    context.getString(io.openim.android.ouicore.R.string.cancel);
            } else {
                snsPopupWindow.getmActionItems().get(0).mTitle =
                    context.getString(io.openim.android.ouicore.R.string.star);
            }
            snsPopupWindow.update();
            snsPopupWindow.setmItemClickListener(new PopupItemClickListener(circlePosition,
                circleItem, circleItem.getId()));
            holder.snsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //弹出popupwindow
                    snsPopupWindow.showPopupWindow(view);
                }
            });

            holder.urlTipTv.setVisibility(View.GONE);
            switch (holder.viewType) {
                case CircleViewHolder.TYPE_URL:// 处理链接动态的链接内容和和图片
                    if (holder instanceof URLViewHolder) {
                        String linkImg = circleItem.getLinkImg();
                        String linkTitle = circleItem.getLinkTitle();
                        Glide.with(context).load(linkImg).into(((URLViewHolder) holder).urlImageIv);
                        ((URLViewHolder) holder).urlContentTv.setText(linkTitle);
                        ((URLViewHolder) holder).urlBody.setVisibility(View.VISIBLE);
                        ((URLViewHolder) holder).urlTipTv.setVisibility(View.VISIBLE);
                    }

                    break;
                case CircleViewHolder.TYPE_IMAGE:// 处理图片
                    if (holder instanceof ImageViewHolder) {
                        final List<PhotoInfo> photos = circleItem.getPhotos();
                        if (photos != null && photos.size() > 0) {
                            ((ImageViewHolder) holder).multiImageView.setVisibility(View.VISIBLE);
                            ((ImageViewHolder) holder).multiImageView.setList(photos);
                            ((ImageViewHolder) holder).multiImageView.setOnItemClickListener(new MultiImageView.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    //imagesize是作为loading时的图片size
                                    ImagePagerActivity.ImageSize imageSize =
                                        new ImagePagerActivity.ImageSize(view.getMeasuredWidth(),
                                            view.getMeasuredHeight());

                                    List<String> photoUrls = new ArrayList<String>();
                                    for (PhotoInfo photoInfo : photos) {
                                        photoUrls.add(photoInfo.url);
                                    }
                                    ImagePagerActivity.startImagePagerActivity(context, photoUrls
                                        , position, imageSize);


                                }
                            });
                        } else {
                            ((ImageViewHolder) holder).multiImageView.setVisibility(View.GONE);
                        }
                    }

                    break;
                case CircleViewHolder.TYPE_VIDEO:
                    if (holder instanceof VideoViewHolder) {
                        ((VideoViewHolder) holder).videoView.setVideoUrl(circleItem.getVideoUrl());
                        ((VideoViewHolder) holder).videoView.setVideoImgUrl(circleItem.getVideoImgUrl());//视频封面图片
                        ((VideoViewHolder) holder).videoView.setPostion(position);
                        ((VideoViewHolder) holder).videoView.setOnPlayClickListener(new CircleVideoView.OnPlayClickListener() {
                            @Override
                            public void onPlayClick(int pos) {
                                curPlayIndex = pos;
                            }
                        });
                    }

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 绑定指定用户的朋友圈视图的数据
     */
    private void bindTargetMomentsView(TargetMomentsViewHolder holder, int circlePosition,
                                       CircleItem circleItem) {
        holder.view.getRoot().setOnClickListener(v -> {
            context.startActivity(new Intent(context, MomentsDetailActivity.class).putExtra(Constant.K_ID, circleItem.getId()));
        });

        int index = circlePosition - 1;
        if (index < 0) index = 0;
        boolean isSameDate =
            index != 0 && ((CircleItem) datas.get(index)).getCreateTime().equals(circleItem.getCreateTime());
        int i10 = Common.dp2px(10);
        int i30 = Common.dp2px(30);
        int i15 = Common.dp2px(15);
        if (index == 0) {
            holder.view.getRoot().setPadding(i10, i30, i10, 0);
        } else {
            holder.view.getRoot().setPadding(i10, isSameDate ? i15 : i30, i10,
                circlePosition == datas.size() - 1 ? i30 : 0);
        }
        holder.view.yearLy.setVisibility(View.GONE);
        String year = TimeUtil.getTime(circleItem.getCreateTimeL(), "yyyy");
        String localDate = TimeUtil.getTime(System.currentTimeMillis(), "yyyy");
        if (!localDate.equals(year) && !year.equals(lastYear)) {
            holder.view.yearLy.setVisibility(View.VISIBLE);
            holder.view.year.setText(year);
            lastYear = year;
        }

        String md = TimeUtil.getTime(circleItem.getCreateTimeL(), "dd\nMM月");
        holder.view.time.setVisibility(View.INVISIBLE);
        SpannableStringBuilder stringBuilder =new SpannableStringBuilder(md);
        stringBuilder .setSpan(new AbsoluteSizeSpan(11,true),
            md.indexOf("\n"),md.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        );
        holder.view.time.setText(stringBuilder);
        if (!md.equals(lastMd)){
            holder.view.time.setVisibility(View.VISIBLE);
            lastMd=md;
        }

        holder.view.content.setText(circleItem.getContent());

        if (Objects.equals(circleItem.getType(), CircleItem.TYPE_IMG)) {
            holder.view.isVideo.setVisibility(View.GONE);
            int photoNum = circleItem.getPhotos().size();

            if (photoNum == 0) {
                holder.view.media.setVisibility(View.GONE);
                holder.view.single.setVisibility(View.GONE);
                holder.view.photoNum.setVisibility(View.GONE);
                holder.view.content.setBackgroundColor(context.getResources().getColor(io.openim.android.ouicore.R.color.theme_bg2));

            } else if (photoNum == 1) {
                holder.view.single.setVisibility(View.VISIBLE);
                holder.view.media.setVisibility(View.GONE);
                holder.view.photoNum.setVisibility(View.GONE);

                Glide.with(context).load(circleItem.getPhotos().get(0).url).placeholder(io.openim.android.ouicore.R.mipmap.ic_chat_photo).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).centerCrop().into(holder.view.icon);
            } else {
                holder.view.media.setVisibility(View.VISIBLE);
                holder.view.single.setVisibility(View.GONE);
                holder.view.photoNum.setVisibility(View.VISIBLE);
                holder.view.photoNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.tal_num_photo), photoNum));
                holder.view.media.setLayoutManager(new GridLayoutManager(context, 2));
                RecyclerViewAdapter<PhotoInfo, ViewHol.ImageViewHolder> adapter;
                holder.view.media.setAdapter(adapter = new RecyclerViewAdapter<PhotoInfo,
                    ViewHol.ImageViewHolder>(ViewHol.ImageViewHolder.class) {

                    @Override
                    public void onBindView(@NonNull ViewHol.ImageViewHolder holder, PhotoInfo url
                        , int position) {
                        holder.view.getRoot().getLayoutParams().height = (photoNum > 2) ?
                            Common.dp2px(38) : -1;

                        Glide.with(context).load(url.url).placeholder(io.openim.android.ouicore.R.mipmap.ic_chat_photo).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).centerCrop().into(holder.view.getRoot());
                    }
                });
                adapter.setItems(circleItem.getPhotos().subList(0, Math.min(photoNum, 4)));
            }
        } else if (Objects.equals(circleItem.getType(), CircleItem.TYPE_VIDEO)) {
            holder.view.single.setVisibility(View.VISIBLE);
            holder.view.isVideo.setVisibility(View.VISIBLE);
            holder.view.media.setVisibility(View.GONE);
            holder.view.photoNum.setVisibility(View.GONE);

            Glide.with(context).load(circleItem.getVideoImgUrl()).placeholder(io.openim.android.ouicore.R.mipmap.ic_chat_photo).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).centerCrop().into(holder.view.icon);
        } else {
            holder.view.media.setVisibility(View.GONE);
            holder.view.single.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return datas.size() + 1;//有head需要加1
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final AvatarImage headIv;
        public final TextView nameTv, newMsgTips;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headIv = itemView.findViewById(R.id.headIv);
            nameTv = itemView.findViewById(R.id.name);
            newMsgTips = itemView.findViewById(R.id.newMsgTips);
        }
    }

    private class PopupItemClickListener implements SnsPopupWindow.OnItemClickListener {
        private String momentID;
        //动态在列表中的位置
        private int mCirclePosition;
        private long mLasttime = 0;
        private CircleItem mCircleItem;

        public PopupItemClickListener(int circlePosition, CircleItem circleItem, String favorId) {
            this.momentID = favorId;
            this.mCirclePosition = circlePosition;
            this.mCircleItem = circleItem;
        }

        @Override
        public void onItemClick(ActionItem actionitem, int position) {
            switch (position) {
                case 0://点赞、取消点赞
                    if (System.currentTimeMillis() - mLasttime < 700)//防止快速点击操作
                        return;
                    mLasttime = System.currentTimeMillis();
                    if (presenter != null) {
                        if (BaseApp.inst().getString(io.openim.android.ouicore.R.string.star).equals(actionitem.mTitle.toString())) {
                            presenter.addFavort(mCirclePosition, momentID);
                        } else {//取消点赞
                            presenter.deleteFavort(mCirclePosition, momentID);
                        }
                    }
                    break;
                case 1://发布评论
                    if (presenter != null) {
                        CommentConfig config = new CommentConfig();
                        config.momentID = momentID;
                        config.circlePosition = mCirclePosition;
                        config.commentType = CommentConfig.Type.PUBLIC;
                        presenter.showEditTextBody(config);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
