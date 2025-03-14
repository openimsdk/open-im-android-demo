package io.openim.android.ouicore.data;

import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.List;
import io.openim.android.ouicore.utils.MThreadTool;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.UserInfo;


public class DataManager{
    //封装过的好友信息 用于字母导航
    private static final String TAG = "DataManager";

    private static final DataManager instance = new DataManager();
    private DataManager() {

    }

    public static DataManager getInstance() {
        return instance;
    }

    private static class FetchAllGroupsRunnable implements Runnable {
        private int offset = 0;
        private final int COUNT = 1000;
        private final WeakReference<DataManager> viewModelRef;
        public FetchAllGroupsRunnable(DataManager viewModel) {
            this.viewModelRef = new WeakReference<>(viewModel);
        }
        @Override
        public void run() {
            fetchGroups(COUNT);
        }

        private void fetchGroups(int count) {
            DataManager viewModel = viewModelRef.get();
            if (viewModel == null) {
                return;
            }
            OpenIMClient.getInstance().groupManager.getJoinedGroupListPage(new OnBase<List<GroupInfo>>() {
                @Override
                public void onError(int code, String error) {
                    Log.e(TAG, "fetchGroups onError:" + error);
                }

                @Override
                public void onSuccess(List<GroupInfo> data) {
                    if (data.isEmpty()) {
                        return;
                    }

                    if (data.size() >= count) {
                        offset += count;
                        fetchGroups(COUNT);
                    }
                    Log.d(TAG, "fetchGroups data.size():" + data.size());
                }
            }, offset, count);
        }
    }
    public void getAllGroup() {
        Log.d(TAG, "getAllGroup()");
        MThreadTool.executorService.execute(new FetchAllGroupsRunnable(this));
    }

    private static class FetchAllFriendsRunnable implements Runnable {
        private int offset = 0;
        private final int FIRST_COUNT = 10000;
        private final int COUNT = 1000;
        private final WeakReference<DataManager> viewModelRef;
        public FetchAllFriendsRunnable(DataManager viewModel) {
            this.viewModelRef = new WeakReference<>(viewModel);
        }
        @Override
        public void run() {
            fetchFriends(FIRST_COUNT);
        }

        private void fetchFriends(int count) {
            DataManager viewModel = viewModelRef.get();
            if (viewModel == null) {
                return;
            }
            OpenIMClient.getInstance().friendshipManager.getFriendListPage(new OnBase<List<UserInfo>>() {
                @Override
                public void onError(int code, String error) {
                    Log.e(TAG, "fetchFriends onError:" + error + "(" + code + ")");
                }

                @Override
                public void onSuccess(List<UserInfo> data) {
                    if (data.isEmpty()) {
                        return;
                    }

                    if (data.size() >= count) {
                        offset += count;
                        fetchFriends(COUNT);
                    }
                    Log.d(TAG, "fetchFriends data.size():" + data.size() + ",offset:" + offset);
                }
            }, offset, count);
        }
    }

    public void getAllFriend() {
        Log.d(TAG, "getAllFriend()");
        MThreadTool.executorService.execute(new FetchAllFriendsRunnable(this));
    }

    public void getTotalUnreadMsgCount() {
        OpenIMClient.getInstance().conversationManager.getTotalUnreadMsgCount(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                Log.d(TAG, "getTotalUnreadMsgCount() onError:" + error + "(" + code + ")");
            }
            @Override
            public void onSuccess(String data) {
                Log.d(TAG, "getTotalUnreadMsgCount() onSuccess");
            }
        });
    }

    private class FetchAllConversationsRunnable implements Runnable {
        private int offset = 0;
        private final int COUNT = 500;

        @Override
        public void run() {
            fetchConversations(COUNT);
        }

        private void fetchConversations(int count) {
            OpenIMClient.getInstance().conversationManager.getConversationListSplit(new OnBase<List<ConversationInfo>>() {
                @Override
                public void onError(int code, String error) {
                    Log.e(TAG, "fetchConversations onError:" + error + "(" + code + ")");
                }

                @Override
                public void onSuccess(List<ConversationInfo> data) {
                    if (data.isEmpty()) {
                        return;
                    }

                    if (data.size() >= count) {
                        offset += count;
                        fetchConversations(COUNT);
                    }
                    Log.d(TAG, "fetchConversations data.size():" + data.size());
                }
            }, offset, count);
        }
    }

    public void getAllConversations() {
        Log.d(TAG, "getAllConversations()");
        MThreadTool.executorService.execute(new FetchAllConversationsRunnable());
    }

}
