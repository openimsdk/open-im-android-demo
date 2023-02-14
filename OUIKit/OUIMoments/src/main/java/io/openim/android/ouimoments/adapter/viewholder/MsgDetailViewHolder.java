package io.openim.android.ouimoments.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouimoments.databinding.ItemMsgDetailBinding;

public class MsgDetailViewHolder extends RecyclerView.ViewHolder {
    public ItemMsgDetailBinding view;

    public MsgDetailViewHolder(@NonNull View itemView) {
        super(ItemMsgDetailBinding.inflate(LayoutInflater.from(itemView.getContext()),
            (ViewGroup) itemView, false).getRoot());
        view = ItemMsgDetailBinding.bind(this.itemView);
    }
}
