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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;


import org.raphets.roundimageview.RoundImageView;

import io.openim.android.ouicore.R;

public class AvatarImage extends FrameLayout {
    private RoundImageView roundImageView;
    private TextView nameTv;

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

    public RoundImageView getRoundImageView() {
        return roundImageView;
    }


    void init(Context context) {
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.MATCH_PARENT);

        roundImageView = new RoundImageView(context);
        roundImageView.setType(RoundImageView.TYPE_ROUND);
        roundImageView.setCornerRadius(6);
        roundImageView.setScaleType(ImageView.ScaleType.CENTER);
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

    public void load(Object url, boolean isGroup, String name) {
        int resId = isGroup ? R.mipmap.ic_my_group :
            io.openim.android.ouicore.R.mipmap.ic_my_friend;
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


            Glide.with(getContext())
                .load(url)
                .error(resId)
                .centerInside()
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        roundImageView.setImageResource(resId);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<
                        ? super Drawable> transition) {
                        roundImageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        roundImageView.setImageDrawable(null);
                    }
                });

        }
    }
}
