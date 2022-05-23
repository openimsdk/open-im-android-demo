package io.openim.android.ouicontact.adapter;

import static io.openim.android.ouicontact.adapter.MessageAdapter.OWN_ID;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicontact.R;

import io.openim.android.ouicontact.databinding.LayoutLoadingSmallBinding;
import io.openim.android.ouicontact.databinding.LayoutMsgBinding;


import io.openim.android.ouicontact.databinding.LayoutMsgTxtLeftBinding;
import io.openim.android.ouicontact.databinding.LayoutMsgTxtRightBinding;
import io.openim.android.ouicontact.utils.Constant;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.models.Message;

public class MessageViewHolder {

    public static RecyclerView.ViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constant.LOADING)
            return new LoadingView(parent);

        if (viewType == Constant.MsgType.TXT)
            return new TXTView(parent);

        return new TXTView(parent);
    }

    //加载中...
    public static class LoadingView extends RecyclerView.ViewHolder {
        public LoadingView(ViewGroup parent) {
            super(LayoutLoadingSmallBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false).getRoot());
        }
    }

    //文本消息
    public static class TXTView extends MessageViewHolder.MsgViewHolder {

        public TXTView(ViewGroup parent) {
            super(parent);
        }

        @Override
        int getLeftInflatedId() {
            return R.layout.layout_msg_txt_left;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_txt_right;
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgTxtLeftBinding v = LayoutMsgTxtLeftBinding.bind(itemView);
            v.avatar.load(message.getSenderFaceUrl());
            v.content.setText(message.getContent());
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl());
            v.content2.setText(message.getContent());
        }

    }

    public abstract static class MsgViewHolder extends RecyclerView.ViewHolder {
        private boolean leftIsInflated = false, rightIsInflated = false;
        private final ViewStub right;
        private final ViewStub left;

        public MsgViewHolder(ViewGroup itemView) {
            super(buildRoot(itemView));
            left = this.itemView.findViewById(R.id.left);
            right = this.itemView.findViewById(R.id.right);

            left.setOnInflateListener((stub, inflated) -> leftIsInflated = true);
            right.setOnInflateListener((stub, inflated) -> rightIsInflated = true);
        }

        public static View buildRoot(ViewGroup parent) {
            return LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg, parent, false);
        }

        abstract int getLeftInflatedId();

        abstract int getRightInflatedId();

        abstract void bindLeft(View itemView, Message message);

        abstract void bindRight(View itemView, Message message);

        /**
         * 是否是自己发的消息
         */
        protected boolean isOwn = false;

        //绑定数据
        public void bindData(Message message, int position) {
            if (isOwn = message.getSendID().equals(OWN_ID)) {
                if (leftIsInflated)
                    left.setVisibility(View.GONE);
                if (rightIsInflated)
                    right.setVisibility(View.VISIBLE);
                if (!rightIsInflated) {
                    right.setLayoutResource(getRightInflatedId());
                    right.inflate();
                }
                bindRight(itemView, message);
            } else {
                if (leftIsInflated)
                    left.setVisibility(View.VISIBLE);
                if (rightIsInflated)
                    right.setVisibility(View.GONE);
                if (!leftIsInflated) {
                    left.setLayoutResource(getLeftInflatedId());
                    left.inflate();
                }
                bindLeft(itemView, message);
            }
        }
    }
}
