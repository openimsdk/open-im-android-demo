package io.openim.android.ouiconversation.ui;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;

import cn.jzvd.Jzvd;
import io.openim.android.ouiconversation.databinding.ActivityPreviewBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.Routes;

@Route(path = Routes.Conversation.PREVIEW)
public class PreviewActivity extends BaseActivity<BaseViewModel, ActivityPreviewBinding> {


    public static final String MEDIA_URL = "media_url";
    public static final String FIRST_FRAME = "first_frame";
    private boolean hasWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runOnUiThread(() -> hasWrite = AndPermission.hasPermissions(this,
            Permission.WRITE_EXTERNAL_STORAGE));
        bindViewDataBinding(ActivityPreviewBinding.inflate(getLayoutInflater()));
        initView();
    }

    @Override
    protected void setLightStatus() {

    }

    private void initView() {
        String url = getIntent().getStringExtra(MEDIA_URL);
        String firstFrame = getIntent().getStringExtra(FIRST_FRAME);
        if (TextUtils.isEmpty(url)) return;

        if (MediaFileUtil.isImageType(url)) {
            view.pic.setVisibility(View.VISIBLE);
            view.download.setVisibility(View.VISIBLE);
            Glide.with(this).load(url).centerInside().into(view.pic);
            view.pic.setOnClickListener(v -> finish());
            view.download.setOnClickListener(v -> {
                Common.permission(this, () -> {
                    hasWrite = true;
                    toast(getString(io.openim.android.ouicore.R.string.start_download));
                    Common.downloadFile(url, null,
                        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            new ContentValues())).subscribe(new NetObserver<Boolean>(this) {
                        @Override
                        public void onSuccess(Boolean success) {
                            if (success)
                                toast(getString(io.openim.android.ouicore.R.string.save_succ));
                            else
                                toast(getString(io.openim.android.ouicore.R.string.save_photo_album));
                        }

                        @Override
                        protected void onFailure(Throwable e) {
                            toast(e.getMessage());
                        }
                    });
                }, hasWrite, Permission.WRITE_EXTERNAL_STORAGE);

            });
        } else if (MediaFileUtil.isVideoType(url)) {
            view.jzVideo.setVisibility(View.VISIBLE);
            view.jzVideo.setUp(url, "");
            view.jzVideo.startVideoAfterPreloading();

            view.jzVideo.posterImageView.setScaleType(ImageView.ScaleType.CENTER);
            Glide.with(this).load(firstFrame).into(view.jzVideo.posterImageView);
        }

    }

    public static ContentValues getImageContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "image/jpeg");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("orientation", Integer.valueOf(0));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        N.clearDispose(this);
    }
}
