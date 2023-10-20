package io.openim.android.ouiconversation.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;


import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.sdk.models.Message;

public class MessageAdapter extends RecyclerView.Adapter {

    private RecyclerView recyclerView;

    List<Message> messages;

    public MessageAdapter() {}

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getContentType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MessageViewHolder.createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (getItemViewType(position) != Constant.LOADING) {
            MessageViewHolder.MsgViewHolder msgViewHolder = (MessageViewHolder.MsgViewHolder) holder;
            msgViewHolder.setMessageAdapter(this);
            msgViewHolder.bindData(message, position);
            if (null != recyclerView)
                msgViewHolder.bindRecyclerView(recyclerView);
        }

    }

    @Override
    public int getItemCount() {
        return null == messages ? 0 : messages.size();
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void bindRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
