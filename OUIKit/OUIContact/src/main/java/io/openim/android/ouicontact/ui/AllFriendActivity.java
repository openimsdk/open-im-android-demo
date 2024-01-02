package io.openim.android.ouicontact.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.openim.android.ouicontact.databinding.ActivityAllFriendBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutPopSelectedFriendsBinding;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.vm.SocialityVM;
import io.openim.android.ouicore.widget.StickyDecoration;
import io.openim.android.sdk.models.FriendInfo;

@Route(path = Routes.Contact.ALL_FRIEND)
public class AllFriendActivity extends BaseActivity<SocialityVM, ActivityAllFriendBinding> {

    private RecyclerViewAdapter<ExUserInfo, RecyclerView.ViewHolder> adapter;
    private LinearLayoutManager mLayoutManager;

    private SelectTargetVM selectTargetVM;
    private ActivityResultLauncher<Intent> launcher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), v -> {
            if (v.getResultCode() != RESULT_OK) return;
            Intent intent = v.getData();
            Set<MultipleChoice> set =
                (Set<MultipleChoice>) intent.getSerializableExtra(Constant.K_RESULT);
            for (MultipleChoice data : set) {
                if (data.isSelect) {
                    if (!selectTargetVM.contains(data)) {
                        selectTargetVM.metaData.val().add(data);
                        selectTargetVM.metaData.update();
                    }
                } else {
                    selectTargetVM.removeMetaData(data.key);
                }
            }
        });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SocialityVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityAllFriendBinding.inflate(getLayoutInflater()));
        sink();
        init();
        vm.getAllFriend();

        listener();
        initView();
    }

    void init() {
        try {
            selectTargetVM = Easy.find(SelectTargetVM.class);
            view.searchView.setVisibility(View.VISIBLE);
            if (!selectTargetVM.isShareCard()) {
                view.bottom.getRoot().setVisibility(View.VISIBLE);
                selectTargetVM.bindDataToView(view.bottom);
                selectTargetVM.showPopAllSelectFriends(view.bottom,
                    LayoutPopSelectedFriendsBinding.inflate(getLayoutInflater()));
                selectTargetVM.submitTap(view.bottom.submit);
            }

            selectTargetVM.metaData.observe(this, v -> {
                if (null != adapter)
                    adapter.notifyDataSetChanged();
            });
        } catch (Exception ignore) {
        }
    }

    private void initView() {
        view.recyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(this) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView,
                                               RecyclerView.State state, int position) {
                LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        protected int calculateTimeForScrolling(int dx) {
                            // 此函数计算滚动dx的距离需要多久，当要滚动的距离很大时，比如说52000，
                            // 经测试，系统会多次调用此函数，每10000距离调一次，所以总的滚动时间
                            // 是多次调用此函数返回的时间的和，所以修改每次调用该函数时返回的时间的
                            // 大小就可以影响滚动需要的总时间，可以直接修改些函数的返回值，也可以修改
                            // dx的值，这里暂定使用后者.
                            // (See LinearSmoothScroller.TARGET_SEEK_SCROLL_DISTANCE_PX)
                            if (dx > 1500) {
                                dx = 1500;
                            }
                            return super.calculateTimeForScrolling(dx);
                        }

                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return mLayoutManager.computeScrollVectorForPosition(targetPosition);
                        }
                    };
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
        });
        adapter = new RecyclerViewAdapter<ExUserInfo, RecyclerView.ViewHolder>() {

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {
                return new ViewHol.ItemViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, ExUserInfo data,
                                   int position) {
                ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                FriendInfo friendInfo = data.userInfo.getFriendInfo();
                itemViewHo.view.avatar.load(friendInfo.getFaceURL());
                itemViewHo.view.nickName.setText(friendInfo.getNickname());

                MultipleChoice target = null;
                if (null!=selectTargetVM && !selectTargetVM.isShareCard()){
                    itemViewHo.view.select.setVisibility(View.VISIBLE);
                    itemViewHo.view.select.setChecked(selectTargetVM
                        .contains(new MultipleChoice(friendInfo.getUserID())));
                    int index = selectTargetVM.metaData.val()
                        .indexOf(new MultipleChoice(friendInfo.getUserID()));

                    if (index != -1) {
                        target = selectTargetVM.metaData.val().get(index);
                        itemViewHo.view.select.setEnabled(target.isEnabled);
                        itemViewHo.view.select.setAlpha(target.isEnabled ? 1f : 0.5f);
                    }
                }
                MultipleChoice finalTarget = target;
                itemViewHo.view.getRoot().setOnClickListener(v -> {
                    if (null != selectTargetVM) {
                        if (selectTargetVM.isShareCard()){
                            selectTargetVM.addMetaData(friendInfo.getUserID(), friendInfo.getNickname(),
                                friendInfo.getFaceURL());

                            finish();
                            Common.finishRoute(Routes.Group.SELECT_TARGET);
                            selectTargetVM.toFinish();
                            return;
                        }
                        if (null != finalTarget && !finalTarget.isEnabled) return;
                        itemViewHo.view.select.setChecked(!itemViewHo.view.select.isChecked());

                        if (itemViewHo.view.select.isChecked()) {
                            MultipleChoice meta = new MultipleChoice(friendInfo.getUserID());
                            meta.name = friendInfo.getNickname();
                            meta.icon = friendInfo.getFaceURL();
                            meta.isSelect = true;
                            selectTargetVM.addDate(meta);
                        } else {
                            selectTargetVM.removeMetaData(friendInfo.getUserID());
                        }
                        return;
                    }

                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                        .withString(Constant.K_ID, friendInfo.getUserID())
                        .navigation(AllFriendActivity.this, 1001);
                });
            }
        };
        view.recyclerView.setAdapter(adapter);
        view.recyclerView.addItemDecoration(new StickyDecoration(this,
            position -> adapter.getItems().get(position).sortLetter));
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) {
            vm.getAllFriend();
        }
    }

