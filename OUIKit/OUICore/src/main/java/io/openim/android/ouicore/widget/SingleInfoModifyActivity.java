package io.openim.android.ouicore.widget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.databinding.ActivitySingleModifyBinding;
import io.openim.android.ouicore.utils.Common;

public class SingleInfoModifyActivity extends BaseActivity<BaseViewModel, ActivitySingleModifyBinding> {
    public static final String SINGLE_INFO_MODIFY_DATA = "single_info_modify_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySingleModifyBinding.inflate(getLayoutInflater()));
        sink();
        SingleInfoModifyData singleInfoModifyData = (SingleInfoModifyData) getIntent().getSerializableExtra(SINGLE_INFO_MODIFY_DATA);
        view.title.setText(singleInfoModifyData.title);
        view.description.setText(singleInfoModifyData.description);
        view.avatar.load(singleInfoModifyData.avatarUrl);
        view.editText.setText(singleInfoModifyData.editT);
        view.editText.post(() -> {
            view.editText.requestFocus();
            view.editText.setSelection(view.editText
                .getText().length());
        });
    }

    public void complete(View v) {
        String input=view.editText.getText().toString();
        if (Common.isBlank(input)){
            toast(getString(R.string.empty_prompt));
            return;
        }
        setResult(RESULT_OK, new Intent().putExtra(SINGLE_INFO_MODIFY_DATA, input));
        finish();
    }

    public static class SingleInfoModifyData implements Serializable {
        public String title;
        public String description;
        public String avatarUrl;
        public String editT;
    }
}
