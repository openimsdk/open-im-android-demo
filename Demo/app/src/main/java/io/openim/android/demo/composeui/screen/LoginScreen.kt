package io.openim.android.demo.composeui.screen

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.hbb20.CountryCodePicker
import io.openim.android.demo.composeui.core.UIComponent
import io.openim.android.demo.ui.ServerConfigActivity
import io.openim.android.demo.ui.login.LoginActivity.Companion.FORM_LOGIN
import io.openim.android.demo.ui.login.LoginActivity.Companion.buildDefaultLanguage
import io.openim.android.demo.ui.main.MainActivity
import io.openim.android.demo.vm.LoginVM
import io.openim.android.ouicore.R
import io.openim.android.ouicore.utils.Common
import io.openim.android.ouicore.utils.Constants
import io.openim.android.ouicore.utils.SharedPreferencesUtil
import io.openim.android.ouicore.widget.BottomPopDialog
import io.openim.android.ouicore.widget.WaitDialog
import kotlinx.coroutines.CoroutineScope
import java.util.Timer
import java.util.TimerTask

class LoginScreen(val vm: LoginVM): UIComponent<LoginScreen.LoginAction, LoginScreen.LoginState>() {
    //Define ui state
    class LoginState(
        val loginType: LoginType,
        val verificationMethod: VerificationMethod,
        val email: String,
        val phoneNumber: String,
        val password: String,
        val hidePassword: Boolean,
        val smsCode: String,
        val enableLoginBtn: Boolean,
        val versionName: String,
        val smsTimerSec: Int,
        action: (LoginAction) -> Unit
    ) : UIState<LoginAction>(action)


    //Define ui action
    sealed class LoginAction: UIAction() {
        object DoubleClickWelcome: LoginAction()
        class ChangePhoneNumber(val phoneNumber: String): LoginAction()
        class ChangeEmail(val email: String): LoginAction()
        class ChangePassword(val password: String): LoginAction()
        class ChangeSmsCode(val smsCode: String): LoginAction()
        object SwitchLoginType: LoginAction()
        object SwitchVerificationMethod: LoginAction()
        object SwitchPasswordVisibility: LoginAction()
        object ForgotPassword: LoginAction()
        object GetSMSCode: LoginAction()
        object Login: LoginAction()
        object Register: LoginAction()
        class InitializeCountryCodePicker(val picker: CountryCodePicker): LoginAction()
    }


    //Define enums
    enum class LoginType {
        PhoneNumber,
        Email,
    }
    enum class VerificationMethod {
        SMS,
        Password,
    }


    private lateinit var scope: CoroutineScope
    private val waitDialog = WaitDialog(vm.getContext())
    private var timer: Timer? = null


