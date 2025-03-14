package io.openim.android.ouicore.vm;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;


import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.entity.UserList;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.PublicUserInfo;
import io.openim.android.sdk.models.SearchResult;
import io.openim.android.sdk.models.SearchResultItem;
import io.openim.android.sdk.models.UserInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import kotlin.Pair;

public class SearchVM extends BaseViewModel {
    public State<List<GroupInfo>> groupsInfo = new State<>(new ArrayList<>());
    public State<List<UserInfo>> userInfo = new State<>(new ArrayList<>());
    public State<List<FriendshipInfo>> friendshipInfo =
        new State<>(new ArrayList<>());
    public State<List<GroupMembersInfo>> groupMembersInfo =
        new State<>(new ArrayList<>());

    public State<String> hail = new State<>();
    //用户 或群组id
    public State<String> searchContent = new State<>("");
    public State<Boolean> isFriend = new State<>(null);
    //true 搜索人 false 搜索群
    public boolean isPerson = false;
    public int page;
    public int pageSize=50;
    private final Handler handler = new Handler();

//    public void getExtendUserInfo(String uid) {
//        List<String> ids = new ArrayList<>();
//        ids.add(uid);
//        Parameter parameter = new Parameter().add("userIDs", ids);
//        N.API(OneselfService.class).getUsersFullInfo(parameter.buildJsonBody())
//            .map(OneselfService.turn(HashMap.class))
//            .compose(N.IOMain())
//            .subscribe(new NetObserver<HashMap>(getContext()) {
//                @Override
//                protected void onFailure(Throwable e) {
//                    getIView().toast(e.getMessage());
//                }
//
//                @Override
//                public void onSuccess(HashMap map) {
//                    try {
//                        ArrayList arrayList = (ArrayList) map.get("users");
//                        if (null == arrayList || arrayList.isEmpty()) return;
//
//                        UserInfo u = GsonHel.getGson().fromJson(arrayList.get(0).toString(),
//                            UserInfo.class);
//                        userInfo.setValue(new ArrayList<>(Collections.
//                            singleton(updateUserInfo(
//                                userInfo.val().isEmpty()?null:
//                                userInfo.val().get(0), u))));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });
//    }
    public UserInfo updateUserInfo(UserInfo origin, Object update) {
        try {
            UserInfo updateInfo = new UserInfo();
            if (update instanceof PublicUserInfo) {
                updateInfo.setUserID(((PublicUserInfo)update).getUserID());
                updateInfo.setNickname(((PublicUserInfo)update).getNickname());
                updateInfo.setFaceURL(((PublicUserInfo)update).getFaceURL());
                updateInfo.setEx(((PublicUserInfo)update).getEx());
                updateInfo.setCreateTime(((PublicUserInfo)update).getCreateTime());
            } else updateInfo = (UserInfo) update;
            if (null == origin) {
                return updateInfo;
            }
            String json = JSONObject.toJSONString(origin);
            Map originMap = JSONObject.parseObject(json, Map.class);

            String json2 = JSONObject.toJSONString(updateInfo);
            Map updateMap = JSONObject.parseObject(json2, Map.class);

            originMap.putAll(updateMap);
            return JSONObject.parseObject(GsonHel.toJson(originMap),
                UserInfo.class);
        } catch (Exception ignored) {
        }
        return origin;
    }
//    public SearchVM getUsersInfoWithCache(String id, String gid) {
//        OpenIMClient.getInstance().userInfoManager
//            .getUsersInfoWithCache(new OnBase<List<PublicUserInfo>>() {
//                @Override
//                public void onSuccess(List<PublicUserInfo> data) {
//                    if (!data.isEmpty()) {
//                        PublicUserInfo u = data.get(0);
//                        userInfo.setValue(new ArrayList<>(Collections.
//                            singleton(updateUserInfo(
//                                userInfo.val().isEmpty()?null:
//                                userInfo.val().get(0), u))));
//                    }
//                }
//            }, new ArrayList<>(Collections.singleton(id)), gid);
//        return this;
//    }

