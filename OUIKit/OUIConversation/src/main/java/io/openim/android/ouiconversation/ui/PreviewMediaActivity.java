package io.openim.android.ouiconversation.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.hjq.permissions.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import io.openim.android.ouiconversation.databinding.ActivityPreviewBinding;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.DialogProgressBinding;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.ouicore.widget.PinchImageView;
import io.openim.android.ouicore.widget.ProgressDialog;
import kotlin.Triple;
import okhttp3.ResponseBody;

@Route(path = Routes.Conversation.PREVIEW)
public class PreviewMediaActivity extends BasicActivity<ActivityPreviewBinding> {

    private HasPermissions hasWrite;
    private PreviewMediaVM vm;
    private List<View> guideView = new ArrayList<>();
    private ProgressDialog downloadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasWrite = new HasPermissions(this, Permission.WRITE_EXTERNAL_STORAGE);
        viewBinding(ActivityPreviewBinding.inflate(getLayoutInflater()));
        vm = Easy.find(PreviewMediaVM.class);
        initView();
    }

    private void initView() {
        PreviewMediaVM.MediaData data = vm.mediaData;
        view.pager.setAdapter(new MediaPagerAdapter(Collections.singletonList(data)));
        view.pager.setCurrentItem(0);
        PreviewMediaVM.MediaData mediaData = vm.mediaData;
        view.download.setVisibility((TextUtils.isEmpty(mediaData.mediaUrl) || !mediaData.mediaUrl.startsWith("http")) ? View.GONE : View.VISIBLE);
        view.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Jzvd.releaseAllVideos();
                view.download.setVisibility((TextUtils.isEmpty(data.mediaUrl) || !data.mediaUrl.startsWith("http")) ? View.GONE : View.VISIBLE);
                for (int i = 0; i < guideView.size(); i++) {
                    guideView.get(i).setSelected(i == position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        view.download.setOnClickListener(v -> {
            hasWrite.safeGo(() -> {
                String originUrl = data.mediaUrl;
                ContentResolver contentResolver = BaseApp.inst().getContentResolver();
                downloadDialog = new ProgressDialog(PreviewMediaActivity.this);
                downloadDialog.setInfo(BaseApp.inst().getString(io.openim.android.ouicore.R.string.downloading));
                downloadDialog.setCanceledOnTouchOutside(false);
                downloadDialog.setOnCancelListener(dialog -> Common.isInterruptDownload = true);
                downloadDialog.show();
                Triple<String, String, ContentValues> fileParams = createContentValues(originUrl);
                Uri uri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
                    uri = MediaStore.Files.getContentUri("external");
                else
                    uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                final Uri targetUri = contentResolver.insert(uri, fileParams.getThird());
                Common.downloadFile(originUrl, null, targetUri).subscribe(new NetObserver<Boolean>(this) {
                    @Override
                    public void onSuccess(Boolean isSuccess) {
                        Common.UIHandler.post(() -> {
                            if (isSuccess) {
                                String tips = String.format(getString(io.openim.android.ouicore.R.string.save_custom_dir), fileParams.getSecond());
                                Toast.makeText(BaseApp.inst(), tips, Toast.LENGTH_SHORT).show();
                            } else {
                                String tips = String.format(getString(io.openim.android.ouicore.R.string.save_cancel));
                                Toast.makeText(BaseApp.inst(), tips, Toast.LENGTH_SHORT).show();
                            }
                            releaseDownloadManagerRes();
                        });
                    }

                    @Override
                    protected void onFailure(Throwable e) {
                        Common.UIHandler.post(() -> {
                            Toast.makeText(BaseApp.inst(), getString(io.openim.android.ouicore.R.string.save_failure) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            releaseDownloadManagerRes();
                        });
                    }
                });
            });
        });
    }

    private void releaseDownloadManagerRes() {
        if (downloadDialog != null && downloadDialog.isShowing()) downloadDialog.dismiss();
    }


    /**
     * 构造ContentValues，用于保存文件至媒体库
     * @param originUrl 原文件地址
     * @return 构造好的ContentValues
     */
    private static @NonNull Triple<String, String, ContentValues> createContentValues(String originUrl) {
        ContentValues contentValues = new ContentValues();
        String extension = originUrl.substring(originUrl.lastIndexOf(".") + 1);
        String displayName = "OPIM" + System.currentTimeMillis() + "." + extension;
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, java.net.URLConnection.guessContentTypeFromName(originUrl));
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
        String path = null;
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q) {
            path = Environment.DIRECTORY_DOWNLOADS;
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path);
        } else {
            path = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS;
            contentValues.put(MediaStore.MediaColumns.DATA, path);
        }
        return new Triple<>(
            displayName,
            path,
            contentValues);
    }


    private void addGuideView(List<PreviewMediaVM.MediaData> list, int index) {
        guideView.clear();
        for (int i = 0; i < list.size(); i++) {
            View view = new View(this);
            view.setBackgroundResource(io.openim.android.ouicore.R.drawable.selector_guide_bg);
            view.setSelected(i == index);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(io.openim.android.ouicore.R.dimen.gudieview_width), getResources().getDimensionPixelSize(io.openim.android.ouicore.R.dimen.gudieview_heigh));
            layoutParams.setMargins(10, 0, 0, 0);
            PreviewMediaActivity.this.view.guide.addView(view, layoutParams);
            guideView.add(view);
        }
    }


    private static class MediaPagerAdapter extends PagerAdapter {
        private final List<PreviewMediaVM.MediaData> list = new ArrayList<>();

        public MediaPagerAdapter(List<PreviewMediaVM.MediaData> list) {
            this.list.addAll(list);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {


            PreviewMediaVM.MediaData mediaData = list.get(position);
            if (mediaData.isVideo) {
                JzvdStd std = new JzvdStd(container.getContext());
                std.setVisibility(View.VISIBLE);
                std.setUp(mediaData.mediaUrl, "");
                if (list.size() == 1) {
                    //单个预览 自动播放
                    std.startVideoAfterPreloading();
                }
                std.posterImageView.setScaleType(ImageView.ScaleType.CENTER);
                Glide.with(container.getContext()).load(mediaData.thumbnail).into(std.posterImageView);
                container.addView(std);
                return std;
            } else {
                PinchImageView pinchImageView = new PinchImageView(container.getContext());
                //关闭硬件加速  Canvas: trying to draw too large(*bytes) bitmap
                pinchImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                Glide.with(container.getContext()).load(mediaData.mediaUrl).thumbnail(Glide.with(container.getContext()).load(mediaData.thumbnail)).centerInside().into(pinchImageView);

                pinchImageView.setOnClickListener(v -> ((Activity) v.getContext()).finish());
                container.addView(pinchImageView);
                return pinchImageView;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, @NonNull Object object) {
            return view.equals(object);
        }

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
    protected void recycle() {
        N.clearDispose(this);
        Easy.delete(PreviewMediaVM.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        view.pager.setAdapter(null);
        releaseDownloadManagerRes();
    }
}
