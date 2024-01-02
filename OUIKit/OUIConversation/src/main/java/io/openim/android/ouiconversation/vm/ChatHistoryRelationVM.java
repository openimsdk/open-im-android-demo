package io.openim.android.ouiconversation.vm;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SearchResult;
import io.openim.android.sdk.models.SearchResultItem;

public class ChatHistoryRelationVM extends BaseVM {
    public String searchContent;
    public String gid;
    public State<SearchResultItem> searchResultItem = new State<>();
    public int page = 1;
    public int count = 30;


    public void searchContent(String content) {
        List<String> keys = null;
        if (!TextUtils.isEmpty(content)) {
            keys = new ArrayList<>();
            keys.add(content);
        }
        List<Integer> messageTypeLists = new ArrayList<>();
        messageTypeLists.add(MessageType.TEXT);
        messageTypeLists.add(MessageType.AT_TEXT);
        OpenIMClient.getInstance().messageManager.searchLocalMessages(new OnBase<SearchResult>() {
            @Override
            public void onError(int code, String error) {
                clear();
            }

            @Override
            public void onSuccess(SearchResult data) {
                if (data.getTotalCount() == 0) {
                    if (page == 1) clear();
                    return;
                }
                List<Message> messages = data.getSearchResultItems().get(0).getMessageList();
                for (Message message : messages) {
                    IMUtil.buildExpandInfo(message);
                }
                if (page == 1) searchResultItem.val().setMessageList(messages);
                else searchResultItem.val().getMessageList().addAll(messages);
                searchResultItem.update();
            }
        }, searchResultItem.val().getConversationID(), keys, 0, new ArrayList<>(),
            messageTypeLists, 0, 0, page, count);
    }

    private void clear() {
        searchResultItem.val().getMessageList().clear();
        searchResultItem.update();
    }
}