    public Observable<UserInfo> getUserData(String uid) {
        List<String> uids = new ArrayList<>();
        uids.add(uid);
        return Observable.create((ObservableOnSubscribe<Pair<UserInfo, Boolean>>) emitter -> {
            OpenIMClient.getInstance().friendshipManager.getFriendsInfo(new OnBase<List<UserInfo>>() {
                @Override
                public void onError(int code, String error) {
                    emitter.onError(new Exception(code+error));
                }

                @Override
                public void onSuccess(List<UserInfo> data) {
                    boolean hasFriendInfo = data != null && !data.isEmpty();
                    if (hasFriendInfo) {
                        emitter.onNext(new Pair<>(data.get(0), true));
                    } else {
                        emitter.onNext(new Pair<>(new UserInfo(), false));
                    }
                    emitter.onComplete();
                }
            }, uids, false);
        }).concatMap(upStreamData ->
            Observable.create(emitter -> {
                if (TextUtils.isEmpty(upStreamData.getFirst().getUserID())) {
                    List<String> ids = new ArrayList<>();
                    ids.add(uid);
                    Parameter parameter = new Parameter().add("userIDs", ids);
                    N.API(OneselfService.class).getUsersFullInfo(parameter.buildJsonBody())
                        .map(OneselfService.turn(HashMap.class))
                        .compose(N.IOMain())
                        .subscribe(new NetObserver<HashMap>(getContext()) {
                            @Override
                            protected void onFailure(Throwable e) {
                                emitter.onError(e);
                            }

                            @Override
                            public void onSuccess(HashMap map) {
                                try {
                                    ArrayList arrayList = (ArrayList) map.get("users");
                                    if (null == arrayList || arrayList.isEmpty()) return;

                                    UserInfo u = GsonHel.getGson().fromJson(arrayList.get(0).toString(),
                                        UserInfo.class);
                                    emitter.onNext(u);
                                } catch (Exception e) {
                                   emitter.onError(e);
                                } finally {
                                    emitter.onComplete();
                                }
                            }
                        });
                } else {
                    emitter.onNext(upStreamData.getFirst());
                    emitter.onComplete();
                }
                isFriend.setValue(upStreamData.getSecond());
            })
        );
    }

    //uid、昵称、备注、手机号
    public void searchUser(String keyword) {
        Parameter parameter = new Parameter();
        parameter.add("keyword", keyword);
//        "pagination": {
//            "pageNumber": 0,
//                "showNumber": 10
//        },
        Map<String, Integer> pa = new HashMap<>();
        pa.put("pageNumber", 1);
        pa.put("showNumber", 100);
        parameter.add("pagination", pa);
        parameter.add("keyword", keyword);
        parameter.add("keyword", keyword);

        N.API(OneselfService.class).searchUser(parameter.buildJsonBody())
            .map(OneselfService.turn(UserList.class))
            .compose(N.IOMain())
            .subscribe(new NetObserver<UserList>("") {


                @Override
                public void onSuccess(UserList o) {
                 try {
                     if (o.users.size() > 0)
                         userInfo.setValue(o.users);
                 }catch (Exception e){e.printStackTrace();}
                }

                @Override
                protected void onFailure(Throwable e) {

                }
            });
    }

