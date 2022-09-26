package io.openim.android.ouicore.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.databinding.LayoutSearchBinding;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.utils.L;

public class SearchView extends LinearLayout {
    boolean clickable = true, isClear;
    String hint;

    public SearchView(Context context) {
        super(context);
        initView();
    }

    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.customize);
        try {
            clickable = array.getBoolean(R.styleable.customize_android_clickable, true);
            hint = array.getString(R.styleable.customize_android_hint);
            isClear = array.getBoolean(R.styleable.customize_isClear, false);
        } finally {
            array.recycle();
        }
        initView();
    }

    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    EditText editText;

    private void initView() {
        setBackground(getResources().getDrawable(R.drawable.sty_radius_4_f0f0f0));
        inflate(getContext(), R.layout.layout_search, this);
        editText = findViewById(R.id.editText);
        if (null != hint)
            editText.setHint(hint);
        ImageView imageView = findViewById(R.id.clearIv);
        imageView.setVisibility(isClear ? VISIBLE : GONE);
        imageView.setOnClickListener(this::onClick);
    }

    public EditText getEditText() {
        return editText;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (clickable)
            return super.onInterceptTouchEvent(ev);
        else
            return true;
    }

    private void onClick(View v) {
        editText.setText("");
    }
}
