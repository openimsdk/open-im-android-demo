package io.openim.android.ouicore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicore.databinding.ItemGroupShowBinding;
import io.openim.android.ouicore.databinding.ItemImgTxtBinding;
import io.openim.android.ouicore.databinding.ItemPsrsonSelectBinding;
import io.openim.android.ouicore.databinding.ItemPsrsonStickyBinding;
import io.openim.android.ouicore.databinding.LayoutContactItemBinding;
import io.openim.android.ouicore.databinding.ViewImageBinding;
import io.openim.android.ouicore.databinding.ViewRecyclerViewBinding;

public class ViewHol {

    public static class ItemViewHo extends RecyclerView.ViewHolder {
        public final ItemPsrsonSelectBinding view;

        public ItemViewHo(@NonNull View itemView) {
            super(ItemPsrsonSelectBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemPsrsonSelectBinding.bind(this.itemView);
        }
    }

    public static class StickyViewHo extends RecyclerView.ViewHolder {
        public final ItemPsrsonStickyBinding view;

        public StickyViewHo(@NonNull View itemView) {
            super(ItemPsrsonStickyBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemPsrsonStickyBinding.bind(this.itemView);
        }
    }

    public static class GroupViewHo extends RecyclerView.ViewHolder {
        public final ItemGroupShowBinding view;

        public GroupViewHo(@NonNull View itemView) {
            super(ItemGroupShowBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemGroupShowBinding.bind(this.itemView);
        }
    }

    public static class ImageTxtViewHolder extends RecyclerView.ViewHolder {
        public ItemImgTxtBinding view;

        public ImageTxtViewHolder(@NonNull View itemView) {
            super(ItemImgTxtBinding.inflate(LayoutInflater.from(itemView.getContext())).getRoot());
            view = ItemImgTxtBinding.bind(this.itemView);
        }
    }

    public static class ContactItemHolder extends RecyclerView.ViewHolder {
        public LayoutContactItemBinding viewBinding;

        public ContactItemHolder(@NonNull View itemView) {
            super(LayoutContactItemBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            this.viewBinding = LayoutContactItemBinding.bind(this.itemView);
        }
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public ViewRecyclerViewBinding viewBinding;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(ViewRecyclerViewBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            this.viewBinding = ViewRecyclerViewBinding.bind(this.itemView);
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ViewImageBinding view;

        public ImageViewHolder(@NonNull View itemView) {
            super(ViewImageBinding.inflate(LayoutInflater.from(itemView.getContext()),(ViewGroup) itemView, false).getRoot());
            view = ViewImageBinding.bind(this.itemView);
        }
    }
}
