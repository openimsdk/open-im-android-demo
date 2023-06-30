package io.openim.android.ouiconversation.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.databinding.ActivityMediaHistoryBinding;
import io.openim.android.ouiconversation.databinding.ItemFileBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.databinding.ItemImgTxtBinding;
import io.openim.android.ouicore.utils.ByteUtil;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.models.Message;
@Route(path = Routes.Conversation.FILE_HISTORY)
public class FileHistoryActivity extends BaseActivity<ChatVM, ActivityMediaHistoryBinding> {

    private RecyclerViewAdapter adapter;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMediaHistoryBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        searchLocalMessages();
        listener();
    }

    private void listener() {
        vm.searchMessageItems.observe(this, list -> {
            if (list.isEmpty()) return;
            adapter.notifyItemRangeChanged(list.size() - vm.count, list.size());
        });
        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerView.getLayoutManager();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == vm.searchMessageItems.getValue().size() - 1
                    && vm.searchMessageItems.getValue().size() >= vm.count) {
                    page++;
                    searchLocalMessages();
                }
            }
        });
    }

    private void searchLocalMessages() {
        vm.searchLocalMessages(null, page, MessageType.FILE);
    }

    private void initView() {
        view.title.setText(
            io.openim.android.ouicore.R.string.file);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<Message, FileItemHolder>(FileItemHolder.class) {
            @Override
            public void onBindView(@NonNull FileItemHolder holder,
                                   Message data, int position) {

                holder.v.avatar.load(data.getSenderFaceUrl());
                holder.v.nickName.setText(data.getSenderNickname());
                holder.v.time.setText(TimeUtil.getTime(data.getSendTime() , TimeUtil.yearTimeFormat));
                holder.v.title.setText(data.getFileElem().getFileName());
                holder.v.size.setText(ByteUtil.bytes2kb(data.getFileElem().getFileSize()));
                holder.v.item.setOnClickListener(v -> GetFilePathFromUri.openFile(v.getContext(), data));
            }
        });
        adapter.setItems(vm.searchMessageItems.getValue());
    }

    public static class FileItemHolder extends RecyclerView.ViewHolder {
        ItemFileBinding v;

        public FileItemHolder(@NonNull View itemView) {
            super(ItemFileBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            v = ItemFileBinding.bind(this.itemView);
        }
    }
}
