package io.openim.android.ouicore.utils;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;

public class TransformationUtil extends ImageViewTarget<Bitmap > {
    private ImageView target;

    public TransformationUtil(ImageView view) {
        super(view);
        this.target = view;
    }

    @Override
    protected void setResource(Bitmap resource) {
        if (resource == null) {
            return;
        }
        view.setImageBitmap(resource);
        int width = resource.getWidth();
        int height = resource.getHeight();

        //获取imageView的宽
        int imageViewWidth = view.getWidth();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (imageViewWidth <=0){//修复等比例缩放bug
            imageViewWidth = params.width;
        }
        //计算缩放比例
        float sy = (float) (imageViewWidth * 0.2) / (float) (width * 0.2);
        //计算图片等比例放大后的高
        int imageViewHeight = (int) (height * sy);
        params.height = imageViewHeight;
        target.setLayoutParams(params);
    }
}
