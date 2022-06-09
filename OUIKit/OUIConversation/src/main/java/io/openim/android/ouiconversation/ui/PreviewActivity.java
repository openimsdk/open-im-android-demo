package io.openim.android.ouiconversation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;

import io.openim.android.ouiconversation.databinding.ActivityPreviewBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.SinkHelper;

public class PreviewActivity extends BaseActivity<BaseViewModel, ActivityPreviewBinding> {


    public static final String MEDIA_URL = "media_url";
    public static final String FIRST_FRAME = "first_frame";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPreviewBinding.inflate(getLayoutInflater()));
        SinkHelper.get(this).setTranslucentStatus(null);
        initView();
    }

    private void initView() {
        String url = getIntent().getStringExtra(MEDIA_URL);
        String firstFrame = getIntent().getStringExtra(FIRST_FRAME);
        if (TextUtils.isEmpty(url)) return;

        if (MediaFileUtil.isImageType(url)) {
            view.pic.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(url)
                .into(view.pic);
            view.pic.setOnClickListener(v -> finish());
        } else if (MediaFileUtil.isVideoType(url)) {
            view.jzVideo.setVisibility(View.VISIBLE);
            view.jzVideo.setUp(url, "");
            Glide.with(this)
                .load(firstFrame)
                .into(view.jzVideo.posterImageView);
        }

    }
}
