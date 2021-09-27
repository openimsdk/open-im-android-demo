package io.openim.demo.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import cn.alvince.zanpakuto.core.C

/**
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
class DemoApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        C.bindApp(this)
    }
}
