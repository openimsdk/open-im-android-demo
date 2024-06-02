package io.openim.android.ouicore.widget;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;


import org.raphets.roundimageview.RoundImageView;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;

public class AvatarImage extends FrameLayout {
    private ImageView roundImageView;
    private TextView nameTv;
    private int resId=-1;

    public AvatarImage(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AvatarImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AvatarImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ImageView getRoundImageView() {
        return roundImageView;
    }


    void init(Context context) {
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.MATCH_PARENT);

        roundImageView=new ImageView(context);
        roundImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(roundImageView,params);
        nameTv = new TextView(context);
        nameTv.setTextColor(Color.WHITE);
        nameTv.setTextSize(14);
        nameTv.setGravity(Gravity.CENTER);
        addView(nameTv, params);
    }
    public void load(Object url) {
        load(url, false, null);
    }
    public void load(Object url, String name) {
        load(url, false, name);
    }
    public void load(Object url, boolean isGroup) {
        load(url, isGroup, null);
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public void load(Object url, boolean isGroup, String name) {
        roundImageView.setImageDrawable(null);
        setBackground(null);

        if (resId == -1) {
            resId = isGroup ? R.mipmap.ic_my_group :
               R.mipmap.ic_my_friend;
        }
        roundImageView.setVisibility(GONE);
        nameTv.setVisibility(GONE);
        if (null == url || (url instanceof String && (String.valueOf(url).isEmpty()
            || String.valueOf(url).contains("ic_avatar")))) {
            if (TextUtils.isEmpty(name)) {
                roundImageView.setVisibility(VISIBLE);
                roundImageView.setImageDrawable(AppCompatResources.getDrawable(getContext(),
                    resId));
            } else {
                nameTv.setVisibility(VISIBLE);
                name=name.substring(0,1);
                setBackground(AppCompatResources.getDrawable(getContext(),
                    R.drawable.sty_radius_6_ff0089ff));
                nameTv.setVisibility(VISIBLE);
                nameTv.setText(name);
            }
        } else {
            roundImageView.setVisibility(VISIBLE);

            Glide.with(BaseApp.inst())
                .load(url)
                .error(resId)
                .centerInside()
                .transform(new RoundedCorners(12))
                .into(roundImageView);
        }
    }
}
