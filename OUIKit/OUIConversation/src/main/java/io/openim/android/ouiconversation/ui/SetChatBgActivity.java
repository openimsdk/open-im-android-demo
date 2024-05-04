package io.openim.android.ouiconversation.ui;

import static android.os.Environment.DIRECTORY_PICTURES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.hjq.permissions.Permission;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.util.List;

import io.openim.android.ouiconversation.databinding.ActivitySetChatBgBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;

public class SetChatBgActivity extends BaseActivity<ChatVM, ActivitySetChatBgBinding> implements ChatVM.ViewAction {

    private Uri fileUri;
    HasPermissions hasStorage, hasShoot;
    private String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySetChatBgBinding.inflate(getLayoutInflater()));
        sink();

        hasStorage = new HasPermissions(this, Permission.Group.STORAGE);
        hasShoot = new HasPermissions(this, Permission.CAMERA);
        if (vm.isSingleChat) id = vm.userID;
        else id = vm.groupID;

        listener();
    }

    private ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        cachePath(fileUri);
    });
    private ActivityResultLauncher<Intent> albumLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        try {
            List<Uri> uris = (List<Uri>) result.getData().getExtras().get("extra_result_selection");
            if (uris.isEmpty()) return;
            cachePath(uris.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

    });

    private void cachePath(Uri uri) {
        String path = GetFilePathFromUri.getFileAbsolutePath(this, uri);
        SharedPreferencesUtil.get(this).setCache(Constants.K_SET_BACKGROUND + id, path);
        setSuccess(path);
    }

    private void setSuccess(String path) {
        Obs.newMessage(Constants.Event.SET_BACKGROUND, path);
        toast(getString(io.openim.android.ouicore.R.string.set_succ));
        finish();
    }

    private void listener() {
        view.reduction.setOnClickListener(view1 -> {
            SharedPreferencesUtil.remove(this, Constants.K_SET_BACKGROUND + id);
            setSuccess("");
        });
        view.menu1.setOnClickListener(v -> {
            showMediaPicker();
        });
        view.menu2.setOnClickListener(v -> {
            takePhoto();
        });
    }

    /**
     * 拍照
     */
    @SuppressLint("WrongConstant")
    private void takePhoto() {
        hasShoot.safeGo(this::goTakePhoto);
    }

    public File buildTemporaryFile() {
        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), "temporary.jpg");
        } else {
            file = new File(getExternalCacheDir(), "temporary.jpg");
        }
        return file;
    }

    private void goTakePhoto() {
        File file = buildTemporaryFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //如果是7.0以上，使用FileProvider，否则会报错
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileProvider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); //设置拍照后图片保存的位置
        }
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //设置图片保存的格式
        takePhotoLauncher.launch(intent);
    }

    @SuppressLint("WrongConstant")
    private void showMediaPicker() {
        hasStorage.safeGo(this::goMediaPicker);
    }

    private void goMediaPicker() {
        Matisse.from(this).choose(MimeType.ofImage()).countable(true).maxSelectable(1).restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED).thumbnailScale(0.85f).imageEngine(new GlideEngine()).forResult(albumLauncher);
    }

    @Override
    public void scrollToPosition(int position) {

    }

    @Override
    public void closePage() {

    }
}
