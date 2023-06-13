package io.openim.android.ouicore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicore.databinding.ItemFile2Binding;
import io.openim.android.ouicore.databinding.ItemGroupShowBinding;
import io.openim.android.ouicore.databinding.ItemImgTxtBinding;
import io.openim.android.ouicore.databinding.ItemImgTxtRightBinding;
import io.openim.android.ouicore.databinding.ItemLabelMemberBinding;
import io.openim.android.ouicore.databinding.ItemPsrsonSelectBinding;
import io.openim.android.ouicore.databinding.ItemPsrsonStickyBinding;
import io.openim.android.ouicore.databinding.LayoutContactItemBinding;
import io.openim.android.ouicore.databinding.LayoutLabelItemBinding;
import io.openim.android.ouicore.databinding.ViewDividingLineBinding;
import io.openim.android.ouicore.databinding.ViewImageBinding;
import io.openim.android.ouicore.databinding.ViewRecyclerViewBinding;
import io.openim.android.ouicore.databinding.ViewSelectImageBinding;
import io.openim.android.ouicore.utils.Common;

public class ViewHol {

    public static class DivisionItemViewHo extends RecyclerView.ViewHolder {
        public final ViewDividingLineBinding view;

        public DivisionItemViewHo(@NonNull View itemView, int height) {
            super(ViewDividingLineBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = ViewDividingLineBinding.bind(this.itemView);
            view.getRoot().getLayoutParams().height = height;
        }
    }

    public static class FileItemViewHo extends RecyclerView.ViewHolder {
        public final ItemFile2Binding view;

        public FileItemViewHo(@NonNull View itemView) {
            super(ItemFile2Binding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = ItemFile2Binding.bind(this.itemView);
        }
    }

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
    public static class ImageTxtRightViewHolder extends RecyclerView.ViewHolder {
        public ItemImgTxtRightBinding view;

        public ImageTxtRightViewHolder(@NonNull View itemView) {
            super(ItemImgTxtRightBinding.inflate(LayoutInflater.from(itemView.getContext()),(ViewGroup) itemView, false).getRoot() );
            view = ItemImgTxtRightBinding.bind(this.itemView);
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
            super(ViewImageBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ViewImageBinding.bind(this.itemView);
        }
    }

    public static class SelectImageViewHolder extends RecyclerView.ViewHolder {
        public ViewSelectImageBinding view;

        public SelectImageViewHolder(@NonNull View itemView) {
            super(ViewSelectImageBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ViewSelectImageBinding.bind(this.itemView);
        }
    }
    public static   class LabelItem extends RecyclerView.ViewHolder{
        public final LayoutLabelItemBinding view;

        public LabelItem(@NonNull View itemView) {
            super(LayoutLabelItemBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = LayoutLabelItemBinding.bind(this.itemView);
        }
    }
    public static   class LabelMemberItem extends RecyclerView.ViewHolder{
        public final ItemLabelMemberBinding view;

        public LabelMemberItem(@NonNull View itemView) {
            super(ItemLabelMemberBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = ItemLabelMemberBinding.bind(this.itemView);
        }
    }
}
