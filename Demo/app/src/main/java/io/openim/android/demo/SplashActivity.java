package io.openim.android.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.main.MainActivity;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginCertificate loginCertificate = LoginCertificate.getCache(this);
        if (null == loginCertificate)
            startActivity(new Intent(this, LoginActivity.class));
        else
            startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
