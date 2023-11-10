package io.openim.android.demo;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.main.MainActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.UserLogic;

@Route(path = Routes.Main.SPLASH)
public class SplashActivity extends AppCompatActivity {
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        UserLogic userLogic = Easy.find(UserLogic.class);
        if (userLogic.isCacheUser()) {
            userLogic.loginCacheUser();
            startActivity(new Intent(this, MainActivity.class));
        } else
            startActivity(new Intent(this, LoginActivity.class));

        finish();
    }
}
