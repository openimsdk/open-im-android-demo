package io.openim.demo.home.binder

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import cn.alvince.zanpakuto.databinding.property.intField
import cn.alvince.zanpakuto.databinding.property.observableField
import io.openim.demo.BR

/**
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
class HomeNavItemVm : BaseObservable() {

    @get:Bindable
    var title: String by observableField(BR.title, "")

    @get:Bindable
    var icon: Int by intField(BR.icon)

    @get:Bindable
    var badge: Int by intField(BR.badge)
}
