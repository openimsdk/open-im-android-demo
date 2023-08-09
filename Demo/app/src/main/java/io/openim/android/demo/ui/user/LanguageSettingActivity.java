package io.openim.android.demo.ui.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Locale;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityLanguageSettingBinding;
import io.openim.android.demo.ui.main.MainActivity;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.LanguageUtil;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;

public class LanguageSettingActivity extends BaseActivity<BaseViewModel,
    ActivityLanguageSettingBinding> {

    private Locale checkLocale;
    private ViewGroup[] views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityLanguageSettingBinding.inflate(getLayoutInflater()));

        views = new ViewGroup[]{view.withSystem, view.chinese, view.english};
        initView();
        click();
    }

    private void initView() {
        String lang = SharedPreferencesUtil.get(this).getString(Constant.K_LANGUAGE_SP);
        if (TextUtils.isEmpty(lang)) {
            selectTab(view.withSystem);
        } else if (lang.equals(Locale.CHINA.getLanguage())) {
            selectTab(view.chinese);
        } else {
            selectTab(view.english);
        }
    }

    private void click() {

        view.withSystem.setOnClickListener(this::selectTab);
        view.chinese.setOnClickListener(this::selectTab);
        view.english.setOnClickListener(this::selectTab);

        view.submit.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            String tips =
                getString(io.openim.android.ouicore.R.string.switch_language_tips) + getString(null == checkLocale ? io.openim.android.ouicore.R.string.with_system : checkLocale == Locale.CHINA ? io.openim.android.ouicore.R.string.chinese : io.openim.android.ouicore.R.string.english);

            commonDialog.getMainView().tips.setText(tips);
            commonDialog.getMainView().cancel.setOnClickListener(v1 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(v1 -> {
                String lang = null == checkLocale ? "" : checkLocale.getLanguage();
                LanguageUtil.switchLanguage(lang, this, MainActivity.class);
            });
            commonDialog.show();

        });
    }

    private void selectTab(View v) {
        for (int i = 0; i < views.length; i++) {
            ViewGroup viewGroup = views[i];
            for (int a = 0; a < viewGroup.getChildCount(); a++) {
                View view = viewGroup.getChildAt(a);
                if (view instanceof ImageView) {
                    boolean isCheck = v == viewGroup;
                    view.setVisibility(isCheck ? View.VISIBLE : View.INVISIBLE);
                    if (isCheck)
                        checkLocale = i == 0 ? null : i == 1 ? Locale.CHINA : Locale.ENGLISH;
                }
            }
        }
    }
}
