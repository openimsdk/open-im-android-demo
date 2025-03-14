package io.openim.android.ouicore.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AlphaBtn extends androidx.appcompat.widget.AppCompatButton {

    public AlphaBtn(@NonNull Context context) {
        super(context);
    }

    public AlphaBtn(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getEnabledFromAttributeSet(attrs);

    }

    public AlphaBtn(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getEnabledFromAttributeSet(attrs);
    }

    private void getEnabledFromAttributeSet(AttributeSet attrs) {
        // 默认为 true，即可用状态
        boolean isEnabled = true;

        // 在 AttributeSet 中查找 android:enabled 属性
        if (attrs != null) {
            // 获取 android:enabled 属性的值，默认为 true
            isEnabled = attrs.getAttributeBooleanValue("http://schemas.android" +
                ".com/apk/res/android", "enabled", true);
        }

        setAlpha(isEnabled ? 1f : getAlphaFromAttributeSet(attrs));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    private float getAlphaFromAttributeSet(AttributeSet attrs) {
        // 获取透明度属性的值，默认为1.0f（完全不透明）
        float alpha = 1.0f;

        // 在 AttributeSet 中查找 android:alpha 属性
        if (attrs != null) {
            int alphaAttribute = attrs.getAttributeResourceValue("http://schemas.android" +
                ".com/apk/res/android", "alpha", -1);

            // 如果 android:alpha 不是资源引用，直接获取 float 值
            if (alphaAttribute == -1) {
                alpha = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res/android"
                    , "alpha", 1.0f);
            } else {
                // 如果 android:alpha 是资源引用，需要通过 Resources 获取 float 值
                Resources.Theme theme = getContext().getTheme();
                TypedValue typedValue = new TypedValue();
                theme.resolveAttribute(alphaAttribute, typedValue, true);
                alpha = typedValue.getFloat();
            }
        }
        return alpha;
    }
}
