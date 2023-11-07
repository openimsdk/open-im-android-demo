package io.openim.android.ouicore.widget;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.net.Uri;
import android.webkit.WebViewClient;



import java.security.Permissions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.databinding.ActivityWebViewBinding;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.L;


public class WebViewActivity extends BaseActivity<BaseViewModel, ActivityWebViewBinding> {

    public final static String ACTION = "action";
    public final static String TITLE = "title";
    public final static String RIGHT = "right";
    public final static String LOAD_URL = "loadUrl";

    //位置
    public final static String LOCATION = "location";

    private String action;
    private String loadUrl;

    //h5 腾讯地图
    private String mapThumbnailUrl;
    public static String mapAppKey = "TMNBZ-3CGC6-C6SSL-EJA3B-E2P5Q-V7F6Q",
        mapThumbnailSize = "1200*600",
        mapBackUrl = "http://callback";


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityWebViewBinding.inflate(getLayoutInflater()));
        sink();
        action = getIntent().getStringExtra(ACTION);
        if (null == action)
            action = "";

        initView();
    }

    public void toBack(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void initView() {
        String title = getIntent().getStringExtra(TITLE);
        if (!TextUtils.isEmpty(title))
            view.title.setText(title);
        String right = getIntent().getStringExtra(RIGHT);
        if (null == right)
            right = "";
        view.right.setText(right);


        view.right.setOnClickListener(v -> finish());

        WebSettings webSettings = view.webView.getSettings();
        // 允许调用 JS，因为网页地图使用的是 JS 定位
        webSettings.setJavaScriptEnabled(true);
        //启用数据库
        webSettings.setDatabaseEnabled(true);
        //启用地理定位，默认为true
        webSettings.setGeolocationEnabled(true);
        //设置定位的数据库路径
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setGeolocationDatabasePath(dir);
        //开启DomStorage缓存
        webSettings.setDomStorageEnabled(true);

        view.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int newProgress) {
                if (newProgress == 100) {
                    view.progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    view.progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    view.progressBar.setProgress(newProgress);//设置进度值
                }
                super.onProgressChanged(v, newProgress);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });

        view.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (TextUtils.isEmpty(url)) return false;

                try {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {//防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }

                if (action.equals(LOCATION)) {
                    if (url.startsWith(mapBackUrl)) {
                        try {
                            Uri uri = request.getUrl();
                            Bundle bundle = new Bundle();
                            Map<String, String> result = new HashMap<>();
                            String[] query = uri.getQuery().split("&");
                            for (String parm : query) {
                                String[] par = parm.split("=");
                                result.put(par[0], par[1]);
                            }
                            String lat = uri.getQueryParameter("latng");
                            //latitude, longitude
                            String[] lats = lat.split(",");
                            bundle.putDouble("latitude", Double.parseDouble(lats[0]));
                            bundle.putDouble("longitude", Double.parseDouble(lats[1]));
                            result.put("latitude", lats[0]);
                            result.put("longitude", lats[1]);
                            result.put("url", mapThumbnailUrl.replace("%s", lat));
                            bundle.putString("description", GsonHel.toJson(result));

                            setResult(RESULT_OK, new Intent().putExtra("result", bundle));
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                        return true;
                    }
                }

                //返回值是true的时候控制去WebView打开
                view.loadUrl(url);
                return true;
            }
        });
        switch (action) {
            default:
                loadUrl = getIntent().getStringExtra(LOAD_URL);
                view.webView.loadUrl(loadUrl);
                break;
            case LOCATION:
                buildLocation();
                break;
        }

    }

    private void buildLocation() {
        loadUrl =
            "https://apis.map.qq.com/tools/locpicker?search=1&type=0&backurl=" + mapBackUrl + "&key=" + mapAppKey + "&referer=myapp&policy=1";
        mapThumbnailUrl =
            "https://apis.map.qq.com/ws/staticmap/v2/?center=%s&zoom=18&size=" + mapThumbnailSize + "&maptype=roadmap&markers=size:large|color:0xFFCCFF|label:k|%s&key=" + mapAppKey;

        view.right.setText(R.string.sure);
        view.title.setText(R.string.my_location);
        view.webView.loadUrl(loadUrl);
    }
}
