package io.openim.android.demo.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityRegisterBinding;
import io.openim.android.ouicore.base.BaseActivity;

public class RegisterActivity extends BaseActivity {


    public static final String IS_PHONE = "isPhone";
    private ActivityRegisterBinding view;
    private boolean isPhone;
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(view.getRoot());

        isPhone = getIntent().getBooleanExtra(IS_PHONE, true);
        initView();
        listener();
    }

    private void listener() {
        view.edt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.protocol.setOnCheckedChangeListener((buttonView, isChecked) -> submitEnabled());
        view.clear.setOnClickListener(v -> view.edt1.setText(""));
    }

    private void initView() {
        view.tips.setText(isPhone?getString(R.string.phone_register):getString(R.string.mail_register));
        view.edt1.setHint(isPhone?getString(R.string.input_phone):getString(R.string.input_mail));
    }

    private void submitEnabled() {
        id = view.edt1.getText().toString();
        view.submit.setEnabled(!id.isEmpty()  && view.protocol.isChecked());
    }
}