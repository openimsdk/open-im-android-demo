package io.openim.android.ouimoments.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.List;

import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.adapter.CircleAdapter;
import io.openim.android.ouimoments.adapter.viewholder.CircleViewHolder;
import io.openim.android.ouimoments.bean.CircleItem;
import io.openim.android.ouimoments.databinding.FragmentMomentsHomeBinding;

public class MomentsDetailFragment extends CircleFragment {
    private String momentID;

    public static MomentsDetailFragment newInstance(String mID) {
        Bundle args = new Bundle();
        args.putString(Constant.K_ID, mID);
        MomentsDetailFragment fragment = new MomentsDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMomentsHomeBinding viewBinding = FragmentMomentsHomeBinding.inflate(inflater);
        initView(viewBinding.getRoot());
        return viewBinding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        momentID = getArguments().getString(Constant.K_ID);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initTitle(View mainView) {

    }

    @Override
    public void initView(View mainView) {
        super.initView(mainView);
        recyclerView.getSwipeToRefresh().setEnabled(false);
//        mainView.findViewById(R.id.titleBarFl).setVisibility(View.GONE);

        try {
            presenter.getMomentsDetail(momentID);
        } catch (Exception e) {
        }

    }

    @Override
    public void createAdapter() {
        circleAdapter = new ExCircleAdapter(getContext());
        circleAdapter.setCirclePresenter(presenter);
        recyclerView.setAdapter(circleAdapter);
    }

    @Override
    public void update2loadData(int loadType, List<CircleItem> datas) {
        circleAdapter.setDatas(datas);
        circleAdapter.notifyDataSetChanged();
    }

    public static  class ExCircleAdapter extends CircleAdapter {

        public ExCircleAdapter(Context context) {
            super(context);
            HEADVIEW_SIZE = 0;
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {
            int itemType = 0;
            CircleItem item = (CircleItem) datas.get(position);
            if (CircleItem.TYPE_URL.equals(item.getType())) {
                itemType = CircleViewHolder.TYPE_URL;
            } else if (CircleItem.TYPE_IMG.equals(item.getType())) {
                itemType = CircleViewHolder.TYPE_IMAGE;
            } else if (CircleItem.TYPE_VIDEO.equals(item.getType())) {
                itemType = CircleViewHolder.TYPE_VIDEO;
            }
            return itemType;
        }
    }
}
