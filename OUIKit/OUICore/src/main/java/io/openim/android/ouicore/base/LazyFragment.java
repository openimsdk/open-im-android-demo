package io.openim.android.ouicore.base;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class LazyFragment extends Fragment {
    //判断控件是否加载完毕
    private boolean isCreateView = false;
    //判断是否已加载过数据
    public boolean isLoadData = false;

    private Object tagData;

    public Object getTagData() {
        return tagData;
    }

    public void setTagData(Object tagData) {
        this.tagData = tagData;
    }

    /**
     * 初始化控件
     */
    public abstract View initViews(LayoutInflater inflater, ViewGroup container);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container);
    }

    private View createView(LayoutInflater inflater, ViewGroup container) {
        View view = initViews(inflater, container);
        isCreateView = true;
        return view;
    }


    //注意，此方法再所有生命周期之前调用，不可操作控件
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isCreateView && !isLoadData) {
            load();
        }
    }


    /**
     * 加载数据
     */
    private void load() {
        //如果没有加载过就加载，否则就不再加载了
        if (!isLoadData) {
            //加载数据操作
            loadData();
            isLoadData = true;
        }
    }

    public abstract void loadData();

    // 第一次进入ViewPager的时候我们需要直接加载，因为此时setUserVisibleHint 已经调用过了。
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getUserVisibleHint()) load();
    }
}
