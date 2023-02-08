package io.openim.android.ouimoments;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.alibaba.android.arouter.launcher.ARouter;

import java.io.File;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.Constant;

/**
 * 
* @ClassName: MyApplication 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author yiw
* @date 2015-12-28 下午4:21:08 
*
 */
public class MyApplication extends BaseApp {
	// 默认存放图片的路径
	public final static String DEFAULT_SAVE_IMAGE_PATH = Environment.getExternalStorageDirectory() + File.separator + "CircleDemo" + File.separator + "Images"
				+ File.separator;

	private static Context mContext;
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
        initNet();
	}
    private void initNet() {
        BaseApp.inst().loginCertificate=new LoginCertificate();
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVSUQiOiI1OTAxNTAzNjciLCJQbGF0Zm9ybSI6IkFuZHJvaWQiLCJleHAiOjE2ODMwMDg2NzAsIm5iZiI6MTY3NTIzMjM3MCwiaWF0IjoxNjc1MjMyNjcwfQ.Zvx1FsHOZ4Zeq6rTQHPynLd3tOTC9cvafQ-x_J_GnB4";
        BaseApp.inst().loginCertificate.imToken=token;
        BaseApp.inst().loginCertificate.nickname="Oliver";
        BaseApp.inst().loginCertificate.userID="590150367";
        BaseApp.inst().loginCertificate.faceURL="http://img.touxiangwu.com/zb_users/upload/2022/11/202211071667789271294192.jpg";
        N.init(new HttpConfig().setBaseUrl(Constant.getAppAuthUrl()));
    }
	public static Context getContext(){
		return mContext;
	}


}
