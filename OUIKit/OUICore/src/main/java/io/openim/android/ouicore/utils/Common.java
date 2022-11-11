package io.openim.android.ouicore.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yanzhenjie.permission.runtime.PermissionDef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.widget.WebViewActivity;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.PictureElem;
import io.openim.android.sdk.models.VideoElem;
import io.reactivex.Observable;

public class Common {
    /**
     * 主线程handler
     */
    public final static Handler UIHandler = new Handler(Looper.getMainLooper());


    public static  void  stringBindForegroundColorSpan(TextView textView, String data,
                                  String target){
        stringBindForegroundColorSpan(textView, data, target,Color.parseColor("#009ad6"));
    }
    /**
     *  设置带背景的目标文字
     * @param textView
     * @param data 数据
     * @param target 目标文字
     */
    public static  void stringBindForegroundColorSpan(TextView textView, String data,
                                                      String target,int bgColor) {
        SpannableStringBuilder spannableString = new SpannableStringBuilder(data);
        String searchContent =target.toLowerCase(Locale.ROOT);
        data = data.toLowerCase(Locale.ROOT);
        int start = data
            .indexOf(searchContent);
        if (start == -1) {
            textView.setText(spannableString);
            return;
        }
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(bgColor);
        spannableString.setSpan(colorSpan, start,
            start + searchContent.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }

    public static String md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static int dp2px(float dp) {
        float scale = BaseApp.inst().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    //收起键盘
    public static void hideKeyboard(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    //弹出键盘
    public static void pushKeyboard(Context context) {
        InputMethodManager inputMethodManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 判断是否是字母
     *
     * @param str 传入字符串
     * @return 是字母返回true，否则返回false
     */
    public static boolean isAlpha(String str) {
        if (TextUtils.isEmpty(str)) return false;
        return str.matches("[a-zA-Z]+");
    }

    /**
     * 设置全屏
     *
     * @param activity
     */
    public static void setFullScreen(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    /**
     * 复制
     *
     * @param clip 内容
     */
    public static void copy(String clip) {
        ClipboardManager cm = (ClipboardManager) BaseApp.inst().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("text", clip);
        cm.setPrimaryClip(mClipData);
    }

    /**
     * 唤醒设备
     *
     * @param context
     */
    public static void wakeUp(Context context) {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "openIM:bright");
        //点亮屏幕
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        //释放
        new Handler().postDelayed(wakeLock::release, 5000);
    }

    /**
     * 是否锁屏
     *
     * @return
     */
    public static boolean isScreenLocked() {
        android.app.KeyguardManager mKeyguardManager = (KeyguardManager) BaseApp.inst().getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    public static int getMipmapId(String var) {
        try {
            return BaseApp.inst().getResources().getIdentifier(var, "mipmap",
                BaseApp.inst().getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }


    @SuppressLint("WrongConstant")
    public static void permission(Context context,
                                  OnGrantedListener onGrantedListener, boolean hasPermission,
                                  String... permissions) {
        if (hasPermission)
            onGrantedListener.onGranted();
        else {
            AndPermission.with(context)
                .runtime()
                .permission(permissions)
                .onGranted(permission -> {
                    // Storage permission are allowed.
                    onGrantedListener
                        .onGranted();
                })
                .onDenied(permission -> {
                    // Storage permission are not allowed.
                })
                .start();
        }
    }

    public interface OnGrantedListener {
        void onGranted();
    }


    //下载图片
    public static Observable<Boolean> downloadFile(String url, String savePath, Uri insertUri) {
        return N.API(OneselfService.class)
            .downloadFileWithDynamicUrlSync(url)
            .compose(N.computationMain())
            .map(body -> {
                    OutputStream outputStream = null;
                    InputStream inputStream = null;
                    try {
                        if (TextUtils.isEmpty(savePath)) {
                            outputStream = BaseApp.inst().getContentResolver().openOutputStream(insertUri, "rw");
                        } else {
                            File file = new File(savePath);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            file = new File(savePath + url.substring(url.lastIndexOf("/")));
                            outputStream = new FileOutputStream(file);
                        }

                        byte[] fileReader = new byte[4096];
                        inputStream = body.byteStream();
                        while (true) {
                            int read = inputStream.read(fileReader);
                            if (read == -1) {
                                break;
                            }
                            outputStream.write(fileReader, 0, read);
                        }
                        outputStream.flush();
                    } catch (IOException e) {
                        return false;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                    return true;
                }
            );
    }


    /**
     * 加载图片
     * 判断本地是否存在 本地存在直接加载 不存在加载网络
     *
     * @return
     */
    public static void loadPicture(ImageView iv, PictureElem elem) {
        String url = elem.getSourcePicture().getUrl();
        try {
            String filePath = elem.getSourcePath();
            if (new File(filePath).exists())
                url = filePath;
        } catch (Exception ignore) {
        }
        Glide.with(iv.getContext())
            .load(url)
            .placeholder(R.mipmap.ic_chat_photo)
            .centerInside()
            .into(iv);
    }

    /**
     * 加载视频缩略图
     * 判断本地是否存在 本地存在直接加载 不存在加载网络
     *
     * @return
     */
    public static void loadVideoSnapshot(ImageView iv, VideoElem elem) {
        String snapshotUrl = elem.getSnapshotUrl();
        try {
            if (null == snapshotUrl) {
                String filePath = elem.getSnapshotPath();
                if (new File(filePath).exists())
                    snapshotUrl = filePath;
            }
        } catch (Exception ignore) {
        }
        Glide.with(iv.getContext())
            .load(snapshotUrl)
            .placeholder(R.mipmap.ic_chat_photo)
            .into(iv);
    }

    /**
     * 地图导航
     *
     * @param message
     * @param v
     */
    public static void toMap(Message message, View v) {
     v.getContext().startActivity(new Intent(v.getContext(), WebViewActivity.class)
            .putExtra(WebViewActivity.LOAD_URL, "https://apis.map.qq.com/uri/v1/geocoder?coord=" +
                message.getLocationElem().getLatitude() + "," + message.getLocationElem().getLongitude() + "&referer=" + WebViewActivity.mapAppKey));
    }

    /***
     * 判断字符串是否未null
     * @param sc
     * @return
     */
    public static Boolean isBlank(CharSequence sc) {
        if (sc != null && sc.length() > 0) {
            for (int i = 0; i < sc.length(); i++) {
                if (!Character.isWhitespace(sc.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}

