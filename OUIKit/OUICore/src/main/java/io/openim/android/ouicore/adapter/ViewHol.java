package io.openim.android.ouicore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicore.databinding.ItemGroupShowBinding;
import io.openim.android.ouicore.databinding.ItemPsrsonSelectBinding;
import io.openim.android.ouicore.databinding.ItemPsrsonStickyBinding;

public class ViewHol {

    public static class ItemViewHo extends RecyclerView.ViewHolder {
        public final ItemPsrsonSelectBinding view;

        public ItemViewHo(@NonNull View itemView) {
            super(ItemPsrsonSelectBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemPsrsonSelectBinding.bind(this.itemView);
        }
    }

    public static class StickyViewHo extends RecyclerView.ViewHolder {
        public final  ItemPsrsonStickyBinding view;

        public StickyViewHo(@NonNull View itemView) {
            super(ItemPsrsonStickyBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemPsrsonStickyBinding.bind(this.itemView);
        }
    }

    public static class GroupViewHo extends RecyclerView.ViewHolder {
        public final  ItemGroupShowBinding view;

        public GroupViewHo(@NonNull View itemView) {
            super(ItemGroupShowBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemGroupShowBinding.bind(this.itemView);
        }
    }
}
