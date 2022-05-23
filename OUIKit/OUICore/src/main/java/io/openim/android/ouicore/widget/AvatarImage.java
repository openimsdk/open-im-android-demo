package io.openim.android.ouicore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class AvatarImage extends androidx.appcompat.widget.AppCompatImageView {
    public AvatarImage(@NonNull Context context) {
        super(context);
    }

    public AvatarImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void load(String url) {
        if (null == url || url.isEmpty() || url.contains("ic_avatar")) {
            setImageDrawable(getContext().getDrawable(io.openim.android.ouicore.R.mipmap.ic_my_friend));
        } else {
            Glide.with(getContext())
                    .load(url)
                    .centerCrop()
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(this);
        }
    }
}
