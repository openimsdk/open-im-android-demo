package io.openim.android.ouimoments.ui;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.widget.CustomItemAnimator;
import io.openim.android.ouicore.widget.SpacesItemDecoration;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.adapter.CircleAdapter;
import io.openim.android.ouimoments.bean.CircleItem;
import io.openim.android.ouimoments.bean.CommentConfig;
import io.openim.android.ouimoments.bean.CommentItem;
import io.openim.android.ouimoments.bean.FavortItem;
import io.openim.android.ouimoments.bean.User;
import io.openim.android.ouimoments.databinding.FragmentMomentsHomeBinding;
import io.openim.android.ouimoments.mvp.contract.CircleContract;
import io.openim.android.ouimoments.mvp.presenter.CirclePresenter;
import io.openim.android.ouimoments.utils.CommonUtils;
import io.openim.android.ouimoments.widgets.CommentListView;
import io.openim.android.ouimoments.widgets.TitleBar;

public class CircleFragment extends BaseFragment implements CircleContract.View {

    protected static final String TAG = CircleFragment.class.getSimpleName();
    private CircleAdapter circleAdapter;
    private LinearLayout edittextbody;
    private EditText editText;
    private ImageView sendIv;

    private int screenHeight;
    private int editTextBodyHeight;
    private int currentKeyboardH;
    private int selectCircleItemH;
    private int selectCommentItemOffset;

    private CirclePresenter presenter;
    private CommentConfig commentConfig;
    private SuperRecyclerView recyclerView;
    private RelativeLayout bodyLayout;
    private LinearLayoutManager layoutManager;
    private TitleBar titleBar;

    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private boolean hasStorage = false;
    private FragmentMomentsHomeBinding viewBinding;


