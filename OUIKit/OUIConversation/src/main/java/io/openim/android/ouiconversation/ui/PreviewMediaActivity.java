package io.openim.android.ouiconversation.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jzvd.JZTextureView;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityPreviewBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.PermissionVM;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.ouicore.widget.PinchImageView;

@Route(path = Routes.Conversation.PREVIEW)
public class PreviewMediaActivity extends BasicActivity<ActivityPreviewBinding> {

    private boolean hasWrite;
    private PreviewMediaVM vm;
    private List<View> guideView = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.UIHandler.postDelayed(() -> hasWrite = AndPermission.hasPermissions(this,
            Permission.WRITE_EXTERNAL_STORAGE), 300);
        viewBinding(ActivityPreviewBinding.inflate(getLayoutInflater()));
        vm = Easy.find(PreviewMediaVM.class);
        initView();
    }

    private void initView() {
        view.pager.setAdapter(new MediaPagerAdapter());
        view.pager.setCurrentItem(vm.currentIndex);
        view.download.setVisibility(vm.mediaDataList.get(vm.currentIndex).isVideo ? View.GONE :
            View.VISIBLE);
        view.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Jzvd.releaseAllVideos();
                view.download.setVisibility(vm.mediaDataList.get(position).isVideo ? View.GONE :
                    View.VISIBLE);
                for (int i = 0; i < guideView.size(); i++) {
                    guideView.get(i).setSelected(i == position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (vm.mediaDataList.size() == 1)
            view.guide.setVisibility(View.GONE);
        else addGuideView();

        view.download.setOnClickListener(v -> {
            Common.permission(this, () -> {
                hasWrite = true;
                toast(getString(io.openim.android.ouicore.R.string.start_download));
                Common.downloadFile(vm.mediaDataList.get(view.pager.getCurrentItem()).mediaUrl,
                    null,
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new ContentValues())).subscribe(new NetObserver<Boolean>(this) {
                    @Override
                    public void onSuccess(Boolean success) {
                        if (success)
                            toast(getString(io.openim.android.ouicore.R.string.save_photo_album));
                        else
                            toast(getString(io.openim.android.ouicore.R.string
                                .save_failure));
                    }

                    @Override
                    protected void onFailure(Throwable e) {
                        toast(e.getMessage());
                    }
                });
            }, hasWrite, Permission.WRITE_EXTERNAL_STORAGE);
        });
    }

    private void addGuideView() {
        guideView.clear();
        for (int i = 0; i < vm.mediaDataList.size(); i++) {
            View view = new View(this);
            view.setBackgroundResource(io.openim.android.ouicore.R.drawable.selector_guide_bg);
            view.setSelected(i == vm.currentIndex);
            LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(io.openim.android.ouicore.R.dimen.gudieview_width), getResources().getDimensionPixelSize(io.openim.android.ouicore.R.dimen.gudieview_heigh));
            layoutParams.setMargins(10, 0, 0, 0);
            PreviewMediaActivity.this.view.guide.addView(view, layoutParams);
            guideView.add(view);
        }
    }


    private static class MediaPagerAdapter extends PagerAdapter {

        private final PreviewMediaVM pvm;

        public MediaPagerAdapter() {
            pvm = Easy.find(PreviewMediaVM.class);
        }

        @Override
        public int getCount() {
            return pvm.mediaDataList.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {


            PreviewMediaVM.MediaData mediaData = pvm.mediaDataList.get(position);
            if (mediaData.isVideo) {
                JzvdStd std = new JzvdStd(container.getContext());
                std.setVisibility(View.VISIBLE);
                std.setUp(mediaData.mediaUrl, "");
                if (pvm.mediaDataList.size()==1){
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
                Glide.with(container.getContext()).load(mediaData.mediaUrl)
                    .thumbnail(Glide.with(container.getContext())
                        .load(mediaData.thumbnail))
                    .centerInside()
                    .into(pinchImageView);

                pinchImageView.setOnClickListener(v -> ((Activity)v.getContext()).finish());
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
    }
}
