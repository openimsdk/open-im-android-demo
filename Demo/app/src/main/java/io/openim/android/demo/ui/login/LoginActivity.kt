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
    private var waitDialog: WaitDialog? = null

    //是否是验证码登录
    private var isVCLogin = false
    private var timer: Timer? = null

    //验证码倒计时
    private var countdown = 60

    private lateinit var screen: LoginScreen


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApp.inst().removeCacheVM(LoginVM::class.java)
        bindVM(LoginVM::class.java, true)
        bindViewDataBinding(ActivityLoginBinding.inflate(getLayoutInflater()));
        setLightStatus()
        SinkHelper.get(this).setTranslucentStatus(null)

//        initView();
//
//        click();
//        listener();

        setContent {
            screen = remember { LoginScreen(vm!!) }
            screen.Main()
        }
    }

//    fun initView() {
//        waitDialog = WaitDialog(this)
//        view!!.loginContent.loginVM = vm
//        view!!.loginVM = vm
//
//        val language = buildDefaultLanguage()
//        view!!.loginContent.countryCode.changeDefaultLanguage(language)
//
//        view!!.version.text = Common.getAppPackageInfo().versionName
//        vm!!.isPhone.value = SharedPreferencesUtil.get(this).getInteger(Constants.K_LOGIN_TYPE) == 0
//    }
//
//    private fun listener() {
//        vm!!.isPhone.observe(this) { v: Boolean ->
//            if (v) {
//                view!!.phoneTv.setTextColor(Color.parseColor("#1D6BED"))
//                view!!.mailTv.setTextColor(Color.parseColor("#333333"))
//                view!!.phoneVv.visibility = View.VISIBLE
//                view!!.mailVv.visibility = View.INVISIBLE
//            } else {
//                view!!.mailTv.setTextColor(Color.parseColor("#1D6BED"))
//                view!!.phoneTv.setTextColor(Color.parseColor("#333333"))
//                view!!.mailVv.visibility = View.VISIBLE
//                view!!.phoneVv.visibility = View.INVISIBLE
//            }
//            view!!.loginContent.edt1.setText("")
//            view!!.loginContent.edt2.setText("")
//            submitEnabled()
//            view!!.loginContent.edt1.hint = if (v) getString(R.string.input_phone) else getString(R.string.input_mail)
//        }
//        vm!!.account.observe(this) { v: String? -> submitEnabled() }
//        vm!!.pwd.observe(this) { v: String? -> submitEnabled() }
//    }
//
//    private fun toRegister() {
//        startActivity(Intent(this, RegisterActivity::class.java))
//    }
//
//    private val gestureDetector = GestureDetector(BaseApp.inst(),
//        object : GestureDetector.SimpleOnGestureListener() {
//            override fun onDoubleTap(e: MotionEvent): Boolean {
//                startActivity(Intent(this@LoginActivity, ServerConfigActivity::class.java))
//                return super.onDoubleTap(e)
//            }
//        })
//
//    private fun click() {
//        view!!.welcome.setOnTouchListener { v, event -> gestureDetector.onTouchEvent(event) }
//        view!!.changeLoginType.setOnClickListener { v ->
//            vm!!.isPhone.setValue(!vm!!.isPhone.`val`())
//        }
//
//        view!!.loginContent.clear.setOnClickListener { v -> view!!.loginContent.edt1.setText("") }
//        view!!.loginContent.clearPwd.setOnClickListener { v -> view!!.loginContent.edt2.setText("") }
//        view!!.loginContent.eyes.setOnCheckedChangeListener { buttonView, isChecked ->
//            view!!.loginContent.edt2.transformationMethod =
//                if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
//        }
//        view!!.protocol.setOnCheckedChangeListener { buttonView, isChecked -> submitEnabled() }
//        view!!.registerTv.setOnClickListener(object : OnDedrepClickListener() {
//            override fun click(v: View) {
//                val dialog = BottomPopDialog(this@LoginActivity)
//                dialog.mainView.menu1.setText(R.string.phone_register)
//                dialog.mainView.menu2.setText(R.string.email_register)
//                dialog.mainView.menu3.setOnClickListener { v1: View? -> dialog.dismiss() }
//                dialog.mainView.menu1.setOnClickListener { v1: View? ->
//                    vm!!.isPhone.value = true
//                    vm!!.isFindPassword = false
//                    toRegister()
//                }
//                dialog.mainView.menu2.setOnClickListener { v1: View? ->
//                    vm!!.isPhone.value = false
//                    vm!!.isFindPassword = false
//                    toRegister()
//                }
//                dialog.show()
//            }
//        })
//        view!!.submit.setOnClickListener { v ->
//            vm!!.areaCode.value = "+" + view!!.loginContent.countryCode.selectedCountryCode
//            waitDialog!!.show()
//            vm!!.login(if (isVCLogin) vm!!.pwd.value else null, 3)
//        }
//        view!!.loginContent.vcLogin.setOnClickListener { v ->
//            isVCLogin = !isVCLogin
//            updateEdit2()
//        }
//        view!!.loginContent.getVC.setOnClickListener { v ->
//            //正在倒计时中...不触发操作
//            if (countdown != 60) return@setOnClickListener
//            vm!!.getVerificationCode(3)
//        }
//        view!!.loginContent.forgotPasswordTv.setOnClickListener { v ->
//            val dialog = BottomPopDialog(this@LoginActivity)
//            dialog.mainView.menu1.setText(R.string.forgot_pasword_by_phone)
//            dialog.mainView.menu2.setText(R.string.forgot_pasword_by_email)
//            dialog.mainView.menu3.setOnClickListener { v1: View? -> dialog.dismiss() }
//            dialog.mainView.menu1.setOnClickListener { v1: View? ->
//                vm!!.isPhone.value = true
//                vm!!.isFindPassword = true
//                toRegister()
//            }
//            dialog.mainView.menu2.setOnClickListener { v1: View? ->
//                vm!!.isPhone.value = false
//                vm!!.isFindPassword = true
//                toRegister()
//            }
//            dialog.show()
//        }
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//    }
//
//    private fun updateEdit2() {
//        if (isVCLogin) {
//            view!!.loginContent.vcTitle.setText(R.string.vc)
//            view!!.loginContent.vcLogin.setText(R.string.password_login)
//            view!!.loginContent.getVC.visibility = View.VISIBLE
//            view!!.loginContent.eyes.visibility = View.GONE
//            view!!.loginContent.edt2.setHint(R.string.input_verification_code)
//        } else {
//            view!!.loginContent.vcTitle.setText(R.string.password)
//            view!!.loginContent.vcLogin.setText(R.string.vc_login)
//            view!!.loginContent.getVC.visibility = View.GONE
//            view!!.loginContent.eyes.visibility = View.VISIBLE
//            view!!.loginContent.edt2.setHint(R.string.input_password)
//        }
//    }
//
//    private fun submitEnabled() {
//        view!!.submit.isEnabled = !vm!!.account.value!!.isEmpty() && !vm!!.pwd.value!!.isEmpty()
//    }

    override fun jump() {
//        startActivity(Intent(this, MainActivity::class.java).putExtra(FORM_LOGIN, true))
//        waitDialog!!.dismiss()
//        finish()
        screen.jump(this)
    }

    override fun err(msg: String) {
//        waitDialog!!.dismiss()
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        screen.err(msg)
    }

    override fun succ(o: Any) {
//        timer = Timer()
//        timer!!.schedule(object : TimerTask() {
//            override fun run() {
//                countdown--
//                runOnUiThread(object : TimerTask() {
//                    override fun run() {
//                        view!!.loginContent.getVC.text = countdown.toString() + "s"
//                    }
//                })
//
//                if (countdown <= 0) {
//                    view!!.loginContent.getVC.setText(R.string.get_vc)
//                    countdown = 60
//                    timer!!.cancel()
//                    timer = null
//                }
//            }
//        }, 0, 1000)
        screen.succ(o)
    }

    override fun initDate() {
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (null != timer) {
//            timer!!.cancel()
//            timer = null
//        }
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
