package io.openim.android.ouicore.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;


import io.openim.android.ouicore.R;
import io.openim.android.ouicore.utils.Common;

public class AvatarImage extends RoundImageView {
    public AvatarImage(@NonNull Context context) {
        super(context);
    }

    public AvatarImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void load(Object url) {
        load(url, false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void load(Object url, boolean isGroup) {
        if (null == url || (url instanceof String && (String.valueOf(url).isEmpty()
            || String.valueOf(url).contains("ic_avatar")))) {
            setImageDrawable(getContext().getDrawable(isGroup ? R.mipmap.ic_my_group : io.openim.android.ouicore.R.mipmap.ic_my_friend));
        } else {
            setScaleType(ScaleType.CENTER);
            Glide.with(getContext())
                .load(url)
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                .into(this);
        }
    }
}
