package io.openim.android.ouicore.utils;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.yanzhenjie.permission.AndPermission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.widget.WebViewActivity;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.PictureElem;
import io.openim.android.sdk.models.VideoElem;
import io.reactivex.Observable;
import q.rorbin.badgeview.QBadgeView;

public class Common {
    /**
     * 主线程handler
     */
    public final static Handler UIHandler = new Handler(Looper.getMainLooper());



    //目标项是否在最后一个可见项之后
    public static  boolean mShouldScroll;
    //记录目标项位置
    public  static int mToPosition;
    /**
     * 滑动到指定位置
     */
    public static  void smoothMoveToPosition(RecyclerView mRecyclerView,  int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem =
            mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前，使用smoothScrollToPosition
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后，最后一个可见项之前
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                // smoothScrollToPosition 不会有效果，此时调用smoothScrollBy来滑动到指定位置
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
    }



    /**
     *  finish routes
     * @param routes
     */
    public static  void finishRoute(String... routes) {
        for (String route : routes) {
            Postcard postcard = ARouter.getInstance().build(route);
            LogisticsCenter.completion(postcard);
            ActivityManager.finishActivity(postcard.getDestination());
        }
    }
    public static void stringBindForegroundColorSpan(TextView textView, String data,
                                                     String target) {
        stringBindForegroundColorSpan(textView, data, target, R.color.theme);
    }

    /**
     * 设置带背景的目标文字
     *
     * @param textView
     * @param data     数据
     * @param target   目标文字
     */
    public static void stringBindForegroundColorSpan(TextView textView, String data,
                                                     String target, int bgColor) {
        SpannableStringBuilder spannableString = new SpannableStringBuilder(data);
        String searchContent = target.toLowerCase(Locale.ROOT);
        data = data.toLowerCase(Locale.ROOT);
        int start = data.indexOf(searchContent);
        if (start == -1) {
            textView.setText(spannableString);
            return;
        }
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(bgColor);
        spannableString.setSpan(colorSpan, start, start + searchContent.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception ignored) {}
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

    public static int px2dp(float px) {
        float scale = BaseApp.inst().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    //收起键盘
    public static void hideKeyboard(Context context, View v) {
        InputMethodManager imm =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
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

    //软键盘是否弹出
    public static boolean isShowKeyboard(Context context) {
        InputMethodManager imm =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //获取状态信息
        return imm.isActive();//true 打开
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
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    /**
     * 复制
     *
     * @param clip 内容
     */
    public static void copy(String clip) {
        ClipboardManager cm =
            (ClipboardManager) BaseApp.inst().getSystemService(Context.CLIPBOARD_SERVICE);
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
        PowerManager.WakeLock wakeLock =
            pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK
                , "openIM:bright");
        //点亮屏幕
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        //释放
        Common.UIHandler.postDelayed(wakeLock::release, 5000);
    }

    /**
     * 是否锁屏
     *
     * @return
     */
    public static boolean isScreenLocked() {
        android.app.KeyguardManager mKeyguardManager =
            (KeyguardManager) BaseApp.inst().getSystemService(Context.KEYGUARD_SERVICE);
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


    public static void permission(Context context, OnGrantedListener onGrantedListener,
                                  boolean hasPermission, String... permissions) {
        if (hasPermission) onGrantedListener.onGranted();
        else {
            AndPermission.with(context).runtime()
                .permission(permissions)
                .onGranted(permission -> {
                // Storage permission are allowed.
                onGrantedListener.onGranted();
            }).onDenied(permission -> {
                // Storage permission are not allowed.
            }).start();
        }
    }

    public interface OnGrantedListener {
        void onGranted();
    }


    //下载图片
    public static Observable<Boolean> downloadFile(String url, String savePath, Uri insertUri) {
        return N.API(OneselfService.class).downloadFileWithDynamicUrlSync(url).compose(N.computationMain()).map(body -> {
            OutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                if (TextUtils.isEmpty(savePath)) {
                    outputStream = BaseApp.inst().getContentResolver().openOutputStream(insertUri
                        , "rw");
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
        });
    }

    /**
     * 地图导航
     *
     * @param message
     * @param v
     */
    public static void toMap(Message message, View v) {
        v.getContext().startActivity(new Intent(v.getContext(), WebViewActivity.class)
            .putExtra(WebViewActivity.LOAD_URL, "https://apis.map.qq.com/uri/v1/geocoder?coord="
                + message.getLocationElem().getLatitude() + "," + message.getLocationElem().getLongitude()
                + "&referer=" + WebViewActivity.mapAppKey)
            .putExtra(WebViewActivity.TITLE,v.getContext().getString(R.string.location)));
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

    public static ActivityResultLauncher<Intent> getCaptureActivityLauncher(AppCompatActivity compatActivity) {
        return compatActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK || null == result.getData()) return;
            String content =
                result.getData().getStringExtra(com.yzq.zxinglibrary.common.Constant.CODED_CONTENT);

            if (content.contains(Constant.QR.QR_ADD_FRIEND)) {
                String userId = content.substring(content.lastIndexOf("/") + 1);
                if (!TextUtils.isEmpty(userId))
                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constant.K_ID, userId).navigation();

            } else if (content.contains(Constant.QR.QR_JOIN_GROUP)) {
                String groupId = content.substring(content.lastIndexOf("/") + 1);
                if (!TextUtils.isEmpty(groupId))
                    ARouter.getInstance().build(Routes.Group.DETAIL).withString(io.openim.android.ouicore.utils.Constant.K_GROUP_ID, groupId).navigation();
            }
        });
    }

    /**
     * 跳转到扫一扫
     */
    public static void jumpScan(Context context, ActivityResultLauncher<Intent> resultLauncher) {
        Intent intent = new Intent(context, CaptureActivity.class);
        ZxingConfig config = new ZxingConfig();
        config.setPlayBeep(true);//是否播放扫描声音 默认为true
        config.setShake(true);//是否震动  默认为true
        config.setDecodeBarCode(true);//是否扫描条形码 默认为true
        config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
        intent.putExtra(com.yzq.zxinglibrary.common.Constant.INTENT_ZXING_CONFIG, config);
        resultLauncher.launch(intent);
    }

    /**
     * 小红点
     *
     * @param context
     * @param target      目标view
     * @param badgeNumber 数
     */
    public static void buildBadgeView(Context context, View target, int badgeNumber) {
        QBadgeView badgeView = (QBadgeView) target.getTag();
        if (null != badgeView) {
            badgeView.setBadgeNumber(badgeNumber);
            return;
        }
        target.setTag(new QBadgeView(context).bindTarget(target).setGravityOffset(10, -2, true).setBadgeNumber(badgeNumber).setBadgeTextSize(8, true).setShowShadow(false));
    }

    public static String containsLink(String text) {
        StringBuilder links=new StringBuilder();
        // 正则表达式模式匹配URL链接
        String pattern = "(http|https)://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*)?";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        while (matcher.find()) {
            links.append(matcher.group());
        }
        return links.toString();
    }

    /**
     * (x,y)是否在view的区域内
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    public static boolean isTouchPointInView(View view, float x, float y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        //view.isClickable() &&
        if (y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }
        return false;
    }


}

