package io.openim.android.ouimoments.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicore.databinding.ViewDividingLineBinding;
import io.openim.android.ouimoments.databinding.LayoutTargetUserMomentsBinding;

public class TargetMomentsViewHolder extends RecyclerView.ViewHolder {

    public final LayoutTargetUserMomentsBinding view;

    public TargetMomentsViewHolder(@NonNull View itemView) {
        super(LayoutTargetUserMomentsBinding.inflate(LayoutInflater.from(itemView.getContext()),
            (ViewGroup) itemView, false).getRoot());
        view = LayoutTargetUserMomentsBinding.bind(this.itemView);
    }
}
