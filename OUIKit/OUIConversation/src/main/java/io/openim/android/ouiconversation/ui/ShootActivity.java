package io.openim.android.ouiconversation.ui;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityShootBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.utils.Constant;

public class ShootActivity extends BaseActivity<ChatVM, ActivityShootBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityShootBinding.inflate(getLayoutInflater()));

        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }

        view.cameraView.setSaveVideoPath(Constant.VIDEODIR);

        view.cameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                Toast.makeText(ShootActivity.this, io.openim.android.ouicore.R.string.camera_punch_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(ShootActivity.this, io.openim.android.ouicore.R.string.camera_permission_failed, Toast.LENGTH_SHORT).show();
            }
        });

        view.cameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                close(saveToFile(bitmap), null);
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                String firstFrameUrl = saveToFile(firstFrame);
                close(url, firstFrameUrl);
            }
        });
        view.cameraView.setLeftClickListener(() -> finish());
    }

    private String saveToFile(Bitmap bitmap) {
        try {
            String fName = UUID.randomUUID().toString();
            File dir = new File(Constant.PICTUREDIR);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(Constant.PICTUREDIR + fName + ".jpg");
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            bitmap.recycle();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    private void close(String fileUrl, String firstFrameUrl) {
        Intent intent = new Intent().putExtra("fileUrl", fileUrl);
        if (!TextUtils.isEmpty(firstFrameUrl))
            intent.putExtra("firstFrameUrl", firstFrameUrl);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        view.cameraView.onPause();
    }
}
