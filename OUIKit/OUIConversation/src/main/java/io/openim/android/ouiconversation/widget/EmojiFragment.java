package io.openim.android.ouiconversation.widget;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

import io.openim.android.ouiconversation.databinding.FragmentEmojiBinding;
import io.openim.android.ouiconversation.databinding.ItemEmojiBinding;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Common;


public class EmojiFragment extends BaseFragment<ChatVM> {
    private FragmentEmojiBinding v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = FragmentEmojiBinding.inflate(inflater);
        init();
        return v.getRoot();
    }

    private void init() {
        v.recyclerview.setLayoutManager(new GridLayoutManager(getContext(), 6));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter<String, ItemEmojiHolder>(ItemEmojiHolder.class) {

            @Override
            public void onBindView(@NonNull ItemEmojiHolder holder, String data, int position) {
                holder.v.emojiIv.setImageResource(Common.getMipmapId(EmojiUtil.emojiFaces.get(data)));
                holder.v.emojiIv.setOnClickListener(v1 -> {
                    vm.emojiMessages.getValue().add(data);
                    vm.emojiMessages.setValue(vm.emojiMessages.getValue());
                });
            }
        };
        v.recyclerview.setAdapter(adapter);
        adapter.setItems(Arrays.asList(EmojiUtil.emojiFaces.keySet().toArray()));
    }

    public static class ItemEmojiHolder extends RecyclerView.ViewHolder {
        public ItemEmojiBinding v;

        public ItemEmojiHolder(@NonNull View itemView) {
            super(ItemEmojiBinding.inflate(LayoutInflater.from(itemView.getContext())).getRoot());
            v = ItemEmojiBinding.bind(this.itemView);
        }
    }

    public void setChatVM(ChatVM vm) {
        this.vm = vm;
    }

}
