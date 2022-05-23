package io.openim.android.ouicontact.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


import io.openim.android.ouicontact.utils.Constant;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.sdk.models.Message;

public class MessageAdapter extends RecyclerView.Adapter {

    List<Message> messages;
    //自己的userId
    public static String OWN_ID;

    public MessageAdapter() {
        OWN_ID = LoginCertificate.getCache(BaseApp.instance()).userID;
    }

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
            msgViewHolder.bindData(message,position);
        }


    }

    @Override
    public int getItemCount() {
        return null == messages ? 0 : messages.size();
    }
}
