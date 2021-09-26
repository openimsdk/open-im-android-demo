package io.openim.demo.app.arch

import android.app.Application
import androidx.databinding.ViewDataBinding
import cn.alvince.zanpakuto.lifecycle.LifecycleAppViewModel

/**
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
open class AppViewModel(application: Application) : LifecycleAppViewModel(application) {

    open fun bindView(binding: ViewDataBinding) {}
}
