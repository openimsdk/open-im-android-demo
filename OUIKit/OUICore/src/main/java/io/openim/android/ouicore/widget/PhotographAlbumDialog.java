package io.openim.android.ouicore.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.hjq.permissions.Permission;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.databinding.DialogPhotographAlbumBinding;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.GlideEngine;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.L;

public class PhotographAlbumDialog extends BaseDialog {
    private AppCompatActivity compatActivity;
    private OnSelectResultListener onSelectResultListener;
    private ActivityResultLauncher<Intent> cropLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
    private ActivityResultLauncher<Intent> albumLauncher;
    private WaitDialog waitDialog;
    private int maxSelectable = 1;
    private boolean isToCrop = true;

    public PhotographAlbumDialog(@NonNull AppCompatActivity context) {
        super(context);
        this.compatActivity = context;
        initView();
    }

    public PhotographAlbumDialog(@NonNull AppCompatActivity context, int themeResId) {
        super(context, themeResId);
        this.compatActivity = context;
        initView();
    }

    protected PhotographAlbumDialog(@NonNull AppCompatActivity context, boolean cancelable,
                                    @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.compatActivity = context;
        initView();
    }

    private HasPermissions hasStorage, hasShoot;
    DialogPhotographAlbumBinding view;

    Uri fileUri;

    public void initView() {
        initLauncher();
        Common.UIHandler.postDelayed(() -> {
            hasStorage = new HasPermissions(getContext(), Permission.MANAGE_EXTERNAL_STORAGE);
            hasShoot = new HasPermissions(getContext(), Permission.CAMERA);
        }, 200);

        Window win = this.getWindow();
        win.requestFeature(Window.FEATURE_NO_TITLE);

        view = DialogPhotographAlbumBinding.inflate(getLayoutInflater());
        click();
        setContentView(view.getRoot());

        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.windowAnimations = R.style.dialog_animation;
        lp.gravity = Gravity.BOTTOM;
        win.setAttributes(lp);
        win.setBackgroundDrawableResource(android.R.color.transparent);
    }

    public interface OnSelectResultListener {
        void onResult(String... paths);
    }

    public void setOnSelectResultListener(OnSelectResultListener onSelectResultListener) {
        this.onSelectResultListener = onSelectResultListener;
    }

    private void initLauncher() {
        cropLauncher =
            compatActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK) return;
                String path = GetFilePathFromUri.getFileAbsolutePath(compatActivity, fileUri);
                dismiss();
                if (null != onSelectResultListener) onSelectResultListener.onResult(path);
            });

        takePhotoLauncher =
            compatActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK) return;
                if (isToCrop)
                    goCrop(fileUri);
                else if (null != onSelectResultListener)
                    onSelectResultListener.onResult(GetFilePathFromUri.getFileAbsolutePath(getContext(), fileUri));
            });
        albumLauncher =
            compatActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK) return;
                try {
                    List<LocalMedia> localMediaList = PictureSelector.obtainSelectorList(result.getData());
                    if (isToCrop) {
                        Uri uri = Uri.parse(localMediaList.get(0).getAvailablePath());
                        goCrop(uri);
                    } else if (null != onSelectResultListener) {
                        String[] paths = new String[localMediaList.size()];
                        for (int i = 0; i < localMediaList.size(); i++) {
                            paths[i] = GetFilePathFromUri.getFileAbsolutePath(getContext(),
                                Uri.parse(localMediaList.get(i).getAvailablePath()));
                        }
                        onSelectResultListener.onResult(paths);
                    }
                } catch (Exception e) {
                    L.e(e.getMessage());
                }

            });

    }

    /**
     * 裁剪照片
     */
    private void goCrop(Uri sourceUri) {
        fileUri = Uri.fromFile(new File(compatActivity.getExternalCacheDir(), System.currentTimeMillis() + ".jpg"));
        File file = new File(sourceUri.getPath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = uriToFileApiQ(sourceUri, BaseApp.inst());
            fileUri = FileProvider.getUriForFile(BaseApp.inst(),
                BaseApp.inst().getPackageName() + ".fileProvider", file);
        }
        Uri uri = Uri.fromFile(file);
        UCrop.of(uri, fileUri)
            .withAspectRatio(1, 1)
            .withMaxResultSize(500, 500)
            .start(this.compatActivity, cropLauncher);
    }

    @TargetApi(Build.VERSION_CODES.Q)
    public static File uriToFileApiQ(Uri uri, Context context) {
        File file = null;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
//            注释掉的方法可以获取到原文件的文件名，但是比较耗时
//            Cursor cursor = contentResolver.query(uri, null, null, null, null);
//            if (cursor.moveToFirst()) {
//                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns
//                .DISPLAY_NAME));}
            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(Constants.PICTURE_DIR, displayName);
                cache.createNewFile();

                FileOutputStream fos = new FileOutputStream(cache);
                FileUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    private void click() {
        view.menu1.setOnClickListener(v -> {
            showMediaPicker();
        });
        view.menu2.setOnClickListener(v -> {
            takePhoto();
        });
        view.menu3.setOnClickListener(v -> dismiss());
    }

    /**
     * 拍照
     */
    @SuppressLint("WrongConstant")
    private void takePhoto() {
        hasShoot.safeGo(this::goTakePhoto);
    }

    public File buildTemporaryFile() {
        return new File(Constants.PICTURE_DIR, System.currentTimeMillis() + ".jpg");
    }

    private void goTakePhoto() {
        File file = buildTemporaryFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //如果是7.0以上，使用FileProvider，否则会报错
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileUri = FileProvider.getUriForFile(compatActivity,
                compatActivity.getPackageName() + ".fileProvider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); //设置拍照后图片保存的位置
        }
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //设置图片保存的格式
        takePhotoLauncher.launch(intent);
    }

    private void showMediaPicker() {
        hasStorage.safeGo(this::goMediaPicker);
    }

    private void goMediaPicker() {
        try {
            PictureSelector.create(compatActivity)
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .setMaxSelectNum(maxSelectable)
                .forResult(albumLauncher);
        } catch (Exception e) {
            L.e(e.getMessage());
        }
    }

    public void setMaxSelectable(int maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    public void setToCrop(boolean toCrop) {
        isToCrop = toCrop;
    }
}


