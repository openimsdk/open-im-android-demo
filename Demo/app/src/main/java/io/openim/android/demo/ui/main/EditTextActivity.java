package io.openim.android.demo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;

import io.openim.android.demo.databinding.ActivityEditTextBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.OnDedrepClickListener;

public class EditTextActivity extends BaseActivity<BaseViewModel, ActivityEditTextBinding> {

    public static final String TITLE = "title";
    public static final String INIT_TXT = "init_txt";
    public static final String MAX_LENGTH = "max_length";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityEditTextBinding.inflate(getLayoutInflater()));
        sink();
        initView();
    }

    private void initView() {
        view.title.setText(getIntent().getStringExtra(TITLE));
        view.edit.setText(getIntent().getStringExtra(INIT_TXT));
        int maxLength=getIntent().getIntExtra(MAX_LENGTH,-1);
        if (maxLength!=-1){
            view.edit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });
        }
        view.save.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                setResult(RESULT_OK, new Intent().putExtra(Constants.K_RESULT,
                    view.edit.getText().toString()));
                finish();
            }
        });
    }
}
