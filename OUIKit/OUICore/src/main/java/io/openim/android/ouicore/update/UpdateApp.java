package io.openim.android.ouicore.update;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson2.JSONArray;
import com.cretin.www.cretinautoupdatelibrary.model.DownloadInfo;
import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.model.UpdateConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.api.NiService;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

public class UpdateApp  {
    public UpdateApp init(int notificationIconRes) {
        OkHttpClient.Builder build = new OkHttpClient.Builder().connectTimeout(10,
            TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS);

        //当你希望使用传入model的方式，让插件自己解析并实现更新
        UpdateConfig updateConfig = new UpdateConfig().setDebug(true)//是否是Debug模式
            .setDataSourceType(TypeConfig.DATA_SOURCE_TYPE_MODEL)//设置获取更新信息的方式
            .setShowNotification(true)//配置更新的过程中是否在通知栏显示进度
            .setNotificationIconRes(notificationIconRes)//配置通知栏显示的图标
            .setUiThemeType(TypeConfig.UI_THEME_G)//配置UI的样式，一种有12种样式可供选择
            .setAutoDownloadBackground(false)//是否需要后台静默下载，如果设置为true，则调用checkUpdate
            // 方法之后会直接下载安装，不会弹出更新页面。当你选择UI样式为TypeConfig
            // .UI_THEME_CUSTOM，静默安装失效，您需要在自定义的Activity中自主实现静默下载，使用这种方式的时候建议setShowNotification
            // (false)，这样基本上用户就会对下载无感知了
            .setCustomDownloadConnectionCreator(new OkHttp3Connection.Creator(build));
        //如果你想使用okhttp作为下载的载体，可以使用如下代码创建一个OkHttpClient，并使用demo中提供的OkHttp3Connection
        // 构建一个ConnectionCreator传入，在这里可以配置信任所有的证书，可解决根证书不被信任导致无法下载apk的问题
        AppUpdateUtils.init(BaseApp.inst(), updateConfig);

        return this;
    }

    public UpdateApp checkUpdate() {
        N.API(NiService.class).post("https://www.pgyer" + ".com/apiv2/app/check",
            new Parameter().add("appKey", "8c728c547000546b886b6824369522bf").add("_api_key",
                "6f43600074306e8bc506ed0cd3275e9e").buildFrom()).map(responseBody -> {
            String string = responseBody.string();
            Map map = GsonHel.fromJson(string, Map.class);
            if (map.containsKey("data") && null != map.get("data")) {
                String toJson = GsonHel.toJson(map.get("data"));
                UpdateInfo updateInfo = GsonHel.fromJson(toJson,
                    UpdateInfo.class);
                DownloadInfo downloadInfo = new DownloadInfo();
                downloadInfo.setApkUrl(updateInfo.downloadURL);
                downloadInfo.setForceUpdateFlag(updateInfo.needForceUpdate ? 2 : 0);
                downloadInfo.setFileSize(Long.parseLong(updateInfo.buildFileSize));
                downloadInfo.setUpdateLog(updateInfo.buildUpdateDescription);
                downloadInfo.setProdVersionCode(Integer.parseInt(updateInfo.buildVersionNo));
                downloadInfo.setProdVersionName((updateInfo.buildVersion));
                return downloadInfo;
            }
            return null;
        }).compose(N.IOMain()).subscribe(new NetObserver<DownloadInfo>("") {
            @Override
            public void onSuccess(DownloadInfo o) {
                AppUpdateUtils.getInstance().checkUpdate(o);
            }

            @Override
            protected void onFailure(Throwable e) {
                Toast.makeText(BaseApp.inst(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return this;
    }

    public static class UpdateInfo {
        public String buildBuildVersion;
        public String forceUpdateVersion;
        public String forceUpdateVersionNo;
        public boolean needForceUpdate;
        public String downloadURL;
        public boolean buildHaveNewVersion;
        public String buildVersionNo;
        public String buildVersion;
        public String buildDescription;
        public String buildUpdateDescription;
        public String appKey;
        public String buildKey;
        public String buildName;
        public String buildIcon;
        public String buildFileKey;
        public String buildFileSize;
    }
}
