package io.openim.demo.app

import android.app.Application
import cn.alvince.zanpakuto.core.C

/**
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        C.bindApp(this)
    }
}
