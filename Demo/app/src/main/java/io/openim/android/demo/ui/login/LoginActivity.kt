package io.openim.android.demo.ui.login

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.hbb20.CountryCodePicker
import io.openim.android.demo.composeui.screen.LoginScreen
import io.openim.android.demo.databinding.ActivityLoginBinding
import io.openim.android.demo.vm.LoginVM
import io.openim.android.ouicore.base.BaseActivity
import io.openim.android.ouicore.base.BaseApp
import io.openim.android.ouicore.utils.LanguageUtil
import io.openim.android.ouicore.utils.SinkHelper
import io.openim.android.ouicore.widget.WaitDialog
import java.util.Locale
import java.util.Timer

class LoginActivity : BaseActivity<LoginVM, ActivityLoginBinding>(), LoginVM.ViewAction {
    private lateinit var screen: LoginScreen


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApp.inst().removeCacheVM(LoginVM::class.java)
        bindVM(LoginVM::class.java, true)
        bindViewDataBinding(ActivityLoginBinding.inflate(getLayoutInflater()));
        setLightStatus()
        SinkHelper.get(this).setTranslucentStatus(null)

        setContent {
            screen = remember { LoginScreen(vm!!) }
            screen.Main()
        }
    }

    override fun jump() {
        screen.jump(this)
    }

    override fun err(msg: String) {
        screen.err(msg)
    }

    override fun succ(o: Any) {
        screen.succ(o)
    }

    override fun initDate() {
    }

    override fun onDestroy() {
        super.onDestroy()
        screen.onDestroy()
    }

    companion object {
        const val FORM_LOGIN: String = "form_login"
        fun buildDefaultLanguage(): CountryCodePicker.Language {
            val locale = LanguageUtil.getCurrentLocale(BaseApp.inst())
            val language = if (locale.language == Locale.CHINA.language) CountryCodePicker.Language.CHINESE_SIMPLIFIED
            else CountryCodePicker.Language.forCountryNameCode(locale.language)
            return language
        }
    }
}
