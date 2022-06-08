package io.openim.android.ouicore.widget;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicore.databinding.ItemImgTxtBinding;

public class ImageTxtViewHolder extends RecyclerView.ViewHolder {
    public ItemImgTxtBinding view;

    public ImageTxtViewHolder(@NonNull View itemView) {
        super(ItemImgTxtBinding.inflate(LayoutInflater.from(itemView.getContext())).getRoot());
        view = ItemImgTxtBinding.bind(this.itemView);
    }
}
