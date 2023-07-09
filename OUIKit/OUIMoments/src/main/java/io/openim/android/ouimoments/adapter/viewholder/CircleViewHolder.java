package io.openim.android.ouimoments.adapter.viewholder;

import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicore.widget.AvatarImage;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.widgets.CommentListView;
import io.openim.android.ouimoments.widgets.ExpandTextView;
import io.openim.android.ouimoments.widgets.PraiseListView;
import io.openim.android.ouimoments.widgets.SnsPopupWindow;
import io.openim.android.ouimoments.widgets.videolist.model.VideoLoadMvpView;
import io.openim.android.ouimoments.widgets.videolist.widget.TextureVideoView;

/**
 */
public abstract class CircleViewHolder extends RecyclerView.ViewHolder implements VideoLoadMvpView {

    public final static int TYPE_URL = 1;
    public final static int TYPE_IMAGE = 2;
    public final static int TYPE_VIDEO = 3;

    public int viewType;

    public AvatarImage headIv;
    public TextView nameTv;
    public TextView urlTipTv;
    /** 动态的内容 */
    public ExpandTextView contentTv;
    public TextView timeTv,authorityTv,aboutWhoTv;
    public TextView deleteBtn;
    public ImageView snsBtn,authorityIv;
    public LinearLayout authorityLy;
    /** 点赞列表*/
    public PraiseListView praiseListView;

    public LinearLayout digCommentBody;
    public View digLine;

    /** 评论列表 */
    public CommentListView commentList;
    // ===========================
    public SnsPopupWindow snsPopupWindow;

    public CircleViewHolder(View itemView, int viewType) {
        super(itemView);
        this.viewType = viewType;

        ViewStub viewStub = (ViewStub) itemView.findViewById(R.id.viewStub);

        initSubView(viewType, viewStub);

        headIv = (AvatarImage) itemView.findViewById(R.id.headIv);
        nameTv = (TextView) itemView.findViewById(R.id.nameTv);
        digLine = itemView.findViewById(R.id.lin_dig);

        contentTv = (ExpandTextView) itemView.findViewById(R.id.contentTv);
        urlTipTv = (TextView) itemView.findViewById(R.id.urlTipTv);
        timeTv = (TextView) itemView.findViewById(R.id.timeTv);
        deleteBtn = (TextView) itemView.findViewById(R.id.deleteBtn);
        aboutWhoTv = (TextView) itemView.findViewById(R.id.aboutWhoTv);
        authorityLy = (LinearLayout) itemView.findViewById(R.id.authorityLy);
        authorityIv = (ImageView) itemView.findViewById(R.id.authorityIv);
        authorityTv = (TextView) itemView.findViewById(R.id.authorityTv);
        snsBtn = (ImageView) itemView.findViewById(R.id.snsBtn);
        praiseListView = (PraiseListView) itemView.findViewById(R.id.praiseListView);

        digCommentBody = (LinearLayout) itemView.findViewById(R.id.digCommentBody);
        commentList = (CommentListView)itemView.findViewById(R.id.commentList);

        snsPopupWindow = new SnsPopupWindow(itemView.getContext());

    }

    public abstract void initSubView(int viewType, ViewStub viewStub);

    @Override
    public TextureVideoView getVideoView() {
        return null;
    }

    @Override
    public void videoBeginning() {

    }

    @Override
    public void videoStopped() {

    }

    @Override
    public void videoPrepared(MediaPlayer player) {

    }

    @Override
    public void videoResourceReady(String videoPath) {

    }
}