//    private ActivityResultLauncher<Intent> searchFriendLauncher =
//        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            try {
//                if (result.getResultCode() != RESULT_OK) return;
//
//                String uid = result.getData().getStringExtra(Constant.K_ID);
//                if (null != selectTargetVM) {
//                    for (ExUserInfo item : adapter.getItems()) {
//                        if (null != item.userInfo && item.userInfo.getUserID().equals(uid)) {
//                            sendChatWindow(item.userInfo.getFriendInfo());
//                            return;
//                        }
//                    }
//                }
//                ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
//                    .withString(Constant.K_ID, uid).navigation();
//            } catch (Exception ignored) {
//
//            }
//        });

    private void listener() {
        view.searchView.setOnClickListener(v -> {
            Postcard postcard = ARouter.getInstance().build(Routes.Contact.SEARCH_FRIENDS_GROUP);
            LogisticsCenter.completion(postcard);
            launcher.launch(new Intent(this, postcard.getDestination())
                .putExtra(Constant.K_RESULT, (Serializable) selectTargetVM.metaData.val())
                .putExtra(Constant.IS_SELECT_FRIEND, true));
        });
        vm.letters.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            StringBuilder letters = new StringBuilder();
            for (String s : v) {
                letters.append(s);
            }
            view.sortView.setLetters(letters.toString());
        });

        vm.exUserInfo.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            List<ExUserInfo> exUserInfos = new ArrayList<>(v);
            adapter.setItems(exUserInfos);
        });

        view.sortView.setOnLetterChangedListener((letter, position) -> {
            for (int i = 0; i < adapter.getItems().size(); i++) {
                ExUserInfo exUserInfo = adapter.getItems().get(i);
                if (exUserInfo.sortLetter.equalsIgnoreCase(letter)) {
                    Common.smoothMoveToPosition(view.recyclerView, i);
                    return;
                }
            }
        });

        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (Common.mShouldScroll && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    Common.mShouldScroll = false;
                    Common. smoothMoveToPosition(view.recyclerView, Common.mToPosition);
                }
            }
        });
    }


}
