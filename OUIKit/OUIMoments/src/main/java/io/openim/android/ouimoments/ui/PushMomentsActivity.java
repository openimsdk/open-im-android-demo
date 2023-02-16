package io.openim.android.ouimoments.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.SpacesItemDecoration;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.databinding.ActivityPushMomentsBinding;
import io.openim.android.ouimoments.databinding.ItemRoundImgBinding;
import io.openim.android.ouimoments.mvp.presenter.PushMomentsVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;

public class PushMomentsActivity extends BaseActivity<PushMomentsVM, ActivityPushMomentsBinding> {

    private RecyclerViewAdapter<Object, RoundIMGItem> adapter;
    private PhotographAlbumDialog albumDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PushMomentsVM.class,true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPushMomentsBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        listener();
        vm.init();
    }

    private void listener() {
        view.whoSeeLy.setOnClickListener(v -> {
            startActivity(new Intent(this,WhoSeeActivity.class));
        });
        view.reminderWhoLy.setOnClickListener(v -> {
            startActivity(new Intent(this,WhoSeeActivity.class));
        });
        vm.selectMedia.observe(this, objects -> {
            adapter.setItems(objects);
        });
        view.content
            .addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    view.restriction.setText(s.length()+"/500");
                }
            });
    }

    private void initView() {
        albumDialog = new PhotographAlbumDialog(PushMomentsActivity.this);
        view.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        view.recyclerView.setAdapter(adapter =
            new RecyclerViewAdapter<Object, RoundIMGItem>(RoundIMGItem.class) {

            @Override
            public void onBindView(@NonNull RoundIMGItem holder, Object data, int position) {
                if (data instanceof Integer) {
                    //表示添加按钮
                    holder.view.roundImageView.setOnClickListener(v -> {
                        albumDialog.show();
                        albumDialog.setMaxSelectable(9);
                        albumDialog.setToCrop(false);
                        albumDialog.setOnSelectResultListener(paths -> {
                            albumDialog.dismiss();
                            for (String path : paths) {
                                vm.addRes(path);
                            }
                        });
                    });
                    holder.view.delete.setVisibility(View.INVISIBLE);
                } else {
                    holder.view.delete.setVisibility(View.VISIBLE);
                    holder.view.delete.setOnClickListener(v -> {
                        vm.removeRes(position);
                    });
                    holder.view.roundImageView.setOnClickListener(v -> {
                        List<String> photoUrls = new ArrayList<>();
                        for (Object o : vm.selectMedia.getValue()) {
                            if (o instanceof String) photoUrls.add((String) o);
                        }
                        ImagePagerActivity.startImagePagerActivity(PushMomentsActivity.this,
                            photoUrls, position, null);
                    });
                }
                Glide.with(PushMomentsActivity.this).load(data).centerInside().into(holder.view.roundImageView);
            }
        });
        adapter.setItems(vm.selectMedia.getValue());
    }

    public static class RoundIMGItem extends RecyclerView.ViewHolder {

        private final ItemRoundImgBinding view;

        public RoundIMGItem(@NonNull View itemView) {
            super(ItemRoundImgBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = ItemRoundImgBinding.bind(this.itemView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }

}
