package io.openim.android.ouicore.widget;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import io.openim.android.ouicore.R;

public class PlaceHolderDrawable extends CircularProgressDrawable {
    /**
     * @param context application context
     */
    public PlaceHolderDrawable(Context context) {
        super(context);
        setColorSchemeColors(getColorById(context, R.color.def_bg), getColorById(context, R.color.img_bg), getColorById(context, R.color.gray));
        setCenterRadius(30f);
        setStrokeWidth(5f);
        start();
    }

    @ColorInt
    private int getColorById(Context context, @ColorRes int id) {
        return ResourcesCompat.getColor(context.getResources(), id, null);
    }
}