    //Define current ui state
    private var loginType by mutableStateOf(LoginType.Email)
    private var verificationMethod by mutableStateOf(VerificationMethod.Password)
    private var email by mutableStateOf("")
    private var phoneNumber by mutableStateOf("")
    private var areaCodePicker: CountryCodePicker? by mutableStateOf(null)
    private var password by mutableStateOf("")
    private var hidePassword by mutableStateOf(true)
    private var smsCode by mutableStateOf("")
    private var enableLoginBtn by mutableStateOf(false)
    private var versionName by mutableStateOf("Loading...")
    //验证码倒计时
    private var smsTimerSec by mutableStateOf(60)


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun UI(state: LoginState) {
        scope = rememberCoroutineScope()
        MaterialTheme {
            Scaffold { padding ->
                Column(
                    Modifier
                        .padding(padding)
                        .padding(top = 80.dp, start = 30.dp, end = 30.dp)
                ) {
                    Image(painterResource(id = R.mipmap.ic_logo), contentDescription = null, modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterHorizontally))
                    Text(
                        text = stringResource(id = R.string.welcome),
                        color = Color(0xff0089ff),
                        fontSize = 17.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .combinedClickable(onDoubleClick = {
                                state.action(LoginAction.DoubleClickWelcome)
                            }) {}
                    )
                    Column(
                        Modifier
                            .padding(top = 58.dp, bottom = 30.dp)
                            .weight(1f)) {
                        AnimatedContent(targetState = state.loginType) { type ->
                            when (type) {
                                LoginType.PhoneNumber -> {
                                    Column {
                                        Text(text = stringResource(id = R.string.phone_login))
                                        Row(Modifier.padding(start = 5.dp)) {
                                            AndroidView(factory = { ctx ->
                                                CountryCodePicker(ctx).apply {
                                                    changeDefaultLanguage(buildDefaultLanguage())
                                                    setAutoDetectedCountry(true)
                                                    setDefaultCountryUsingNameCode("CN")
                                                    showFlag(false)
                                                    showFullName(false)
                                                    showNameCode(false)

                                                    state.action(LoginAction.InitializeCountryCodePicker(this))
                                                }
                                            })
                                            OutlinedTextField(value = state.phoneNumber, onValueChange = {
                                                state.action(LoginAction.ChangePhoneNumber(it))
                                            }, trailingIcon = {
                                                IconButton(onClick = { state.action(LoginAction.ChangePhoneNumber("")) }) {
                                                    Icon(Icons.Default.Clear, contentDescription = null)
                                                }
                                            }, placeholder = {
                                                Text(text = stringResource(id = R.string.input_phone))
                                            },modifier = Modifier.weight(1f))
                                        }
                                    }
                                }

                                LoginType.Email -> {
                                    Column {
                                        Text(text = stringResource(id = R.string.email_login))
                                        Row(Modifier.padding(start = 5.dp)) {
                                            OutlinedTextField(value = state.email, onValueChange = {
                                                state.action(LoginAction.ChangeEmail(it))
                                            }, trailingIcon = {
                                                IconButton(onClick = { state.action(LoginAction.ChangeEmail("")) }) {
                                                    Icon(Icons.Default.Clear, contentDescription = null)
                                                }
                                            }, placeholder = {
                                                Text(text = stringResource(id = R.string.input_mail))
                                            }, modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedContent(targetState = state.verificationMethod, modifier = Modifier.padding(top = 16.dp)) { type ->
                            when (type) {
                                VerificationMethod.Password -> {
                                    Column {
                                        Text(text = stringResource(id = R.string.password))
                                        OutlinedTextField(value = state.password, onValueChange = {
                                            state.action(LoginAction.ChangePassword(it))
                                        }, trailingIcon = {
                                            Row(Modifier.padding(end = 5.dp)) {
                                                IconButton(onClick = { state.action(LoginAction.ChangePassword("")) }) {
                                                    Icon(Icons.Default.Clear, contentDescription = null)
                                                }
                                                IconButton(onClick = {
                                                    state.action(LoginAction.SwitchPasswordVisibility)
                                                }) {
                                                    if (state.hidePassword) {
                                                        Image(painterResource(id = io.openim.android.demo.R.mipmap.ic_open_eye), contentDescription = null)
                                                    } else {
                                                        Image(painterResource(id = io.openim.android.demo.R.mipmap.ic_close_eye), contentDescription = null)
                                                    }
                                                }
                                            }
                                        }, placeholder = {
                                            Text(text = stringResource(id = R.string.input_password))
                                        }, modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 5.dp))
                                        
                                        Box(
                                            Modifier
                                                .padding(top = 10.dp, bottom = 5.dp, start = 5.dp, end = 5.dp)
                                                .fillMaxWidth()) {
                                            Text(text = stringResource(id = R.string.forgot_password), color = Color(0xff0089ff), fontSize = 12.sp, modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .clickable {
                                                    state.action(LoginAction.ForgotPassword)
                                                })
                                            Text(text = stringResource(id = R.string.vc_login), color = Color(0xff0089ff), fontSize = 12.sp, modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .clickable { state.action(LoginAction.SwitchVerificationMethod) })
                                        }
                                    }
                                }

                                VerificationMethod.SMS -> {
                                    Column {
                                        Text(text = stringResource(id = R.string.vc))
                                        OutlinedTextField(value = state.smsCode, onValueChange = {
                                            state.action(LoginAction.ChangeSmsCode(it))
                                        }, trailingIcon = {
                                            Row {
                                                Icon(Icons.Default.Clear, contentDescription = null)
                                                val text = if (state.smsTimerSec == 60) stringResource(id = R.string.get_vc) else "${state.smsTimerSec}s" 
                                                Text(text = text, color = Color(0xff0089ff), modifier = Modifier
                                                    .padding(start = 5.dp)
                                                    .clickable { state.action(LoginAction.GetSMSCode) }, fontSize = 17.sp)
                                            }
                                        }, placeholder = {
                                            Text(text = stringResource(id = R.string.input_verification_code))
                                        }, modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 5.dp),
                                            // 显示密码样式
                                            keyboardOptions = if (state.hidePassword) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
                                            // 显示密码样式
                                            visualTransformation = if (state.hidePassword) PasswordVisualTransformation() else VisualTransformation.None
                                        )

                                        Box(
                                            Modifier
                                                .padding(top = 10.dp, bottom = 5.dp, start = 5.dp, end = 5.dp)
                                                .fillMaxWidth()) {
                                            Text(text = stringResource(id = R.string.password_login), color = Color(0xff0089ff), fontSize = 12.sp, modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .clickable { state.action(LoginAction.SwitchVerificationMethod) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)) {
                        Button(onClick = {
                            state.action(LoginAction.Login)
                        }, Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 32.dp, top = 15.dp)
                            .height(48.dp),
                            enabled = state.enableLoginBtn,
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.theme)),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(text = stringResource(id = R.string.login), fontSize = 18.sp, color = Color.White)
                        }

                        Spacer(
                            Modifier
                                .height(1.dp)
                                .background(colorResource(id = R.color.def_bg)))

                        Button(onClick = {
                            state.action(LoginAction.SwitchLoginType)
                        }, Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 32.dp, top = 15.dp)
                            .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff0f6ff)),
                            shape = MaterialTheme.shapes.large
                        ) {
                            when(state.loginType) {
                                LoginType.Email -> Text(text = stringResource(id = R.string.phone_login), fontSize = 17.sp, color = Color(0xff0089ff))
                                LoginType.PhoneNumber -> Text(text = stringResource(id = R.string.email_login), fontSize = 17.sp, color = Color(0xff0089ff))
                            }
                        }
                    }

                    Text(text = state.versionName, color = Color(0xff8e9ab0), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

                    Row(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 20.dp, bottom = 50.dp)) {
                        Text(text = stringResource(id = R.string.do_not_account), fontSize = 12.sp, color = Color(0xff8e9ab0))
                        Text(text = stringResource(id = R.string.soon_register), fontSize = 12.sp, color = Color(0xff0089ff), modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable {
                                state.action(LoginAction.Register)
                            })
                    }
                }
            }
        }
    }

    @Composable
    override fun Presenter(): LoginState {
        //init data
        LaunchedEffect(key1 = Unit) {
            initData()
        }

        //callback state
        return LoginState(
            loginType, 
            verificationMethod,
            email,
            phoneNumber,
            password,
            hidePassword,
            smsCode,
            enableLoginBtn,
            versionName,
            smsTimerSec,
        ) { action ->
            //process action
            processAction(action)
        }
    }

    //Async get data
    private suspend fun initData() {
        versionName = Common.getAppPackageInfo().versionName
        vm.isPhone.value = SharedPreferencesUtil.get(vm.getContext()).getInteger(Constants.K_LOGIN_TYPE) == 0
    }

    private fun processAction(action: LoginAction) {
        when (action) {
            LoginAction.DoubleClickWelcome -> {
                vm.getContext().startActivity(Intent(vm.getContext(), ServerConfigActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            is LoginAction.ChangeEmail -> {
                email = action.email
                vm.account.value = email
                enableLoginBtn = submitEnabled()
            }
            is LoginAction.ChangePassword -> {
                password = action.password
                vm.pwd.value = password
                enableLoginBtn = submitEnabled()
            }
            is LoginAction.ChangePhoneNumber -> {
                phoneNumber = action.phoneNumber
                vm.account.value = phoneNumber
                enableLoginBtn = submitEnabled()
            }
            is LoginAction.ChangeSmsCode -> {
                smsCode = action.smsCode
                vm.pwd.value = smsCode
                enableLoginBtn = submitEnabled()
            }
            LoginAction.SwitchLoginType -> {
                loginType = if (loginType == LoginType.PhoneNumber) LoginType.Email else LoginType.PhoneNumber
                vm.isPhone.value = loginType == LoginType.PhoneNumber
            }
            LoginAction.SwitchVerificationMethod -> verificationMethod = if (verificationMethod == VerificationMethod.Password) VerificationMethod.SMS else VerificationMethod.Password
            LoginAction.SwitchPasswordVisibility -> hidePassword = !hidePassword
            LoginAction.ForgotPassword -> {
                val dialog = BottomPopDialog(vm.getContext())
                dialog.mainView.menu1.setText(R.string.forgot_pasword_by_phone)
                dialog.mainView.menu2.setText(R.string.forgot_pasword_by_email)
                dialog.mainView.menu3.setOnClickListener { v1: View? -> dialog.dismiss() }
                dialog.mainView.menu1.setOnClickListener { v1: View? ->
                    // 手机号找回密码
                    vm.isPhone.value = true
                    vm.isFindPassword = true
                    toRegister()
                }
                dialog.mainView.menu2.setOnClickListener { v1: View? ->
                    // 邮箱找回密码
                    vm.isPhone.value = false
                    vm.isFindPassword = true
                    toRegister()
                }
                dialog.show()
            }
            LoginAction.GetSMSCode -> {
                //倒计时结束，可以获取验证码
                if (smsTimerSec == 60) {
                    vm.getVerificationCode(3)
                }
            }
            LoginAction.Login -> {
                vm.areaCode.value = "+" + areaCodePicker?.selectedCountryCode
                waitDialog.show()
                vm.login(if (verificationMethod == VerificationMethod.SMS) vm.pwd.value else null, 3)
            }
            LoginAction.Register -> {
                val dialog = BottomPopDialog(vm.getContext())
                dialog.mainView.menu1.setText(R.string.phone_register)
                dialog.mainView.menu2.setText(R.string.email_register)
                dialog.mainView.menu3.setOnClickListener { v1: View? -> dialog.dismiss() }
                dialog.mainView.menu1.setOnClickListener { v1: View? ->
                    // 手机号注册
                    vm.isPhone.value = true
                    vm.isFindPassword = false
                    toRegister()
                }
                dialog.mainView.menu2.setOnClickListener { v1: View? ->
                    // 邮箱注册
                    vm.isPhone.value = false
                    vm.isFindPassword = false
                    toRegister()
                }
                dialog.show()
            }

            is LoginAction.InitializeCountryCodePicker -> areaCodePicker = action.picker
        }
    }

    private fun submitEnabled(): Boolean {
        return vm.account.value!!.isNotEmpty() && vm.pwd.value!!.isNotEmpty()
    }
    private fun toRegister() {
        vm.getContext().startActivity(Intent(vm.getContext(), RegisterActivity::class.java))
    }


    //Delegates
    fun jump(activity: Activity) {
        vm.getContext().startActivity(Intent(vm.getContext(), MainActivity::class.java).putExtra(FORM_LOGIN, true))
        waitDialog.dismiss()
        activity.finish()
    }

    fun err(msg: String) {
        waitDialog.dismiss()
        Toast.makeText(vm.getContext(), msg, Toast.LENGTH_SHORT).show()
    }

    fun succ(o: Any) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                smsTimerSec--

                if (smsTimerSec <= 0) {
                    smsTimerSec = 60
                    timer!!.cancel()
                    timer = null
                }
            }
        }, 0, 1000)
    }
    fun onDestroy() {
        if (null != timer) {
            timer!!.cancel()
            timer = null
        }
    }
}