    public void searchGroupMemberByNickname(String groupId, String key) {
        List<String> keys = new ArrayList<>(); // 用户ID集合
        keys.add(key);
        OpenIMClient.getInstance().groupManager.searchGroupMembers(new OnBase<List<GroupMembersInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(List<GroupMembersInfo> data) {
                if (key.isEmpty()) return;
                groupMembersInfo.getValue().addAll(data);
                groupMembersInfo.setValue(groupMembersInfo.getValue());
            }
        }, groupId, keys, false, true, page, pageSize);
    }

    public void addFriend() {
        OnBase<String> callBack = new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                Toast.makeText(getContext(), "发送成功", Toast.LENGTH_SHORT).show();
                getIView().onSuccess("");
//                String remarkStr = remark.getValue();
//                if (!TextUtils.isEmpty(remarkStr)) {
//                    OpenIMClient.getInstance().friendshipManager.setFriendRemark(null,
//                        searchContent.getValue(), remarkStr);
//                }
            }
        };
        if (isPerson)
            OpenIMClient.getInstance().friendshipManager.addFriend(callBack,
                searchContent.getValue(), hail.getValue());
        else
            OpenIMClient.getInstance().groupManager.joinGroup(callBack, searchContent.getValue(),
                hail.getValue(), 2);
    }

    public void searchGroup(String gid) {
        List<String> groupIds = new ArrayList<>(); // 群ID集合
        groupIds.add(gid);
        OpenIMClient.getInstance().groupManager.getGroupsInfo(new IMUtil.IMCallBack<List<GroupInfo>>(){
            @Override
            public void onSuccess(List<GroupInfo> data) {
                groupsInfo.setValue(data);
            }
        }, groupIds);
    }

    public void searchFriendV2() {
        Parameter parameter = new Parameter();

        Map<String, Integer> pa = new HashMap<>();
        pa.put("pageNumber", 1);
        pa.put("showNumber", 100);
        parameter.add("pagination", pa);
        parameter.add("keyword", searchContent.getValue());

        N.API(OneselfService.class).searchFriends(parameter.buildJsonBody())
            .map(OneselfService.turn(UserList.class))
            .compose(N.IOMain())
            .subscribe(new NetObserver<UserList>("") {


                @Override
                public void onSuccess(UserList o) {
                    try {
                        if (page == 1) {
                            userInfo.getValue().clear();
                        }
                        if (null!=o.users&& !o.users.isEmpty()) {
                            userInfo.getValue().addAll(o.users);
                        }
                        userInfo.setValue(userInfo.getValue());
                    }catch (Exception e){e.printStackTrace();}
                }
            });
    }

    public void searchGroupV2() {
        OpenIMClient.getInstance().groupManager.searchGroups(new IMUtil.IMCallBack<List<GroupInfo>>(){
            @Override
            public void onSuccess(List<GroupInfo> data) {
                if (page == 1) {
                    groupsInfo.getValue().clear();
                }
                if (!data.isEmpty()) {
                    groupsInfo.getValue().addAll(data);
                }
                groupsInfo.setValue(groupsInfo.getValue());
            }
        }, buildKeyWord(), true, true);
    }

    /**
     * 查询当前用户信息，如果是好友则返回好友信息，反之则为普通的PublicUserInfo
     * @param uid 待查询用户id
     * @return 可订阅UserInfo
     */
    public Observable<UserInfo> queryRealUserInfo(String uid) {
        List<String> uids = new ArrayList<>();
        uids.add(uid);
        return Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            OpenIMClient.getInstance().friendshipManager.checkFriend(new OnBase<List<FriendshipInfo>>() {
                @Override
                public void onError(int code, String error) {
                    emitter.onError(new Exception(error+code));
                }

                @Override
                public void onSuccess(List<FriendshipInfo> data) {
                    emitter.onNext(data != null && !data.isEmpty() && data.get(0).getResult() == 1);
                    emitter.onComplete();
                }
            }, uids);
        }).concatMap(isFriend ->
            Observable.create(emitter -> {
                    if (isFriend)
                        OpenIMClient.getInstance().friendshipManager.getFriendsInfo(new OnBase<List<UserInfo>>() {
                            @Override
                            public void onError(int code, String error) {
                                emitter.onError(new Exception(error+code));
                            }

                            @Override
                            public void onSuccess(List<UserInfo> data) {
                                if (data != null && !data.isEmpty()) {
                                    emitter.onNext(data.get(0));
                                    emitter.onComplete();
                                }
                                else
                                    emitter.onError(new Exception("The queried friend info is null value. "));
                            }
                        }, uids, false);
                    else
                        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<PublicUserInfo>>() {
                            @Override
                            public void onError(int code, String error) {
                                emitter.onError(new Exception(error+code));
                            }

                            @Override
                            public void onSuccess(List<PublicUserInfo> data) {
                                if (data != null && !data.isEmpty()){
                                    PublicUserInfo current = data.get(0);
                                    UserInfo publicUserInfo = new UserInfo();
                                    publicUserInfo.setUserID(current.getUserID());
                                    publicUserInfo.setNickname(current.getNickname());
                                    publicUserInfo.setFaceURL(current.getFaceURL());
                                    publicUserInfo.setEx(current.getEx());
                                    publicUserInfo.setCreateTime(current.getCreateTime());
                                    emitter.onNext(publicUserInfo);
                                    emitter.onComplete();
                                }
                                else
                                    emitter.onError(new Exception("The queried user info is null value. "));
                            }
                        }, uids);
                }
            ));
    }

    private List<String> buildKeyWord() {
        List<String> keyWords = new ArrayList<>();
        keyWords.add(searchContent.getValue());
        return keyWords;
    }


    public void clearData() {
        groupsInfo.getValue().clear();
        userInfo.getValue().clear();
    }

    public void addTextChangedListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) searchContent.setValue("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String input = s.toString();
                    page = 0;
                    searchContent.setValue(input);
                }, 500);
            }
        });
    }
}