    public static CircleFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable(Constant.K_RESULT, user);
        CircleFragment fragment = new CircleFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentMomentsHomeBinding.inflate(inflater);
        initTitle(viewBinding.getRoot());
        initView(viewBinding.getRoot());
        //实现自动下拉刷新功能
        recyclerView.getSwipeToRefresh().post(() -> {
            recyclerView.setRefreshing(true);//执行下拉刷新的动画
            refreshListener.onRefresh();//执行数据加载操作
        });
        return viewBinding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermission();
        presenter = new CirclePresenter(this);
        if (getArguments() != null) {
            presenter.user = (User) getArguments().getSerializable(Constant.K_RESULT);
        }
    }


    private void initPermission() {
        Common.UIHandler.post(() -> {
            hasStorage = AndPermission.hasPermissions(getActivity(), Permission.Group.STORAGE);
            Common.permission(getActivity(), () -> hasStorage = true, hasStorage,
                Permission.Group.STORAGE);
        });
    }

    @Override
    public void onDestroy() {
        if (presenter != null) {
            presenter.recycle();
        }
        super.onDestroy();
    }

    /**
     * 获取 recyclerView 的Y滑动距离
     *
     * @return
     */
    public int getScrolledYDistance() {
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleItem = layoutManager.findViewByPosition(position);
        if (null != firstVisibleItem) {
            int itemHeight = firstVisibleItem.getHeight();
            return (position) * itemHeight - firstVisibleItem.getTop();
        }
        return 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View mainView) {
        bodyLayout = mainView.findViewById(R.id.bodyLayout);
        recyclerView = mainView.findViewById(R.id.superRecyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        SpacesItemDecoration divItemDecoration = new SpacesItemDecoration();
        divItemDecoration.addNotDrawIndex(1);
        recyclerView.addItemDecoration(divItemDecoration);
        recyclerView.getRecyclerView().setItemAnimator(new CustomItemAnimator());
        recyclerView.getMoreProgressView().getLayoutParams().width =
            ViewGroup.LayoutParams.MATCH_PARENT;
        recyclerView.setOnTouchListener((v, event) -> {
            if (edittextbody.getVisibility() == View.VISIBLE) {
                updateEditTextBodyVisible(View.GONE, null);
                return true;
            }
            return false;
        });
        refreshListener = () -> presenter.loadData(CirclePresenter.TYPE_PULLREFRESH);
        recyclerView.setRefreshListener(refreshListener);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int alpha = getScrolledYDistance();
                if (alpha >= 255) {
                    alpha = 255;
                }
                //标题栏渐变
                // a:alpha透明度 r:红 g：绿 b蓝
                //没有透明效果
                // titlebar.setBackgroundColor(Color.rgb(57, 174, 255));
                //透明效果是由参数1决定的，透明范围[0,255]
                // titlebar.setBackgroundColor(Color.argb(alpha, 57, 174, 255));
                titleBar.getBackground().setAlpha(alpha);
                viewBinding.titleBarFl.getBackground().setAlpha(alpha);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    Glide.with(MainActivity.this).resumeRequests();
//                } else {
//                    Glide.with(MainActivity.this).pauseRequests();
//                }
            }
        });
        circleAdapter = new CircleAdapter(getActivity());
        circleAdapter.setCirclePresenter(presenter);
        recyclerView.setAdapter(circleAdapter);

        edittextbody = (LinearLayout) mainView.findViewById(R.id.editTextBodyLl);
        editText = (EditText) mainView.findViewById(R.id.circleEt);
        sendIv = (ImageView) mainView.findViewById(R.id.sendIv);
        sendIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter != null) {
                    //发布评论
                    String content = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(content)) {
                        toast(getString(io.openim.android.ouicore.R.string.coments_null_tips));
                        return;
                    }
                    presenter.addComment(content, commentConfig);
                }
                updateEditTextBodyVisible(View.GONE, null);
            }
        });
        setViewTreeObserver();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {

        super.onHiddenChanged(hidden);
    }

    private void initTitle(View mainView) {
        viewBinding.titleBarFl.setPadding(0,SinkHelper.getStatusBarHeight(getContext()),0,0);
        viewBinding.titleBarFl.setBackgroundColor(Color.parseColor("#FF022234"));
        titleBar = mainView.findViewById(R.id.main_title_bar);
        if (presenter.isSpecifiedUser()) {
            titleBar.setLeftImageResource(com.yzq.zxinglibrary.R.drawable.ic_back);
            titleBar.setTitle(String.format(getString(io.openim.android.ouicore.R.string.to_user_moments), presenter.user.getName()));
            titleBar.setLeftClickListener(v -> getActivity().finish());
        } else titleBar.setTitle(getString(io.openim.android.ouicore.R.string.moments));
        titleBar.setTitleColor(getResources().getColor(R.color.white));
        titleBar.setBackgroundColor(Color.parseColor("#FF022234"));


        if (!presenter.isSpecifiedUser()) {
            titleBar.addAction(new TitleBar.ImageAction(R.drawable.ic_moments_new_message) {
                @Override
                public void performAction(View view) {
                    startActivity(new Intent(getActivity(), MsgDetailActivity.class));
                }
            });
            titleBar.addAction(new TitleBar.ImageAction(R.drawable.ic_plus_add) {
                @Override
                public void performAction(View view) {

                }
            });
        }
    }


    private void setViewTreeObserver() {

        final ViewTreeObserver swipeRefreshLayoutVTO = bodyLayout.getViewTreeObserver();
        swipeRefreshLayoutVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                bodyLayout.getWindowVisibleDisplayFrame(r);
                int statusBarH = getStatusBarHeight();//状态栏高度
                int screenH = bodyLayout.getRootView().getHeight();
                if (r.top != statusBarH) {
                    //在这个demo中r.top代表的是状态栏高度，在沉浸式状态栏时r.top＝0，通过getStatusBarHeight获取状态栏高度
                    r.top = statusBarH;
                }
                int keyboardH = screenH - (r.bottom - r.top);
                Log.d(TAG,
                    "screenH＝ " + screenH + " &keyboardH = " + keyboardH + " &r.bottom=" + r.bottom + " &top=" + r.top + " &statusBarH=" + statusBarH);

                if (keyboardH == currentKeyboardH) {//有变化时才处理，否则会陷入死循环
                    return;
                }

                currentKeyboardH = keyboardH;
                screenHeight = screenH;//应用屏幕的高度
                editTextBodyHeight = edittextbody.getHeight();

                if (keyboardH < 150) {//说明是隐藏键盘的情况
                    updateEditTextBodyVisible(View.GONE, null);
                    return;
                }
                //偏移listview
                if (layoutManager != null && commentConfig != null) {
                    layoutManager.scrollToPositionWithOffset(commentConfig.circlePosition + CircleAdapter.HEADVIEW_SIZE, getListviewOffset(commentConfig));
                }
            }
        });
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (edittextbody != null && edittextbody.getVisibility() == View.VISIBLE) {
                updateEditTextBodyVisible(View.GONE, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateAdapterIndex(int index) {
        circleAdapter.notifyItemChanged(index);
    }

    @Override
    public void update2DeleteCircle(String circleId) {
        List<CircleItem> circleItems = circleAdapter.getDatas();
        for (int i = 0; i < circleItems.size(); i++) {
            if (circleId.equals(circleItems.get(i).getId())) {
                circleItems.remove(i);
                circleAdapter.notifyItemRemoved(i + 1);
                return;
            }
        }
    }

    @Override
    public void update2AddFavorite(int circlePosition, FavortItem addItem) {
        if (addItem != null) {
            CircleItem item = (CircleItem) circleAdapter.getDatas().get(circlePosition);
            item.getFavorters().add(addItem);
            circleAdapter.notifyItemChanged(circlePosition + 1);
        }
    }

    @Override
    public void update2DeleteFavort(int circlePosition, String favortId) {
        CircleItem item = (CircleItem) circleAdapter.getDatas().get(circlePosition);
        List<FavortItem> items = item.getFavorters();
        for (int i = 0; i < items.size(); i++) {
            if (favortId.equals(items.get(i).getId())) {
                items.remove(i);
                circleAdapter.notifyItemChanged(circlePosition + 1);
                return;
            }
        }
    }

    @Override
    public void update2AddComment(int circlePosition, CommentItem addItem) {
        if (addItem != null) {
            CircleItem item = (CircleItem) circleAdapter.getDatas().get(circlePosition);
            item.getComments().add(addItem);
            circleAdapter.notifyItemChanged(circlePosition + 1);
        }
        //清空评论文本
        editText.setText("");
    }

    @Override
    public void update2DeleteComment(int circlePosition, String commentId) {
        CircleItem item = (CircleItem) circleAdapter.getDatas().get(circlePosition);
        List<CommentItem> items = item.getComments();
        for (int i = 0; i < items.size(); i++) {
            if (commentId.equals(items.get(i).getId())) {
                items.remove(i);
                circleAdapter.notifyItemChanged(circlePosition + 1);
                return;
            }
        }
    }

    @Override
    public void updateEditTextBodyVisible(int visibility, CommentConfig commentConfig) {
        this.commentConfig = commentConfig;
        edittextbody.setVisibility(visibility);

        measureCircleItemHighAndCommentItemOffset(commentConfig);

        if (View.VISIBLE == visibility) {
            editText.requestFocus();
            //弹出键盘
            CommonUtils.showSoftInput(editText.getContext(), editText);

        } else if (View.GONE == visibility) {
            //隐藏键盘
            CommonUtils.hideSoftInput(editText.getContext(), editText);
        }
    }

    @Override
    public void update2loadData(int loadType, List<CircleItem> datas) {
        if (loadType == CirclePresenter.TYPE_PULLREFRESH) {
            recyclerView.setRefreshing(false);
            circleAdapter.setDatas(datas);
        } else if (loadType == CirclePresenter.TYPE_UPLOADREFRESH) {
            circleAdapter.getDatas().addAll(datas);
        }
        circleAdapter.notifyDataSetChanged();

        if (datas.size() < CirclePresenter.pageSize) {
            recyclerView.removeMoreListener();
            recyclerView.hideMoreProgress();
        } else {
            recyclerView.setupMoreListener((overallItemsCount, itemsBeforeMore,
                                            maxLastVisiblePosition) -> presenter.loadData(CirclePresenter.TYPE_UPLOADREFRESH), 1);
        }

    }


    /**
     * 测量偏移量
     *
     * @param commentConfig
     * @return
     */
    private int getListviewOffset(CommentConfig commentConfig) {
        if (commentConfig == null) return 0;
        //这里如果你的listview上面还有其它占高度的控件，则需要减去该控件高度，listview的headview除外。
        //int listviewOffset = mScreenHeight - mSelectCircleItemH - mCurrentKeyboardH -
        // mEditTextBodyHeight;
        int listviewOffset =
            screenHeight - selectCircleItemH - currentKeyboardH - editTextBodyHeight;
        if (commentConfig.commentType == CommentConfig.Type.REPLY) {
            //回复评论的情况
            listviewOffset = listviewOffset + selectCommentItemOffset;
        }
        Log.i(TAG, "listviewOffset : " + listviewOffset);
        return listviewOffset;
    }

    private void measureCircleItemHighAndCommentItemOffset(CommentConfig commentConfig) {
        if (commentConfig == null) return;

        int firstPosition = layoutManager.findFirstVisibleItemPosition();
        //只能返回当前可见区域（列表可滚动）的子项
        View selectCircleItem =
            layoutManager.getChildAt(commentConfig.circlePosition + CircleAdapter.HEADVIEW_SIZE - firstPosition);

        if (selectCircleItem != null) {
            selectCircleItemH = selectCircleItem.getHeight();
        }

        if (commentConfig.commentType == CommentConfig.Type.REPLY) {
            //回复评论的情况
            CommentListView commentLv =
                (CommentListView) selectCircleItem.findViewById(R.id.commentList);
            if (commentLv != null) {
                //找到要回复的评论view,计算出该view距离所属动态底部的距离
                View selectCommentItem = commentLv.getChildAt(commentConfig.commentPosition);
                if (selectCommentItem != null) {
                    //选择的commentItem距选择的CircleItem底部的距离
                    selectCommentItemOffset = 0;
                    View parentView = selectCommentItem;
                    do {
                        int subItemBottom = parentView.getBottom();
                        parentView = (View) parentView.getParent();
                        if (parentView != null) {
                            selectCommentItemOffset += (parentView.getHeight() - subItemBottom);
                        }
                    } while (parentView != null && parentView != selectCircleItem);
                }
            }
        }
    }


    @Override
    public void showLoading(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String errorMsg) {
        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
    }


}
