package io.openim.demo.home.viewmodel

import android.app.Application
import androidx.databinding.ViewDataBinding
import io.openim.demo.app.arch.AppViewModel
import io.openim.demo.app.binding.BRW
import io.openim.demo.home.binder.HomeNavHostVm

/**
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
class MainHostViewModel(application: Application) : AppViewModel(application) {

    private val binder = HomeNavHostVm()

    override fun bindView(binding: ViewDataBinding) {
        super.bindView(binding)
        binding.setVariable(BRW.binder, binder)
    }
}
