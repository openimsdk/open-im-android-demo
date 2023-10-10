package io.openim.android.ouiconversation.ui;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.openim.android.ouiconversation.databinding.ActivityShootBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.Routes;

@Route(path = Routes.Conversation.SHOOT)
public class ShootActivity extends BaseActivity<BaseViewModel, ActivityShootBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityShootBinding.inflate(getLayoutInflater()));
        Common.setFullScreen(this);
        int status = getIntent().getIntExtra(Constant.K_RESULT, JCameraView.BUTTON_STATE_BOTH);
        view.cameraView.setSaveVideoPath(Constant.VIDEO_DIR);
        view.cameraView.setFeatures(status);
        view.cameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                Toast.makeText(ShootActivity.this,
                    io.openim.android.ouicore.R.string.camera_punch_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(ShootActivity.this,
                    io.openim.android.ouicore.R.string.camera_permission_failed,
                    Toast.LENGTH_SHORT).show();
            }
        });

        view.cameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                close(saveToAlbum(bitmap), null);
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                String firstFrameUrl = saveToLocalFile(firstFrame);
                close(url, firstFrameUrl);
            }
        });
        view.cameraView.setLeftClickListener(() -> finish());
    }


    public InputStream bitmap2InputStream(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private String saveToAlbum(Bitmap bitmap) {
        //插入相册并返回路径
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            new ContentValues());//
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = BaseApp.inst().getContentResolver().openOutputStream(uri
                , "rw");
            byte[] fileReader = new byte[4096];
            inputStream = bitmap2InputStream(bitmap);
            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream)
                    inputStream.close();
                if (null != outputStream)
                    outputStream.close();
            } catch (IOException ignored) {
            }
        }
        return GetFilePathFromUri.getFileAbsolutePath(this, uri);
    }

    private String saveToLocalFile(Bitmap bitmap) {
        try {
            String fName = UUID.randomUUID().toString();
            File dir = new File(Constant.PICTURE_DIR);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(Constant.PICTURE_DIR + fName + ".jpg");
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
