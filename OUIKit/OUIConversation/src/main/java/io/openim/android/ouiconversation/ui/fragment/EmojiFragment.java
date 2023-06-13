package io.openim.android.ouiconversation.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.vanniktech.emoji.Emoji;
import com.vanniktech.emoji.EmojiTheming;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.recent.RecentEmojiManager;
import com.vanniktech.emoji.search.NoSearchEmoji;
import com.vanniktech.emoji.variant.VariantEmojiManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.FragmentEmojiBinding;
import io.openim.android.ouiconversation.databinding.ItemEmojiBinding;
import io.openim.android.ouiconversation.ui.MediaHistoryActivity;
import io.openim.android.ouiconversation.ui.emoji.CustomEmojiManageActivity;
import io.openim.android.ouiconversation.vm.CustomEmojiVM;
import io.openim.android.ouiconversation.widget.TailInputEditText;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.ISubscribe;
import io.openim.android.ouicore.base.vm.Subject;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.entity.CustomEmoji;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.widget.GridSpaceItemDecoration;
import io.openim.android.ouicore.widget.SpacesItemDecoration;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;
import io.realm.Realm;
import io.realm.RealmResults;


public class EmojiFragment extends BaseFragment<ChatVM> {
    private FragmentEmojiBinding v;
    private TailInputEditText tailInputEditText;
    private RecyclerViewAdapter<Object, ViewHol.ImageViewHolder> customEmojiAdapter;
    public int addID = io.openim.android.ouicore.R.mipmap.ic_add3;
    private CustomEmojiVM customEmojiVM;
    private final List<Object> customEmojis=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        tailInputEditText = getActivity().findViewById(R.id.chatInput);
        v = FragmentEmojiBinding.inflate(inflater);
        init();
        listener();
        return v.getRoot();
    }

    private void listener() {
        customEmojiVM.loadCustomEmoji();
        customEmojiVM.customEmojis.observe(getActivity(), emojis -> {
            customEmojis.clear();
            customEmojis.add(0,addID);
            customEmojis.addAll(emojis);
            customEmojiAdapter.setItems(customEmojis);
        });
    }

    private void init() {
        customEmojiVM = Easy.find(CustomEmojiVM.class);

        v.emojiView.setUp(v.getRoot(), null, null, tailInputEditText,
            new EmojiTheming(getResources().getColor(io.openim.android.ouicore.R.color.white),
                getResources().getColor(io.openim.android.ouicore.R.color.txt_shallow),
                getResources().getColor(io.openim.android.ouicore.R.color.theme),
                getResources().getColor(io.openim.android.ouicore.R.color.txt_shallow),
                getResources().getColor(io.openim.android.ouicore.R.color.txt_black),
                getResources().getColor(io.openim.android.ouicore.R.color.txt_shallow)),
            new RecentEmojiManager(getContext()), NoSearchEmoji.INSTANCE,
            new VariantEmojiManager(getContext()), null);

        v.menu.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == group.getChildAt(0).getId()) {
                v.emojiView.setVisibility(View.VISIBLE);
                v.customEmoji.setVisibility(View.INVISIBLE);
            } else {
                v.emojiView.setVisibility(View.INVISIBLE);
                v.customEmoji.setVisibility(View.VISIBLE);
            }
        });
        ((RadioButton) v.menu.getChildAt(0)).setChecked(true);


        v.customEmoji.setLayoutManager(new GridLayoutManager(getContext(), 4));
        GridSpaceItemDecoration divItemDecoration = new GridSpaceItemDecoration(Common.dp2px(25));
        v.customEmoji.addItemDecoration(divItemDecoration);
        v.customEmoji.setAdapter(customEmojiAdapter = new RecyclerViewAdapter<Object,
            ViewHol.ImageViewHolder>(ViewHol.ImageViewHolder.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ImageViewHolder holder, Object obj,
                                   int position) {
                holder.view.getRoot().getLayoutParams().height = Common.dp2px(65);
                if (obj instanceof CustomEmoji) {
                    CustomEmoji data = (CustomEmoji) obj;
                    String url = data.getThumbnailUrl();
                    if (TextUtils.isEmpty(url))
                        url = data.getSourceUrl();
                    Glide.with(getContext()).load(url).centerCrop().into(holder.view.getRoot());
                    holder.view.getRoot().setOnClickListener(v1 -> {
                        Map<String, Object> param = new HashMap<>();
                        param.put("url", data.getSourceUrl());
                        param.put("width", data.getSourceW());
                        param.put("height", data.getSourceH());

                        Message msg =
                            OpenIMClient.getInstance().messageManager.createFaceMessage(-1,
                                GsonHel.toJson(param));
                        vm.sendMsg(msg);
                    });
                } else {
                    Glide.with(getContext()).load(obj).centerCrop().into(holder.view.getRoot());
                    holder.view.getRoot().setOnClickListener(v1 -> {
                        startActivity(new Intent(getActivity(), CustomEmojiManageActivity.class));
                    });
                }
            }
        });
    }




    public void setChatVM(ChatVM vm) {
        this.vm = vm;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        v.emojiView.tearDown();
    }
}
